package io.lighty;

import io.lighty.concurrent.FutureListener;

public interface ChannelFutureListener extends FutureListener<ChannelFuture> {

    static ChannelFutureListener CLOSE_LISTENER = new ChannelFutureListener() {
        @Override
        public void onComplete(ChannelFuture future) {
            future.channel().close();
        }
    };
}
