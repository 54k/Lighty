package io.lighty.concurrent;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AbstractEventExecutorGroup implements EventExecutorGroup {

    @Override
    public boolean inExecutorThread(Thread thread) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean inExecutorThread() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventExecutorGroup parent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> Promise<V> newPromise() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return next().submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return next().submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return next().submit(task);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return next().schedule(command, delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return next().schedule(callable, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return next().scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return next().scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return next().invokeAll(tasks);
    }

    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return next().invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return next().invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return next().invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        next().execute(command);
    }

    @Override
    @Deprecated
    public abstract void shutdown();

    @Override
    @Deprecated
    public List<Runnable> shutdownNow() {
        shutdown();
        return Collections.emptyList();
    }
}
