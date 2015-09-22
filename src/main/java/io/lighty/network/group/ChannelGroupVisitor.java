package io.lighty.network.group;

public interface ChannelGroupVisitor<T> {

    T visit(ChannelGroup channelGroup);
}
