package io.lighty.network.concurrent;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractEventExecutor extends AbstractExecutorService implements EventExecutor {

    public static final long PURGE_TASK_INTERVAL = TimeUnit.SECONDS.toNanos(5);

    private final EventExecutorGroup parent;
    private final Queue<ScheduledTask<?>> delayedTaskQueue = new PriorityQueue<>();

    protected AbstractEventExecutor() {
        this(null);
    }

    protected AbstractEventExecutor(EventExecutorGroup parent) {
        this.parent = parent;
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new PromiseTask<>(this, runnable, value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new PromiseTask<>(this, callable);
    }

    @Override
    public <V> Promise<V> newPromise() {
        return new DefaultPromise<>(this);
    }

    @Override
    public boolean inExecutorThread() {
        return inExecutorThread(Thread.currentThread());
    }

    @Override
    public EventExecutorGroup parent() {
        return parent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Future<Void> submit(Runnable task) {
        return (Future<Void>) super.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return (Future<T>) super.submit(task, result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return (Future<T>) super.submit(task);
    }

    protected ScheduledTask<?> fetchDelayedTask() {
        ScheduledTask<?> delayedTask = delayedTaskQueue.peek();
        if (delayedTask == null) {
            return null;
        }
        long nanoTime = ScheduledTask.nanos();
        if (delayedTask.triggerTime() <= nanoTime) {
            delayedTaskQueue.remove();
            return delayedTask;
        }
        return null;
    }

    protected ScheduledTask<?> peekDelayedTask() {
        return delayedTaskQueue.peek();
    }

    protected void cancelDelayedTasks() {
        for (ScheduledTask<?> scheduledTask : delayedTaskQueue) {
            scheduledTask.cancel();
        }
        delayedTaskQueue.clear();
    }

    protected int pendingTasks() {
        return delayedTaskQueue.size();
    }

    protected void schedulePurgeTask() {
        delayedTaskQueue.add(newPurgeTask());
    }

    private ScheduledTask<?> newPurgeTask() {
        return new ScheduledTask<>(this, PromiseTask.toCallable(new PurgeTask()), ScheduledTask.triggerTime(PURGE_TASK_INTERVAL),
                -PURGE_TASK_INTERVAL, delayedTaskQueue);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return schedule(new ScheduledTask<>(this, PromiseTask.toCallable(command), ScheduledTask.triggerTime(unit.toNanos(initialDelay)), -unit.toNanos(delay),
                delayedTaskQueue));
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return schedule(new ScheduledTask<>(this, PromiseTask.toCallable(command), ScheduledTask.triggerTime(unit.toNanos(initialDelay)), unit.toNanos(period),
                delayedTaskQueue));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return (ScheduledFuture<V>) schedule(new ScheduledTask(this, callable, ScheduledTask.triggerTime(unit.toNanos(delay)), delayedTaskQueue));
    }

    @Override
    @SuppressWarnings("unchecked")
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return schedule(new ScheduledTask(this, PromiseTask.toCallable(command), ScheduledTask.triggerTime(unit.toNanos(delay)), delayedTaskQueue));
    }

    private ScheduledFuture<?> schedule(ScheduledTask<?> task) {
        if (task == null) {
            throw new IllegalArgumentException("task");
        }

        if (inExecutorThread()) {
            delayedTaskQueue.add(task);
        } else {
            execute(() -> delayedTaskQueue.add(task));
        }

        return task;
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return Collections.emptyList();
    }

    private final class PurgeTask implements Runnable {
        @Override
        public void run() {
            Iterator<ScheduledTask<?>> i = delayedTaskQueue.iterator();
            while (i.hasNext()) {
                ScheduledTask<?> task = i.next();
                if (task.isCancelled()) {
                    i.remove();
                }
            }
        }
    }
}
