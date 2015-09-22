package io.lighty.network.oio;

import io.lighty.network.ChannelOption;
import io.lighty.network.DefaultChannelConfig;

import java.net.ServerSocket;

public final class OioServerSocketChannelConfig extends DefaultChannelConfig {

    private int backlog = 0;

    OioServerSocketChannelConfig(OioServerSocketChannel channel) {
        super(channel);
    }

    @Override
    protected ServerSocket javaChannel() {
        return (ServerSocket) super.javaChannel();
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    @Override
    protected <T> boolean setOption0(ChannelOption<T> channelOption, T value) throws Exception {
        if (channelOption == ChannelOption.SO_BACKLOG) {
            setBacklog((Integer) value);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T getOption0(ChannelOption<T> channelOption) throws Exception {
        Object result = null;
        if (channelOption == ChannelOption.SO_BACKLOG) {
            result = getBacklog();
        }
        return (T) result;
    }
}
