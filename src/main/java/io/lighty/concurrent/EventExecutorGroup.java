package io.lighty.concurrent;

import java.util.Set;

public interface EventExecutorGroup extends EventExecutor {

    EventExecutor next();

    <E extends EventExecutor> Set<E> children();
}
