package io.lighty.network;

import io.lighty.network.concurrent.EventExecutor;
import io.lighty.network.pipeline.HandlerContextInvoker;

public interface EventLoop extends EventExecutor {

    HandlerContextInvoker asInvoker();

    @Override
    EventLoopGroup parent();

    ChannelFuture register(Channel channel);

    ChannelFuture register(ChannelPromise channelPromise);

    ChannelFuture unregister(Channel channel);

    ChannelFuture unregister(ChannelPromise channelPromise);
}
