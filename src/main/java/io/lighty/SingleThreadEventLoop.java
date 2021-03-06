package io.lighty;

import io.lighty.concurrent.EventExecutorGroup;
import io.lighty.concurrent.SingleThreadEventExecutor;
import io.lighty.pipeline.DefaultHandlerContextInvoker;
import io.lighty.pipeline.HandlerContextInvoker;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public abstract class SingleThreadEventLoop extends SingleThreadEventExecutor implements EventLoop {

    private final HandlerContextInvoker invoker = new DefaultHandlerContextInvoker(this);

    protected SingleThreadEventLoop(EventExecutorGroup parent, boolean wakeUpForTask) {
        super(parent, wakeUpForTask);
    }

    protected SingleThreadEventLoop(EventExecutorGroup parent, boolean wakeUpForTask, ThreadFactory threadFactory) {
        super(parent, wakeUpForTask, threadFactory);
    }

    protected SingleThreadEventLoop(EventExecutorGroup parent, boolean wakeUpForTask, Executor executor) {
        super(parent, wakeUpForTask, executor);
    }

    @Override
    public HandlerContextInvoker asInvoker() {
        return invoker;
    }

    @Override
    public ChannelFuture register(Channel channel) {
        return register(new DefaultChannelPromise(channel, this));
    }

    @Override
    public ChannelFuture unregister(Channel channel) {
        return unregister(new DefaultChannelPromise(channel, this));
    }

    @Override
    public ChannelFuture register(final ChannelPromise channelPromise) {
        if (channelPromise == null) {
            throw new IllegalArgumentException("channelPromise");
        }
        if (inExecutorThread()) {
            register0(channelPromise);
        } else {
            execute(new Runnable() {
                @Override
                public void run() {
                    register0(channelPromise);
                }
            });
        }
        return channelPromise;
    }

    private void register0(ChannelPromise channelPromise) {
        try {
            channelPromise.channel().unsafe().register(SingleThreadEventLoop.this);
            channelPromise.setSuccess();
        } catch (ChannelException e) {
            channelPromise.setFailure(e);
        }
    }

    @Override
    public ChannelFuture unregister(final ChannelPromise channelPromise) {
        if (channelPromise == null) {
            throw new IllegalArgumentException("channelPromise");
        }
        if (inExecutorThread()) {
            unregister0(channelPromise);
        } else {
            execute(() -> unregister0(channelPromise));
        }
        return channelPromise;
    }

    private void unregister0(ChannelPromise channelPromise) {
        try {
            channelPromise.channel().unsafe().unregister();
            channelPromise.setSuccess();
        } catch (ChannelException e) {
            channelPromise.setFailure(e);
        }
    }

    @Override
    public EventLoopGroup parent() {
        return (EventLoopGroup) super.parent();
    }

    @Override
    protected Queue<Runnable> newTaskQueue() {
        return new ConcurrentLinkedQueue<>();
    }
}
