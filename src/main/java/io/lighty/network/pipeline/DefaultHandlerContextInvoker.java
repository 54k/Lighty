package io.lighty.network.pipeline;

import io.lighty.network.concurrent.EventExecutor;
import io.lighty.network.ChannelPromise;

import static io.lighty.network.pipeline.HandlerContextInvokerUtils.*;

public class DefaultHandlerContextInvoker implements HandlerContextInvoker {

    private EventExecutor executor;

    public DefaultHandlerContextInvoker(EventExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void invokeOnRegistered(final HandlerContext context) {
        if (executor.inExecutorThread()) {
            invokeOnRegisteredNow(context);
        } else {
            AbstractHandlerContext dctx = (AbstractHandlerContext) context;
            Runnable event = dctx.registeredEvent;
            if (event == null) {
                dctx.registeredEvent = event = () -> invokeOnRegisteredNow(context);
            }
            executor.execute(event);
        }
    }

    @Override
    public void invokeOnUnregistered(final HandlerContext context) {
        if (executor.inExecutorThread()) {
            invokeOnUnregisteredNow(context);
        } else {
            AbstractHandlerContext dctx = (AbstractHandlerContext) context;
            Runnable event = dctx.unregisteredEvent;
            if (event == null) {
                dctx.unregisteredEvent = event = () -> invokeOnUnregisteredNow(context);
            }
            executor.execute(event);
        }
    }

    @Override
    public void invokeOnOpen(final HandlerContext context) {
        if (executor.inExecutorThread()) {
            invokeOnOpenNow(context);
        } else {
            AbstractHandlerContext dctx = (AbstractHandlerContext) context;
            Runnable event = dctx.openEvent;
            if (event == null) {
                dctx.openEvent = event = () -> invokeOnOpenNow(context);
            }
            executor.execute(event);
        }
    }

    @Override
    public void invokeOnRead(final HandlerContext context, final ChannelPromise channelPromise) {
        if (executor.inExecutorThread()) {
            invokeOnReadNow(context, channelPromise);
        } else {
            AbstractHandlerContext dctx = (AbstractHandlerContext) context;
            Runnable event = dctx.readEvent;
            if (event == null) {
                dctx.readEvent = event = () -> invokeOnReadNow(context, channelPromise);
            }
            executor.execute(event);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void invokeOnMessageReceived(final HandlerContext context, final Object message) {
        if (executor.inExecutorThread()) {
            invokeOnMessageReceivedNow(context, message);
        } else {
            executor.execute(() -> invokeOnMessageReceivedNow(context, message));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void invokeOnMessageSent(final HandlerContext context, final Object message, final ChannelPromise channelPromise) {
        if (executor.inExecutorThread()) {
            invokeOnMessageSentNow(context, message, channelPromise);
        } else {
            executor.execute(() -> invokeOnMessageSentNow(context, message, channelPromise));
        }
    }

    @Override
    public void invokeOnClosing(final HandlerContext context, final ChannelPromise channelPromise) {
        if (executor.inExecutorThread()) {
            invokeOnClosingNow(context, channelPromise);
        } else {
            executor.execute(() -> invokeOnClosingNow(context, channelPromise));
        }
    }

    @Override
    public void invokeOnClosed(final HandlerContext context) {
        if (executor.inExecutorThread()) {
            invokeOnCloseNow(context);
        } else {
            AbstractHandlerContext dctx = (AbstractHandlerContext) context;
            Runnable event = dctx.closeEvent;
            if (event == null) {
                dctx.closeEvent = event = () -> invokeOnCloseNow(context);
            }
            executor.execute(event);
        }
    }

    @Override
    public void invokeOnDisconnect(final HandlerContext context, final ChannelPromise channelPromise) {
        if (executor.inExecutorThread()) {
            invokeOnDisconnectNow(context, channelPromise);
        } else {
            AbstractHandlerContext dctx = (AbstractHandlerContext) context;
            Runnable event = dctx.disconnectEvent;
            if (event == null) {
                dctx.disconnectEvent = event = () -> invokeOnDisconnectNow(context, channelPromise);
            }
            executor.execute(event);
        }
    }

    @Override
    public void invokeOnExceptionCaught(final HandlerContext context, final Throwable e) {
        if (executor.inExecutorThread()) {
            invokeOnExceptionCaughtNow(context, e);
        } else {
            executor.execute(() -> invokeOnExceptionCaughtNow(context, e));
        }
    }
}
