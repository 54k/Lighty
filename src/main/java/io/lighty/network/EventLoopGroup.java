package io.lighty.network;

import io.lighty.network.concurrent.EventExecutorGroup;

public interface EventLoopGroup extends EventExecutorGroup, EventLoop {

    @Override
    EventLoop next();
}
