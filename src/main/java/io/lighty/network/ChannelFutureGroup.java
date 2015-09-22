package io.lighty.network;

import io.lighty.network.concurrent.Future;
import io.lighty.network.concurrent.FutureListener;

import java.util.concurrent.TimeUnit;

public interface ChannelFutureGroup extends Future<Void>, Iterable<ChannelFuture> {

    @Override
    ChannelFutureGroup addListener(FutureListener<? extends Future<? super Void>> futureListener);

    @Override
    ChannelFutureGroup addListeners(FutureListener<? extends Future<? super Void>>... futureListeners);

    @Override
    ChannelFutureGroup removeListener(FutureListener<? extends Future<? super Void>> futureListener);

    @Override
    ChannelFutureGroup removeListeners(FutureListener<? extends Future<? super Void>>... futureListeners);

    @Override
    ChannelFutureGroup await() throws InterruptedException;

    @Override
    ChannelFutureGroup sync() throws InterruptedException;

    @Override
    ChannelFutureGroup sync(long timeout, TimeUnit unit) throws InterruptedException;

    @Override
    ChannelFutureGroup sync(long timeoutMillis) throws InterruptedException;
}
