package io.lighty.network.group;

import io.lighty.network.concurrent.EventExecutor;
import io.lighty.network.concurrent.Future;
import io.lighty.network.concurrent.FutureListener;
import io.lighty.network.ChannelFuture;
import io.lighty.network.DefaultChannelFutureGroup;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

final class DefaultChannelGroupFuture extends DefaultChannelFutureGroup implements ChannelGroupFuture {

    private final ChannelGroup group;

    public DefaultChannelGroupFuture(ChannelGroup group, Collection<ChannelFuture> channelFutures) {
        this(null, group, channelFutures);
    }

    public DefaultChannelGroupFuture(EventExecutor eventExecutor, ChannelGroup group, Collection<ChannelFuture> futures) {
        super(eventExecutor, futures);
        if (group == null) {
            throw new IllegalArgumentException("group");
        }

        this.group = group;
    }

    @Override
    public ChannelGroup group() {
        return group;
    }

    @Override
    public ChannelGroupFuture addListener(FutureListener<? extends Future<? super Void>> futureListener) {
        super.addListener(futureListener);
        return this;
    }

    @Override
    public ChannelGroupFuture addListeners(FutureListener<? extends Future<? super Void>>... futureListeners) {
        super.addListeners(futureListeners);
        return this;
    }

    @Override
    public ChannelGroupFuture removeListener(FutureListener<? extends Future<? super Void>> futureListener) {
        super.removeListener(futureListener);
        return this;
    }

    @Override
    public ChannelGroupFuture removeListeners(FutureListener<? extends Future<? super Void>>... futureListeners) {
        super.removeListeners(futureListeners);
        return this;
    }

    @Override
    public ChannelGroupFuture await() throws InterruptedException {
        super.await();
        return this;
    }

    @Override
    public ChannelGroupFuture sync() throws InterruptedException {
        super.sync();
        return this;
    }

    @Override
    public ChannelGroupFuture sync(long timeout, TimeUnit unit) throws InterruptedException {
        super.sync(timeout, unit);
        return this;
    }

    @Override
    public ChannelGroupFuture sync(long timeoutMillis) throws InterruptedException {
        super.sync(timeoutMillis);
        return this;
    }
}
