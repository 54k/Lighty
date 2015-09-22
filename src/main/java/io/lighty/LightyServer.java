package io.lighty;

import io.lighty.nio.NioEventLoopGroup;
import io.lighty.nio.NioServerSocketChannel;
import io.lighty.nio.NioSocketChannel;
import io.lighty.pipeline.HandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public final class LightyServer {

    private static final NioEventLoopGroup SERVER_GROUP = new NioEventLoopGroup();
    private static final NioEventLoopGroup CLIENT_GROUP = new NioEventLoopGroup();

    private static final Logger log = LoggerFactory.getLogger(LightyServer.class);

    private final InetSocketAddress from;
    private final Bootstrap serverBootstrap;

    public LightyServer(InetSocketAddress from, InetSocketAddress to) {
        this.from = from;
        serverBootstrap = newBootstrap(NioServerSocketChannel.class, SERVER_GROUP);
        serverBootstrap.addChildHandler(new ChannelConnector(to, newBootstrap(NioSocketChannel.class, CLIENT_GROUP)));
    }

    private static Bootstrap newBootstrap(Class<? extends Channel> channelClass, NioEventLoopGroup eventLoopGroup) {
        return new Bootstrap().channelClass(channelClass)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .group(eventLoopGroup);
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
            Channel channel = context.channel();
            Channel ch = clientFactory.newChannel();
            ch.pipeline().addLast(new ForwardingProxyHandler(channel));
            channel.pipeline().addLast(new ForwardingProxyHandler(ch));
            try {
                ch.connect(to).sync();
            } catch (InterruptedException ignore) {
                channel.close();
            }
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
        public void onDisconnect(HandlerContext context, ChannelPromise channelPromise) {
            ch.close();
        }

        @Override
        public void onClose(HandlerContext context) {
            ch.close();
        }

        @Override
        public void onExceptionCaught(HandlerContext context, Throwable e) {
            ch.close();
            log.error("IO Exception occurred", e);
        }
    }
}
