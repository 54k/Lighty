package io.lighty.network;

import io.lighty.network.pipeline.HandlerContext;

/**
 * Every {@link io.lighty.network.Channel} has own instance of {@link io.lighty.network.pipeline.Pipeline} which intercepts events occurred on session. <br/>
 * Each {@link io.lighty.network.Handler} processed in {@link io.lighty.network.Handler}'s thread. <br/>
 * Inbound events processed from first to last filter. Outbound events processed from last to first.
 *
 * @param <I> Type of inbound high-level message. First filter in {@link io.lighty.network.pipeline.Pipeline} receives array of bytes.
 * @param <O> Type of outbound high-level message. Last filter in {@link io.lighty.network.pipeline.Pipeline} should return array of bytes.
 */
public interface Handler<I, O> {

    /**
     * Called when {@link io.lighty.network.Handler} is added to {@link io.lighty.network.pipeline.Pipeline}
     *
     * @param context {@link io.lighty.network.pipeline.HandlerContext}
     */
    void onHandlerAdded(HandlerContext context);

    /**
     * Called when {@link io.lighty.network.Handler} is removed from {@link io.lighty.network.pipeline.Pipeline}
     *
     * @param context {@link io.lighty.network.pipeline.HandlerContext}
     */
    void onHandlerRemoved(HandlerContext context);

    /**
     * Called when {@link io.lighty.network.Channel} is registered by {@link io.lighty.network.nio.NioEventLoop}
     *
     * @param context {@link io.lighty.network.pipeline.HandlerContext}
     */
    void onRegistered(HandlerContext context);

    /**
     * Called when {@link io.lighty.network.Channel} is unregistered by {@link io.lighty.network.nio.NioEventLoop}
     *
     * @param context {@link io.lighty.network.pipeline.HandlerContext}
     */
    void onUnregistered(HandlerContext context);

    /**
     * Called when {@link io.lighty.network.Channel} is fully established
     *
     * @param context {@link io.lighty.network.pipeline.HandlerContext}
     */
    void onOpen(HandlerContext context);

    /**
     * Called when read operation requested on {@link io.lighty.network.Channel}
     *
     * @param context {@link io.lighty.network.pipeline.HandlerContext}
     */
    void onRead(HandlerContext context, ChannelPromise channelPromise);

    /**
     * Called when inbound message received
     *
     * @param context {@link io.lighty.network.pipeline.HandlerContext}
     * @param message high level message
     */
    void onMessageReceived(HandlerContext context, I message);

    /**
     * Called when outbound message sent
     *
     * @param context {@link io.lighty.network.pipeline.HandlerContext}
     * @param message high level message
     */
    void onMessageSent(HandlerContext context, O message, ChannelPromise channelPromise);

    /**
     * Called when {@link io.lighty.network.Channel} is requested to close
     *
     * @param context {@link io.lighty.network.pipeline.HandlerContext}
     */
    void onClosing(HandlerContext context, ChannelPromise channelPromise);

    /**
     * Called when {@link io.lighty.network.Channel} is fully closed
     *
     * @param context {@link io.lighty.network.pipeline.HandlerContext}
     */
    void onClose(HandlerContext context);

    /**
     * Called when {@link io.lighty.network.Channel} is requested to disconnect
     *
     * @param context {@link io.lighty.network.pipeline.HandlerContext}
     */
    void onDisconnect(HandlerContext context, ChannelPromise channelPromise);

    /**
     * Called when error occurs
     *
     * @param context {@link io.lighty.network.pipeline.HandlerContext}
     */
    void onExceptionCaught(HandlerContext context, Throwable e);
}
