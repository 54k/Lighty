package io.lighty.buffer;

import java.nio.ByteBuffer;

public interface ByteBufferPool {

    ByteBuffer acquire(int size, boolean direct);

    void release(ByteBuffer buffer);

    void clear();
}
