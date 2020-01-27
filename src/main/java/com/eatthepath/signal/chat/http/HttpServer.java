package com.eatthepath.signal.chat.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class HttpServer {

    private final IOThreadPool threadPool;

    private final ServerSocketChannel serverSocketChannel;

    public HttpServer() throws IOException {
        // TODO Make pool size configurable
        threadPool = new IOThreadPool(1);

        serverSocketChannel = ServerSocketChannel.open();
    }

    public void start() throws IOException {
        threadPool.start();

        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(false);

        serverSocketChannel.register(threadPool.getNextThread().getSelector(), SelectionKey.OP_ACCEPT);
        System.out.println("Registered!");
    }

    private void shutDown() {
        // TODO
    }
}
