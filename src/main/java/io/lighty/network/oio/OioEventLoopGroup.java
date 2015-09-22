package io.lighty.network.oio;

import io.lighty.network.concurrent.DefaultThreadFactory;
import io.lighty.network.ThreadPerChannelEventLoopGroup;

public final class OioEventLoopGroup extends ThreadPerChannelEventLoopGroup {

    public OioEventLoopGroup() {
        this(0);
    }

    public OioEventLoopGroup(int maxChannels) {
        super(maxChannels, new DefaultThreadFactory("network-oio-eventloop"));
    }
}
