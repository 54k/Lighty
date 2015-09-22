package io.lighty;

import io.lighty.network.AbstractHandler;
import io.lighty.network.Bootstrap;
import io.lighty.network.Channel;
import io.lighty.network.nio.NioEventLoopGroup;
import io.lighty.network.nio.NioServerSocketChannel;
import io.lighty.network.nio.NioSocketChannel;
import io.lighty.network.pipeline.HandlerContext;

import java.net.InetSocketAddress;

public class ProxyServer {

    private final InetSocketAddress from;
    private final Bootstrap serverBootstrap;

    public ProxyServer(InetSocketAddress from, InetSocketAddress to) {
        this.from = from;
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        serverBootstrap = newBootstrap(NioServerSocketChannel.class, eventLoopGroup);
        serverBootstrap.addChildHandler(new ChannelConnector(to, newBootstrap(NioSocketChannel.class, eventLoopGroup)));
    }

    private static Bootstrap newBootstrap(Class<? extends Channel> channelClass, NioEventLoopGroup eventLoopGroup) {
        return new Bootstrap().channelClass(channelClass).group(eventLoopGroup);
    }

    public void start() throws Exception {
        serverBootstrap.bind(from).sync();
    }

    private static final class ChannelConnector extends AbstractHandler<Object, Object> {

        private final InetSocketAddress to;
        private final Bootstrap clientFactory;

        ChannelConnector(InetSocketAddress to, Bootstrap clientFactory) {
            this.to = to;
            this.clientFactory = clientFactory;
        }

        @Override
        public void onOpen(HandlerContext context) {
            Channel ch = clientFactory.newChannel();
            ch.pipeline().addLast(new ForwardingProxyHandler(context.channel()));
            context.channel().pipeline().addLast(new ForwardingProxyHandler(ch));
            ch.connect(to);
        }
    }

    private static final class ForwardingProxyHandler extends AbstractHandler<Object, Object> {

        private final Channel ch;

        ForwardingProxyHandler(Channel ch) {
            this.ch = ch;
        }

        @Override
        public void onMessageReceived(HandlerContext context, Object message) {
            ch.write(message);
        }

        @Override
        public void onClose(HandlerContext context) {
            ch.close();
        }
    }
}
