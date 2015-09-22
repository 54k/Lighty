package io.lighty.network.codec;

import io.lighty.network.AbstractHandler;
import io.lighty.network.ChannelPromise;
import io.lighty.network.buffer.ByteBufferPool;
import io.lighty.network.buffer.DynamicByteBuffer;
import io.lighty.network.pipeline.HandlerContext;

public abstract class MessageToByteEncoder<O> extends AbstractHandler<Object, O> {

    @Override
    public void onMessageSent(HandlerContext context, O message, ChannelPromise channelPromise) {
        ByteBufferPool allocator = context.channel().config().getByteBufferPool();
        DynamicByteBuffer out = allocator.acquireDynamic(0, preferDirectBuffer());

        try {
            encode(context, message, out);
            out.flip();
            byte[] bytes = new byte[out.remaining()];
            out.get(bytes);
            context.write(bytes, channelPromise);
        } catch (EncoderException e) {
            throw e;
        } catch (Throwable e) {
            throw new EncoderException(e);
        } finally {
            allocator.release(out);
        }
    }

    protected boolean preferDirectBuffer() {
        return false;
    }

    protected abstract void encode(HandlerContext context, O message, DynamicByteBuffer out);
}
