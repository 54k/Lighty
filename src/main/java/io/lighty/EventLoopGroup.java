package io.lighty;

import io.lighty.concurrent.EventExecutorGroup;

public interface EventLoopGroup extends EventExecutorGroup, EventLoop {

    @Override
    EventLoop next();
}
