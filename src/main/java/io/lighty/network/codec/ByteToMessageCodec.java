package io.lighty.network.codec;

import io.lighty.network.AbstractHandler;
import io.lighty.network.ChannelPromise;
import io.lighty.network.buffer.DynamicByteBuffer;
import io.lighty.network.pipeline.HandlerContext;

import java.nio.ByteBuffer;
import java.util.List;

public abstract class ByteToMessageCodec<O> extends AbstractHandler<byte[], O> {

    private final Decoder decoder;
    private final Encoder encoder;

    protected ByteToMessageCodec() {
        decoder = new Decoder();
        encoder = new Encoder();
    }

    @Override
    public void onMessageReceived(HandlerContext context, byte[] message) {
        decoder.onMessageReceived(context, message);
    }

    @Override
    public void onMessageSent(HandlerContext context, O message, ChannelPromise channelPromise) {
        encoder.onMessageSent(context, message, channelPromise);
    }

    @Override
    public void onHandlerRemoved(HandlerContext context) {
        decoder.onHandlerRemoved(context);
    }

    @Override
    public void onClose(HandlerContext context) {
        decoder.onClose(context);
    }

    protected abstract void decode(HandlerContext context, ByteBuffer message, List<Object> out);

    protected abstract void encode(HandlerContext context, O message, DynamicByteBuffer out);

    private final class Decoder extends ByteToMessageDecoder {
        @Override
        protected void decode(HandlerContext context, ByteBuffer message, List<Object> out) {
            ByteToMessageCodec.this.decode(context, message, out);
        }
    }

    private final class Encoder extends MessageToByteEncoder<O> {
        @Override
        protected void encode(HandlerContext context, O message, DynamicByteBuffer out) {
            ByteToMessageCodec.this.encode(context, message, out);
        }
    }
}
