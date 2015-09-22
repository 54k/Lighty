package io.lighty;

import io.lighty.concurrent.Future;
import io.lighty.concurrent.FutureListener;

import java.util.concurrent.TimeUnit;

public interface ChannelFuture extends Future<Void> {

    Channel channel();

    @Override
    ChannelFuture addListener(FutureListener<? extends Future<? super Void>> futureListener);

    @Override
    ChannelFuture addListeners(FutureListener<? extends Future<? super Void>>... futureListeners);

    @Override
    ChannelFuture removeListener(FutureListener<? extends Future<? super Void>> futureListener);

    @Override
    ChannelFuture removeListeners(FutureListener<? extends Future<? super Void>>... futureListeners);

    @Override
    ChannelFuture await() throws InterruptedException;

    @Override
    ChannelFuture sync() throws InterruptedException;

    @Override
    ChannelFuture sync(long timeout, TimeUnit unit) throws InterruptedException;

    @Override
    ChannelFuture sync(long timeoutMillis) throws InterruptedException;
}
