package io.lighty.network.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface Future<V> extends java.util.concurrent.Future<V> {

    boolean isUncancellable();

    boolean isSuccess();

    V getNow();

    Throwable getCause();

    Future<V> await() throws InterruptedException;

    boolean await(long timeout, TimeUnit unit) throws InterruptedException;

    boolean await(long timeoutMillis) throws InterruptedException;

    V get(long timeoutMillis) throws InterruptedException, ExecutionException, TimeoutException;

    Future<V> addListener(FutureListener<? extends Future<? super V>> futureListener);

    Future<V> addListeners(io.lighty.network.concurrent.FutureListener<? extends Future<? super V>>... futureListeners);

    Future<V> removeListener(io.lighty.network.concurrent.FutureListener<? extends Future<? super V>> futureListener);

    Future<V> removeListeners(io.lighty.network.concurrent.FutureListener<? extends Future<? super V>>... futureListeners);

    Future<V> sync() throws InterruptedException;

    Future<V> sync(long timeout, TimeUnit unit) throws InterruptedException;

    Future<V> sync(long timeoutMillis) throws InterruptedException;

    boolean cancel();

    @Override
    boolean cancel(boolean mayInterruptIfRunning);
}
