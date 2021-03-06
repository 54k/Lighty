package io.lighty;

import io.lighty.buffer.ByteBufferPool;
import io.lighty.buffer.RecvByteBufferAllocator;
import io.lighty.concurrent.EventExecutor;
import io.lighty.concurrent.GlobalEventExecutor;
import io.lighty.pipeline.DefaultPipeline;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractChannel implements Channel {

    protected static final ClosedChannelException CLOSED_CHANNEL_EXCEPTION = new ClosedChannelException();
    protected static final NotYetConnectedException NOT_YET_CONNECTED_EXCEPTION = new NotYetConnectedException();

    private final ChannelPromise VOID_PROMISE = new VoidChannelPromise(this);
    private final DefaultPipeline pipeline;
    private final Channel parent;
    private final Object ch;
    private final Unsafe unsafe;
    private final ChannelConfig config;

    private volatile Object attachment;
    private volatile EventLoop eventLoop;
    private volatile boolean registered;
    private SocketAddress localAddress;
    private SocketAddress remoteAddress;

    private String strCache;

    protected AbstractChannel(Channel parent, Object ch) {
        this.parent = parent;
        this.ch = ch;

        config = newConfig();
        unsafe = newUnsafe();

        pipeline = new DefaultPipeline(this);
    }

    protected static void safeSetSuccess(ChannelPromise channelPromise) {
        channelPromise.trySuccess();
    }

    protected static void safeSetFailure(ChannelPromise channelPromise, Throwable error) {
        channelPromise.tryFailure(error);
    }

    @Override
    public Channel parent() {
        return parent;
    }

    @Override
    public SocketAddress getLocalAddress() {
        if (localAddress == null) {
            try {
                return localAddress = unsafe().getLocalAddress();
            } catch (Exception ignore) {
                return null;
            }
        }
        return localAddress;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        if (remoteAddress == null) {
            try {
                return remoteAddress = unsafe().getRemoteAddress();
            } catch (Exception ignore) {
                return null;
            }
        }
        return remoteAddress;
    }

    @Override
    public ChannelPromise newChannelPromise() {
        return new DefaultChannelPromise(this, eventLoop());
    }

    @Override
    public ChannelPromise voidPromise() {
        return VOID_PROMISE;
    }

    @Override
    public boolean isRegistered() {
        return registered;
    }

    @Override
    public boolean isActive() {
        return unsafe().isActive();
    }

    @Override
    public ChannelConfig config() {
        return config;
    }

    protected ChannelConfig newConfig() {
        return new DefaultChannelConfig(this);
    }

    @Override
    public Object attach(Object attachment) {
        return this.attachment = attachment;
    }

    @Override
    public Object attachment() {
        return attachment;
    }

    @Override
    public DefaultPipeline pipeline() {
        return pipeline;
    }

    @Override
    public EventLoop eventLoop() {
        return eventLoop;
    }

    @Override
    public ChannelFuture bind(InetSocketAddress address) {
        ChannelPromise channelPromise = newChannelPromise();
        unsafe().bind(address, channelPromise);
        return channelPromise;
    }

    @Override
    public ChannelFuture connect(InetSocketAddress address) {
        ChannelPromise channelPromise = newChannelPromise();
        unsafe().connect(address, channelPromise);
        return channelPromise;
    }

    @Override
    public ChannelFuture disconnect() {
        ChannelPromise channelPromise = newChannelPromise();
        unsafe().disconnect(channelPromise);
        return channelPromise;
    }

    @Override
    public ChannelFuture read() {
        ChannelPromise channelPromise = newChannelPromise();
        pipeline().fireRead(channelPromise);
        return channelPromise;
    }

    @Override
    public ChannelFuture write(Object message, ChannelPromise channelPromise) {
        pipeline().fireMessageSent(message, channelPromise);
        return channelPromise;
    }

    @Override
    public ChannelFuture write(Object message) {
        return write(message, newChannelPromise());
    }

    @Override
    public ChannelFuture close() {
        ChannelPromise channelPromise = newChannelPromise();
        pipeline().fireClosing(channelPromise);
        return channelPromise;
    }

    @Override
    public ChannelFuture closeFuture() {
        return unsafe().closeFuture();
    }

    @Override
    public ChannelFuture unregister() {
        return eventLoop().unregister(this);
    }

    @Override
    public ChannelFuture register(EventLoop eventLoop) {
        if (!isEventLoopCompatible(eventLoop)) {
            throw new RegistrationException("event loop is not compatible.");
        }
        return eventLoop.register(this);
    }

    @Override
    public Object javaChannel() {
        return ch;
    }

    protected abstract boolean isEventLoopCompatible(EventLoop eventLoop);

    @Override
    public Unsafe unsafe() {
        return unsafe;
    }

    protected abstract AbstractUnsafe newUnsafe();

    @Override
    public ByteBufferPool byteBufferPool() {
        return config.getByteBufferPool();
    }

    @Override
    public String toString() {
        if (strCache == null) {
            strCache = getClass().getName() + "(localAddress: " + getLocalAddress() + ", remoteAddress: " + getRemoteAddress() + ", pipeline: " + pipeline();
        }
        return strCache + ", registered: " + registered + ", attachment: " + attachment + ')';
    }

    private static final class ClosePromise extends DefaultChannelPromise {

        public ClosePromise(AbstractChannel ch) {
            super(ch);
        }

        @Override
        protected EventExecutor executor() {
            return channel().isRegistered() ? channel().eventLoop() : GlobalEventExecutor.INSTANCE;
        }

        @Override
        public ChannelPromise setSuccess() {
            throw new IllegalStateException();
        }

        @Override
        public ChannelPromise setFailure(Throwable cause) {
            throw new IllegalStateException();
        }

        @Override
        public boolean trySuccess() {
            throw new IllegalStateException();
        }

        @Override
        public boolean tryFailure(Throwable cause) {
            throw new IllegalStateException();
        }

        boolean setClosed() {
            return super.trySuccess();
        }
    }

    protected abstract class AbstractUnsafe implements Unsafe {

        private final ClosePromise closePromise = new ClosePromise(AbstractChannel.this);
        private final AtomicBoolean pendingClose = new AtomicBoolean();

        private volatile boolean inFlush;

        private ChannelOutboundBuffer channelOutboundBuffer = newChannelOutboundBuffer();
        private RecvByteBufferAllocator.Handle allocHandle;

        protected ChannelOutboundBuffer newChannelOutboundBuffer() {
            return new ChannelOutboundBuffer(AbstractChannel.this);
        }

        @Override
        public void bind(InetSocketAddress address, ChannelPromise channelPromise) {
            if (!channelPromise.setUncancellable()) {
                return;
            }

            try {
                boolean wasActive = isActive();
                doBind(address, channelPromise);
                safeSetSuccess(channelPromise);
                if (!wasActive && isActive()) {
                    pipeline().fireOpen();
                    if (config().isAutoRead()) {
                        readRequested();
                    }
                }
            } catch (Throwable t) {
                safeSetFailure(channelPromise, t);
                close(voidPromise());
            }
        }

        protected void doBind(InetSocketAddress address, ChannelPromise channelPromise) throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public void disconnect(ChannelPromise channelPromise) {
            if (!channelPromise.setUncancellable()) {
                return;
            }

            try {
                boolean wasActive = isActive();
                doDisconnect(channelPromise);
                safeSetSuccess(channelPromise);
                if (wasActive && !isActive()) {
                    pipeline().fireClose();
                }
            } catch (Throwable t) {
                safeSetFailure(channelPromise, t);
                close(voidPromise());
            }
        }

        protected void doDisconnect(ChannelPromise channelPromise) throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public void read(ChannelPromise channelPromise) {
            if (!pendingClose.get()) {
                if (isActive() && channelPromise.setUncancellable()) {
                    readRequested();
                    safeSetSuccess(channelPromise);
                } else if (isOpen() && !isActive()) {
                    safeSetFailure(channelPromise, NOT_YET_CONNECTED_EXCEPTION);
                }
            } else {
                safeSetFailure(channelPromise, CLOSED_CHANNEL_EXCEPTION);
            }
        }

        protected abstract void readRequested();

        @Override
        public void write(Object message, ChannelPromise channelPromise) {
            try {
                if (!pendingClose.get()) {
                    if (isActive() && channelPromise.setUncancellable()) {
                        channelOutboundBuffer.addMessage(message, channelPromise);
                        if (config().isAutoFlush()) {
                            writeRequested();
                        }
                    } else if (isOpen() && !isActive()) {
                        channelOutboundBuffer.clear(NOT_YET_CONNECTED_EXCEPTION);
                        safeSetFailure(channelPromise, NOT_YET_CONNECTED_EXCEPTION);
                    }
                } else {
                    channelOutboundBuffer.clear(CLOSED_CHANNEL_EXCEPTION);
                    safeSetFailure(channelPromise, CLOSED_CHANNEL_EXCEPTION);
                }
            } catch (Throwable e) {
                channelOutboundBuffer.clear(e);
                safeSetFailure(channelPromise, e);
            }
        }

        protected abstract void writeRequested();

        @Override
        public void register(EventLoop eventScheduler) {
            registered = true;
            AbstractChannel.this.eventLoop = eventScheduler;
            boolean success = false;
            try {
                pipeline.fireRegistered();
                afterRegister();
                success = true;
                if (isActive()) {
                    pipeline.fireOpen();
                    if (config().isAutoRead()) {
                        read(voidPromise());
                    }
                }
            } finally {
                if (!success) {
                    unregister();
                    closeForcibly();
                }
            }
        }

        protected abstract void afterRegister();

        @Override
        public void unregister() {
            if (registered) {
                registered = false;
                pipeline.fireUnregistered();
                afterUnregister();
            }
        }

        protected abstract void afterUnregister();

        @Override
        public void flush() {
            if (inFlush) {
                return;
            }

            inFlush = true;
            if (!isActive()) {
                try {
                    if (isOpen()) {
                        channelOutboundBuffer.clear(NOT_YET_CONNECTED_EXCEPTION);
                    } else {
                        channelOutboundBuffer.clear(CLOSED_CHANNEL_EXCEPTION);
                    }
                } finally {
                    inFlush = false;
                }
                return;
            }

            try {
                if (!channelOutboundBuffer.isEmpty()) {
                    doWrite(channelOutboundBuffer);
                }
            } catch (Throwable e) {
                channelOutboundBuffer.clear(e);
            } finally {
                inFlush = false;
                if (!channelOutboundBuffer.isEmpty()) {
                    writeRequested();
                }
            }
        }

        protected abstract void doWrite(ChannelOutboundBuffer channelOutboundBuffer) throws Exception;

        protected void exceptionCaught(final Throwable e) {
            invokeLater(() -> pipeline().fireExceptionCaught(e));
        }

        @Override
        public ChannelFuture closeFuture() {
            return closePromise;
        }

        @Override
        public void close(ChannelPromise channelPromise) {
            if (!channelPromise.setUncancellable()) {
                return;
            }
            if (pendingClose.compareAndSet(false, true)) {
                close0();
            }

            if (!(channelPromise instanceof VoidChannelPromise)) {
                closePromise.chainPromise(channelPromise);
            }
        }

        private void close0() {
            if (inFlush) {
                invokeLater(this::close0);
                return;
            }

            if (!closePromise.isDone()) {
                final boolean wasActive = isActive();
                pendingClose.set(true);
                closeForcibly();
                channelOutboundBuffer.clear(CLOSED_CHANNEL_EXCEPTION);
                if (wasActive && !isActive()) {
                    closePromise.setClosed();
                    invokeLater(() -> {
                        pipeline.fireClose();
                        unregister();
                    });
                }
            }
        }

        protected void invokeLater(Runnable task) {
            try {
                eventLoop().execute(task);
            } catch (RejectedExecutionException ignore) {
            }
        }

        protected RecvByteBufferAllocator.Handle allocHandle() {
            if (allocHandle == null) {
                allocHandle = config().getRecvByteBufferAllocator().newHandle();
            }
            return allocHandle;
        }
    }
}
