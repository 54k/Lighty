package io.lighty.nio;

import io.lighty.concurrent.ScheduledFuture;
import io.lighty.AbstractChannel;
import io.lighty.ChannelException;
import io.lighty.ChannelFuture;
import io.lighty.ChannelFutureListener;
import io.lighty.ChannelOutboundBuffer;
import io.lighty.ChannelPromise;
import io.lighty.EventLoop;
import io.lighty.RegistrationException;
import io.lighty.ServerChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractNioChannel extends AbstractChannel {

    protected int readOp = SelectionKey.OP_READ;
    private volatile SelectionKey selectionKey;

    protected AbstractNioChannel(SelectableChannel ch) {
        this(null, ch);
    }

    protected AbstractNioChannel(AbstractNioChannel parent, SelectableChannel ch) {
        super(parent, ch);
    }

    @Override
    public NioEventLoop eventLoop() {
        return (NioEventLoop) super.eventLoop();
    }

    @Override
    protected boolean isEventLoopCompatible(EventLoop eventLoop) {
        return eventLoop instanceof NioEventLoop;
    }

    @Override
    public NioUnsafe unsafe() {
        return (NioUnsafe) super.unsafe();
    }

    @Override
    protected abstract AbstractNioUnsafe newUnsafe();

    @Override
    public SelectableChannel javaChannel() {
        return (SelectableChannel) super.javaChannel();
    }

    public static interface NioUnsafe extends Unsafe {

        void read();

        void write();

        void finishConnect();
    }

    protected abstract class AbstractNioUnsafe extends AbstractUnsafe implements NioUnsafe {

        private final List<Object> messages = new ArrayList<>(config().getReadSpinCount());

        private final Runnable writeTask = new Runnable() {
            @Override
            public void run() {
                interestOps(interestOps() | SelectionKey.OP_WRITE);
            }
        };
        private ChannelPromise connectPromise;
        private ScheduledFuture<?> connectTimeout;

        @Override
        protected void writeRequested() {
            if (eventLoop().inExecutorThread()) {
                writeTask.run();
            } else {
                invokeLater(writeTask);
            }
        }

        private final Runnable readTask = new Runnable() {
            @Override
            public void run() {
                interestOps(interestOps() | readOp);
            }
        };

        @Override
        protected void readRequested() {
            if (eventLoop().inExecutorThread()) {
                readTask.run();
            } else {
                invokeLater(readTask);
            }
        }

        @Override
        public void connect(final InetSocketAddress address, ChannelPromise channelPromise) {
            if (!channelPromise.setUncancellable()) {
                return;
            }

            try {
                boolean wasActive = isActive();
                if (doConnect(address, channelPromise)) {
                    safeSetSuccess(channelPromise);
                    if (!wasActive && isActive()) {
                        pipeline().fireOpen();
                        if (config().isAutoRead()) {
                            readRequested();
                        }
                    }
                } else {
                    connectPromise = channelPromise;
                    long connectTimeoutMillis = config().getConnectTimeoutMillis();

                    if (connectTimeoutMillis > 0) {
                        connectTimeout = eventLoop().schedule(new Runnable() {
                            @Override
                            public void run() {
                                ChannelException cause = new ChannelException("Connection timeout: " + address);
                                if (connectPromise != null && connectPromise.tryFailure(cause)) {
                                    closeForcibly();
                                }
                            }
                        }, config().getConnectTimeoutMillis(), TimeUnit.MILLISECONDS);
                    }

                    channelPromise.addListener(new ChannelFutureListener() {
                        @Override
                        public void onComplete(ChannelFuture future) {
                            if (future.isCancelled()) {
                                if (connectTimeout != null) {
                                    connectTimeout.cancel();
                                }
                                connectPromise = null;
                                closeForcibly();
                            }
                        }
                    });

                    interestOps(SelectionKey.OP_CONNECT);
                }
            } catch (Throwable t) {
                safeSetFailure(channelPromise, t);
                closeForcibly();
            }
        }

        protected abstract boolean doConnect(InetSocketAddress address, ChannelPromise channelPromise) throws Exception;

        @Override
        public void finishConnect() {
            boolean wasActive = isActive();
            try {
                if (doFinishConnect()) {
                    boolean connectSuccess = connectPromise.trySuccess();

                    if (!wasActive && isActive()) {
                        if (config().isAutoRead()) {
                            readRequested();
                        }
                        pipeline().fireOpen();
                    }

                    if (!connectSuccess) {
                        closeForcibly();
                    }
                } else {
                    safeSetFailure(connectPromise, new ChannelException("Connection failed"));
                    closeForcibly();
                }
            } catch (Throwable t) {
                safeSetFailure(connectPromise, t);
                closeForcibly();
            }
        }

        protected abstract boolean doFinishConnect() throws Exception;

        @Override
        public void read() {
            Throwable error = null;
            boolean closed = false;
            try {
                int messagesRead = 0;
                try {
                    for (int i = 0; i < config().getReadSpinCount(); i++) {
                        int read = doReadMessages(messages);
                        if (read == 0) {
                            break;
                        }
                        if (read < 0) {
                            closed = true;
                            break;
                        }
                        messagesRead += read;
                    }
                } catch (Throwable e) {
                    error = e;
                }

                for (int i = 0; i < messagesRead; i++) {
                    pipeline().fireMessageReceived(messages.get(i));
                }

                if (error != null) {
                    if (error instanceof IOException) {
                        closed = !(AbstractNioChannel.this instanceof ServerChannel);
                    }
                    pipeline().fireExceptionCaught(error);
                }

                if (closed && isOpen()) {
                    close(voidPromise());
                }
                messages.clear();
            } finally {
                if (isActive() && !config().isAutoRead()) {
                    removeReadOp();
                }
            }
        }

        /**
         * @return number of messages read or -1 if end of stream occurred
         */
        protected abstract int doReadMessages(List<Object> messages) throws Exception;

        @Override
        public void flush() {
            if (isFlushPending()) {
                return;
            }
            super.flush();
        }

        private boolean isFlushPending() {
            return selectionKey.isValid() && (selectionKey.interestOps() & SelectionKey.OP_WRITE) != 0;
        }

        @Override
        public void write() {
            super.flush();
        }

        @Override
        protected void doWrite(ChannelOutboundBuffer channelOutboundBuffer) throws Exception {
            boolean done = false;
            Object message = channelOutboundBuffer.current();
            if (message != null) {
                for (int i = 0; i < config().getWriteSpinCount(); i++) {
                    if (doWriteMessage(message)) {
                        done = true;
                        break;
                    }
                }
            }

            if (done) {
                channelOutboundBuffer.remove();
            }
        }

        protected boolean doWriteMessage(Object message) throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void afterRegister() {
            try {
                if (isOpen()) {
                    try {
                        javaChannel().configureBlocking(false);
                    } catch (IOException e) {
                        throw new ChannelException(e);
                    }
                }
                selectionKey = javaChannel().register(eventLoop().selector, 0, AbstractNioChannel.this);
            } catch (ClosedChannelException e) {
                throw new ChannelException(e);
            }
        }

        @Override
        protected void afterUnregister() {
            eventLoop().cancel(selectionKey);
            if (isOpen()) {
                try {
                    javaChannel().configureBlocking(true);
                } catch (IOException e) {
                    throw new ChannelException(e);
                }
            }
        }

        @Override
        public boolean isActive() {
            return javaChannel().isOpen();
        }

        @Override
        public boolean isOpen() {
            return javaChannel().isOpen();
        }

        private void removeReadOp() {
            interestOps(interestOps() & ~readOp);
        }

        @Override
        public void closeForcibly() {
            try {
                javaChannel().close();
            } catch (IOException ignore) {
            }
        }

        protected int interestOps() {
            checkRegistered();
            if (selectionKey.isValid()) {
                return selectionKey.interestOps();
            }
            return 0;
        }

        protected void interestOps(int interestOps) {
            checkRegistered();
            if ((interestOps & ~javaChannel().validOps()) != 0) {
                throw new IllegalArgumentException("interestOps are not valid.");
            }

            if (selectionKey.isValid()) {
                selectionKey.interestOps(interestOps);
                eventLoop().wakeUpSelector();
            } else {
                closeForcibly();
            }
        }

        private void checkRegistered() {
            if (!isRegistered()) {
                throw new RegistrationException("Not registered to dispatcher.");
            }
        }


    }
}
