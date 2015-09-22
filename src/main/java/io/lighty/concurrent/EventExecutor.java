package io.lighty.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public interface EventExecutor extends ScheduledExecutorService {

    boolean inExecutorThread();

    boolean inExecutorThread(Thread thread);

    <V> Promise<V> newPromise();

    EventExecutorGroup parent();

    boolean isShuttingDown();

    Future<Void> shutdownGracefully();

    Future<Void> terminationFuture();

    @Override
    <T> Future<T> submit(Callable<T> task);

    @Override
    <T> Future<T> submit(Runnable task, T result);

    @Override
    Future<?> submit(Runnable task);

    @Override
    ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);

    @Override
    <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit);

    @Override
    ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);

    @Override
    ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);
}
