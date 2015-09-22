package io.lighty;

import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws Exception {
        new ProxyServer(new InetSocketAddress(8080), new InetSocketAddress("www.odnoklassniki.ru", 80)).start();
        new ProxyServer(new InetSocketAddress(5222), new InetSocketAddress("xmpp.odnoklassniki.ru", 5222)).start();
    }
}
