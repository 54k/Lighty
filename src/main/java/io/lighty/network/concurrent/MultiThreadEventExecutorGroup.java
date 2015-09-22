package io.lighty.network.concurrent;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class MultiThreadEventExecutorGroup extends AbstractEventExecutorGroup {

    private final AtomicInteger childIndex = new AtomicInteger();
    private final EventExecutor[] children;
    private final ExecutorChooser chooser;
    private final Set<EventExecutor> readonlyChildren;
    private final FutureGroup<Void> shutdownFutureGroup;

    protected MultiThreadEventExecutorGroup(int nThreads, ThreadFactory threadFactory, Object... args) {
        this(nThreads, threadFactory == null ? null : new ThreadPerTaskExecutor(threadFactory), args);
    }

    protected MultiThreadEventExecutorGroup(int nThreads, Executor executor, Object... args) {
        if (nThreads < 1) {
            throw new IllegalArgumentException("nThreads > 1");
        }

        if (executor == null) {
            executor = new ThreadPerTaskExecutor();
        }

        children = new EventExecutor[nThreads];
        if (isPowerOfTwo(nThreads)) {
            chooser = new PowerOfTwoExecutorChooser();
        } else {
            chooser = new GenericExecutorChooser();
        }

        for (int i = 0; i < children.length; i++) {
            children[i] = newEventExecutor(executor, args);
        }

        Set<EventExecutor> readonlyChildren = new LinkedHashSet<>(children.length);
        Collections.addAll(readonlyChildren, children);
        this.readonlyChildren = Collections.unmodifiableSet(readonlyChildren);

        Set<Future<Void>> shutdownFutures = readonlyChildren.stream().map(EventExecutor::terminationFuture).collect(Collectors.toSet());
        shutdownFutureGroup = new DefaultFutureGroup<>(shutdownFutures);
    }

    private static boolean isPowerOfTwo(int val) {
        return (val & -val) == val;
    }

    protected abstract EventExecutor newEventExecutor(Executor executor, Object... args);

    @Override
    public boolean isShuttingDown() {
        return Arrays.stream(children).anyMatch(EventExecutor::isShuttingDown);
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
    public boolean inExecutorThread(Thread thread) {
        return Arrays.stream(children).anyMatch(c -> c.inExecutorThread(thread));
    }

    @Override
    public boolean inExecutorThread() {
        return Arrays.stream(children).anyMatch(EventExecutor::inExecutorThread);
    }

    @Override
    public void shutdown() {
        for (EventExecutor c : children) {
            c.shutdown();
        }
    }

    @Override
    public FutureGroup<Void> shutdownGracefully() {
        for (EventExecutor c : children) {
            c.shutdownGracefully();
        }
        return shutdownFutureGroup;
    }

    @Override
    public FutureGroup<Void> terminationFuture() {
        return shutdownFutureGroup;
    }

    @Override
    public EventExecutor next() {
        return chooser.next();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends EventExecutor> Set<E> children() {
        return (Set<E>) readonlyChildren;
    }

    @Override
    public boolean isShutdown() {
        return Arrays.stream(children).allMatch(EventExecutor::isShutdown);
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    private interface ExecutorChooser {
        EventExecutor next();
    }

    private final class PowerOfTwoExecutorChooser implements ExecutorChooser {
        @Override
        public EventExecutor next() {
            return children[childIndex.getAndIncrement() & children.length - 1];
        }
    }

    private final class GenericExecutorChooser implements ExecutorChooser {
        @Override
        public EventExecutor next() {
            return children[childIndex.getAndIncrement() % children.length];
        }
    }
}
