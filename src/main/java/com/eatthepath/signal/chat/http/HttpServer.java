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

    private final AsynchronousChannelGroup channelGroup;
    private final AsynchronousServerSocketChannel serverSocketChannel;

    private final ConcurrentMap<Channel, ByteBuffer> accumulationBuffersByChannel = new ConcurrentHashMap<>();

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    public HttpServer(final int port) throws IOException {
        this.port = port;

        channelGroup = AsynchronousChannelGroup.withFixedThreadPool(4, Executors.defaultThreadFactory());
        serverSocketChannel = AsynchronousServerSocketChannel.open();
    }

    public void start() throws IOException {
        serverSocketChannel.bind(new InetSocketAddress(port));

        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

            @Override
            public void completed(final AsynchronousSocketChannel channel, final Void attachment) {
                log.debug("Bound channel: {}", channel);

                // TODO Read message
                accumulateHttpMessage(channel);
                // TODO Respond to message

                // Accept the next incoming channel
                serverSocketChannel.accept(null, this);
            }

            @Override
            public void failed(final Throwable throwable, final Void attachment) {
                log.error("Failed to bind channel", throwable);
            }
        });
    }

    private void accumulateHttpMessage(final AsynchronousSocketChannel channel) {
        /* final ByteBuffer accumulationBuffer = accumulationBuffersByChannel.computeIfAbsent(
                channel, key -> ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)); */

        final ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);

        channel.read(buffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(final Integer bytesRead, final Void attachment) {
                accumulationBuffersByChannel.merge(channel, buffer, (existingBuffer, newBuffer) -> {
                    final ByteBuffer combinedBuffer = ByteBuffer.allocate(existingBuffer.position() + newBuffer.position());
                    combinedBuffer.put(existingBuffer);
                    combinedBuffer.put(newBuffer);

                    return combinedBuffer;
                });

                // TODO See if we have a complete HTTP message
            }

            @Override
            public void failed(final Throwable throwable, final Void attachment) {
                log.error("Failed to read from channel", throwable);

                try {
                    channel.close();
                } catch (final IOException e) {
                    log.error("Failed to close channel after a read failure", e);
                }
            }
        });
    }

    private void shutDown() {
        channelGroup.shutdown();
    }
}
