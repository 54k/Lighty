package io.lighty.network;

public interface ChannelFactory<I extends Channel> {

    I createChannel(Class<? extends I> channelClass);
}
