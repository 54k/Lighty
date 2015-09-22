package io.lighty.buffer;

import java.nio.ByteBuffer;

public final class Buffers {

    private Buffers() {
    }

    public static byte[] getRemainingBytes(ByteBuffer buffer) {
        int length = buffer.limit() - buffer.position();
        byte[] message = new byte[length];
        buffer.get(message, 0, length);
        return message;
    }
}
