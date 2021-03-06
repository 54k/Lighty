package io.lighty;

import io.lighty.concurrent.DefaultPromise;
import io.lighty.concurrent.EventExecutor;
import io.lighty.concurrent.Future;
import io.lighty.concurrent.FutureListener;
import io.lighty.concurrent.Promise;

import java.util.concurrent.TimeUnit;

public class DefaultChannelPromise extends DefaultPromise<Void> implements ChannelPromise {

    private final Channel channel;

    public DefaultChannelPromise(Channel channel) {
        this(channel, null);
    }

    public DefaultChannelPromise(Channel channel, EventExecutor eventExecutor) {
        super(eventExecutor);
        this.channel = channel;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    protected EventExecutor executor() {
        EventExecutor eventExecutor = super.executor();
        if (eventExecutor == null) {
            return channel.eventLoop();
        }
        return eventExecutor;
    }

    @Override
    public ChannelPromise setSuccess() {
        return setSuccess(null);
    }

    @Override
    public ChannelPromise setSuccess(Void result) {
        super.setSuccess(result);
        return this;
    }

    @Override
    public boolean trySuccess() {
        return trySuccess(null);
    }

    @Override
    public ChannelPromise setFailure(Throwable cause) {
        super.setFailure(cause);
        return this;
    }

    @Override
    public ChannelPromise chainPromise(Promise<Void> promise) {
        super.chainPromise(promise);
        return this;
    }

    @Override
    public ChannelPromise chainPromises(Promise<Void>... promises) {
        super.chainPromises(promises);
        return this;
    }

    @Override
    public ChannelFuture addListener(FutureListener<? extends Future<? super Void>> futureListener) {
        super.addListener(futureListener);
        return this;
    }

    @Override
    public ChannelFuture addListeners(FutureListener<? extends Future<? super Void>>... futureListeners) {
        super.addListeners(futureListeners);
        return this;
    }

    @Override
    public ChannelFuture removeListener(FutureListener<? extends Future<? super Void>> futureListener) {
        super.removeListener(futureListener);
        return this;
    }

    @Override
    public ChannelFuture removeListeners(FutureListener<? extends Future<? super Void>>... futureListeners) {
        super.removeListeners(futureListeners);
        return this;
    }

    @Override
    public ChannelFuture sync() throws InterruptedException {
        super.sync();
        return this;
    }

    @Override
    public ChannelFuture sync(long timeout, TimeUnit unit) throws InterruptedException {
        super.sync(timeout, unit);
        return this;
    }

    @Override
    public ChannelFuture sync(long timeoutMillis) throws InterruptedException {
        super.sync(timeoutMillis);
        return this;
    }

    @Override
    public ChannelFuture await() throws InterruptedException {
        super.await();
        return this;
    }
}
