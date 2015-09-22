package io.lighty;

import io.lighty.buffer.ByteBufferPool;
import io.lighty.buffer.RecvByteBufferAllocator;

public interface ChannelConfig {

    boolean isAutoRead();

    ChannelConfig setAutoRead(boolean value);

    boolean isAutoFlush();

    ChannelConfig setAutoFlush(boolean value);

    ByteBufferPool getByteBufferPool();

    ChannelConfig setByteBufferPool(ByteBufferPool byteBufferPool);

    int getWriteSpinCount();

    ChannelConfig setWriteSpinCount(int writeSpinCount);

    int getReadSpinCount();

    ChannelConfig setReadSpinCount(int readSpinCount);

    RecvByteBufferAllocator getRecvByteBufferAllocator();

    ChannelConfig setRecvByteBufferAllocator(RecvByteBufferAllocator byteBufferAllocator);

    int getConnectTimeoutMillis();

    void setConnectTimeoutMillis(int connectTimeoutMillis);

    <T> boolean setOption(ChannelOption<T> channelOption, T value);

    <T> T getOption(ChannelOption<T> channelOption);
}
