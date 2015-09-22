package io.lighty.network.pipeline;

import io.lighty.network.Channel;
import io.lighty.network.ChannelFuture;
import io.lighty.network.ChannelPromise;
import io.lighty.network.Handler;

public interface HandlerContext {

    String name();

    Handler handler();

    Channel channel();

    void fireRegistered();

    void fireUnregistered();

    void fireOpen();

    void fireMessageReceived(Object message);

    void fireClose();

    void fireExceptionCaught(Throwable e);

    ChannelFuture read();

    ChannelFuture read(ChannelPromise channelPromise);

    ChannelFuture write(Object message);

    ChannelFuture write(Object message, ChannelPromise channelPromise);

    ChannelFuture close();

    ChannelFuture close(ChannelPromise channelPromise);

    ChannelFuture disconnect();

    ChannelFuture disconnect(ChannelPromise channelPromise);

    boolean isRemoved();
}
