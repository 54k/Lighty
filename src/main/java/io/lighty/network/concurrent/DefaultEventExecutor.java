package io.lighty.network.concurrent;

import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public final class DefaultEventExecutor extends SingleThreadEventExecutor {

    public DefaultEventExecutor() {
        this(null);
    }

    public DefaultEventExecutor(EventExecutorGroup parent) {
        super(parent, true);
    }

    public DefaultEventExecutor(EventExecutorGroup parent, Executor executor) {
        super(parent, true, executor);
    }

    @Override
    protected Queue<Runnable> newTaskQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Override
    protected void run() {
        for (; ; ) {
            Runnable task = takeTask();
            if (task != null) {
                try {
                    task.run();
                } catch (Throwable ignore) {
                }
            }

            if (isShuttingDown()) {
                confirmShutdown();
                break;
            }
        }
    }
}
