package io.lighty.network.nio;

import io.lighty.network.ChannelOption;

import java.nio.channels.ServerSocketChannel;

public final class NioServerSocketChannelConfig extends NetworkChannelConfig {

    NioServerSocketChannelConfig(AbstractNioChannel channel) {
        super(channel);
    }

    @Override
    protected ServerSocketChannel javaChannel() {
        return (ServerSocketChannel) super.javaChannel();
    }

    @Override
    protected <T> boolean setOption0(ChannelOption<T> channelOption, T value) throws Exception {
        return true;
    }

    @Override
    protected <T> T getOption0(ChannelOption<T> channelOption) throws Exception {
        return null;
    }
}
