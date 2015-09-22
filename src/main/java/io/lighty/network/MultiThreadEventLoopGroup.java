package io.lighty.network;

import io.lighty.network.concurrent.MultiThreadEventExecutorGroup;
import io.lighty.network.pipeline.HandlerContextInvoker;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public abstract class MultiThreadEventLoopGroup extends MultiThreadEventExecutorGroup implements EventLoopGroup {

    private static final int DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    protected MultiThreadEventLoopGroup(ThreadFactory threadFactory, Object... args) {
        super(DEFAULT_NUM_THREADS, threadFactory, args);
    }

    protected MultiThreadEventLoopGroup(Executor executor, Object... args) {
        super(DEFAULT_NUM_THREADS, executor, args);
    }

    protected MultiThreadEventLoopGroup(int nThreads, ThreadFactory threadFactory, Object... args) {
        super(nThreads > 0 ? nThreads : DEFAULT_NUM_THREADS, threadFactory, args);
    }

    protected MultiThreadEventLoopGroup(int nThreads, Executor executor, Object... args) {
        super(nThreads > 0 ? nThreads : DEFAULT_NUM_THREADS, executor, args);
    }

    @Override
    public EventLoopGroup parent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventLoop next() {
        return (EventLoop) super.next();
    }

    @Override
    protected abstract EventLoop newEventExecutor(Executor executor, Object... args);

    @Override
    public ChannelFuture register(Channel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("channel");
        }
        return next().register(channel);
    }

    @Override
    public ChannelFuture register(ChannelPromise channelPromise) {
        if (channelPromise == null) {
            throw new IllegalArgumentException("channelPromise");
        }
        return next().register(channelPromise);
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
    public HandlerContextInvoker asInvoker() {
        throw new UnsupportedOperationException();
    }
}
