package com.eatthepath.signal.chat.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;

public class HttpServer {

    private final int port;

    private final HttpRequestAccumulator requestAccumulator;

    private final AsynchronousChannelGroup channelGroup;
    private final AsynchronousServerSocketChannel serverSocketChannel;

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    public HttpServer(final int port) throws IOException {
        this(port, new DefaultHttpRequestAccumulator());
    }

    // Visible for testing
    HttpServer(final int port,
               final HttpRequestAccumulator requestAccumulator) throws IOException {

        this.port = port;
        this.requestAccumulator = requestAccumulator;

        channelGroup = AsynchronousChannelGroup.withFixedThreadPool(4, Executors.defaultThreadFactory());
        serverSocketChannel = AsynchronousServerSocketChannel.open();
    }

    public void start() throws IOException {
        serverSocketChannel.bind(new InetSocketAddress(port));

        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

            @Override
            public void completed(final AsynchronousSocketChannel channel, final Void attachment) {
                log.trace("Accepted channel: {}", channel);

                requestAccumulator.accumulateHttpRequest(channel);

                // Accept the next incoming channel
                serverSocketChannel.accept(null, this);
            }

            @Override
            public void failed(final Throwable throwable, final Void attachment) {
                log.error("Failed to accept channel", throwable);
            }
        });
    }

    private void shutDown() {
        channelGroup.shutdown();
    }
}
