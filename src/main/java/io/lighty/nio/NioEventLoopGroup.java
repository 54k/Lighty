package io.lighty.nio;

import io.lighty.concurrent.DefaultThreadFactory;
import io.lighty.concurrent.EventExecutor;
import io.lighty.EventLoop;
import io.lighty.MultiThreadEventLoopGroup;

import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public final class NioEventLoopGroup extends MultiThreadEventLoopGroup {

    public NioEventLoopGroup() {
        this(0, new DefaultThreadFactory("network-nio-eventloop", Thread.MAX_PRIORITY));
    }

    public NioEventLoopGroup(int nThreads) {
        this(nThreads, new DefaultThreadFactory("network-nio-eventloop", Thread.MAX_PRIORITY));
    }

    public NioEventLoopGroup(int nThreads, Executor executor) {
        this(nThreads, executor, SelectorProvider.provider());
    }

    public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        this(nThreads, threadFactory, SelectorProvider.provider());
    }

    public NioEventLoopGroup(int nThreads, Executor executor, SelectorProvider selectorProvider) {
        super(nThreads, executor, selectorProvider);
    }

    public NioEventLoopGroup(int nThreads, ThreadFactory threadFactory, SelectorProvider selectorProvider) {
        super(nThreads, threadFactory, selectorProvider);
    }

    @Override
    protected EventLoop newEventExecutor(Executor executor, Object... args) {
        return new NioEventLoop(this, (SelectorProvider) args[0], executor);
    }

    public void setIoRatio(int ioRatio) {
        for (EventExecutor e : children()) {
            ((NioEventLoop) e).setIoRatio(ioRatio);
        }
    }
}
