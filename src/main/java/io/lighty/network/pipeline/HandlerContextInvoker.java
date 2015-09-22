package io.lighty.network.pipeline;

import io.lighty.network.ChannelPromise;

public interface HandlerContextInvoker {

    void invokeOnRegistered(HandlerContext context);

    void invokeOnUnregistered(HandlerContext context);

    void invokeOnOpen(HandlerContext context);

    void invokeOnRead(HandlerContext context, ChannelPromise channelPromise);

    void invokeOnMessageReceived(HandlerContext context, Object message);

    void invokeOnMessageSent(HandlerContext context, Object message, ChannelPromise channelPromise);

    void invokeOnClosing(HandlerContext context, ChannelPromise channelPromise);

    void invokeOnClosed(HandlerContext context);

    void invokeOnDisconnect(HandlerContext context, ChannelPromise channelPromise);

    void invokeOnExceptionCaught(HandlerContext context, Throwable e);
}
