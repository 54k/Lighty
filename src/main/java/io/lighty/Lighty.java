package io.lighty;

import io.lighty.nio.NioEventLoopGroup;
import io.lighty.nio.NioServerSocketChannel;
import io.lighty.nio.NioSocketChannel;
import io.lighty.pipeline.HandlerContext;

import java.net.InetSocketAddress;

public class Lighty {

    private final InetSocketAddress from;
    private final Bootstrap serverBootstrap;

    public static void main(String[] args) throws Exception {
        new Lighty(new InetSocketAddress(8080), new InetSocketAddress("www.odnoklassniki.ru", 80)).start();
        new Lighty(new InetSocketAddress(5222), new InetSocketAddress("xmpp.odnoklassniki.ru", 5222)).start();
    }

    public Lighty(InetSocketAddress from, InetSocketAddress to) {
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
