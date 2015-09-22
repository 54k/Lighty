package io.lighty.network;

import io.lighty.network.concurrent.AbstractEventExecutorGroup;
import io.lighty.network.concurrent.DefaultPromise;
import io.lighty.network.concurrent.EventExecutor;
import io.lighty.network.concurrent.Future;
import io.lighty.network.concurrent.FutureListener;
import io.lighty.network.concurrent.GlobalEventExecutor;
import io.lighty.network.concurrent.ThreadPerTaskExecutor;
import io.lighty.network.pipeline.HandlerContextInvoker;

import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public abstract class ThreadPerChannelEventLoopGroup extends AbstractEventExecutorGroup implements EventLoopGroup {

    final Set<EventLoop> activeChildren = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<EventLoop> readOnlyActiveChildren = Collections.unmodifiableSet(activeChildren);
    final Queue<EventLoop> idleChildren = new ConcurrentLinkedQueue<>();
    final Executor executor;

    private final ChannelException tooManyChannels;
    private final int maxChannels;
    private final DefaultPromise<Void> shutdownFuture = new DefaultPromise<>();
    private final FutureListener<Future<Void>> shutdownListener = future -> {
        if (isTerminated()) {
            shutdownFuture.trySuccess(null);
        }
    };
    private volatile boolean shuttingDown;

    protected ThreadPerChannelEventLoopGroup() {
        this(0);
    }

    protected ThreadPerChannelEventLoopGroup(int maxChannels) {
        this(maxChannels, Executors.defaultThreadFactory());
    }

    protected ThreadPerChannelEventLoopGroup(int maxChannels, ThreadFactory threadFactory) {
        this(maxChannels, new ThreadPerTaskExecutor(threadFactory));
    }

    protected ThreadPerChannelEventLoopGroup(int maxChannels, Executor executor) {
        if (maxChannels < 0) {
            throw new IllegalArgumentException(String.format("maxChannels: %d (expected: >= 0)", maxChannels));
        }
        if (executor == null) {
            throw new NullPointerException("executor");
        }

        this.maxChannels = maxChannels;
        this.executor = executor;

        tooManyChannels = new ChannelException("too many channels (max: " + maxChannels + ')');
        tooManyChannels.setStackTrace(new StackTraceElement[0]);
    }

    @Override
    public boolean isShuttingDown() {
        return shuttingDown;
    }

    @Deprecated
    @Override
    public void shutdown() {
        shuttingDown = true;

        for (EventLoop l : activeChildren) {
            l.shutdown();
        }

        for (EventLoop l : idleChildren) {
            l.shutdown();
        }
    }

    @Override
    public EventLoop next() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture register(Channel channel) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        try {
            return nextChild().register(channel);
        } catch (Throwable t) {
            ChannelPromise promise = new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE);
            promise.setFailure(t);
            return promise;
        }
    }

    @Override
    public ChannelFuture register(ChannelPromise channelPromise) {
        if (channelPromise == null) {
            throw new NullPointerException("channelPromise");
        }
        try {
            return nextChild().register(channelPromise);
        } catch (Throwable t) {
            channelPromise.setFailure(t);
            return channelPromise;
        }
    }

    @Override
    public ChannelFuture unregister(ChannelPromise channelPromise) {
        return channelPromise.channel().eventLoop().unregister(channelPromise);
    }

    @Override
    public ChannelFuture unregister(Channel channel) {
        return channel.unregister();
    }

    @Override
    public EventLoopGroup parent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HandlerContextInvoker asInvoker() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends EventExecutor> Set<E> children() {
        return (Set<E>) readOnlyActiveChildren;
    }

    @Override
    public Future<Void> shutdownGracefully() {
        shuttingDown = true;

        for (EventLoop l : activeChildren) {
            l.shutdownGracefully();
        }
        for (EventLoop l : idleChildren) {
            l.shutdownGracefully();
        }

        return shutdownFuture;
    }

    @Override
    public Future<Void> terminationFuture() {
        return shutdownFuture;
    }

    @Override
    public boolean isShutdown() {
        for (EventLoop l : activeChildren) {
            if (!l.isShutdown()) {
                return false;
            }
        }
        for (EventLoop l : idleChildren) {
            if (!l.isShutdown()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isTerminated() {
        for (EventLoop l : activeChildren) {
            if (!l.isTerminated()) {
                return false;
            }
        }
        for (EventLoop l : idleChildren) {
            if (!l.isTerminated()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return shutdownGracefully().await(timeout, unit);
    }

    protected EventLoop newChild() throws Exception {
        return new ThreadPerChannelEventLoop(this);
    }

    private EventLoop nextChild() throws Exception {
        if (shuttingDown) {
            throw new RejectedExecutionException("shutting down");
        }

        EventLoop loop = idleChildren.poll();
        if (loop == null) {
            if (maxChannels > 0 && activeChildren.size() >= maxChannels) {
                throw tooManyChannels;
            }
            loop = newChild();
            loop.terminationFuture().addListener(shutdownListener);
        }
        activeChildren.add(loop);
        return loop;
    }
}
