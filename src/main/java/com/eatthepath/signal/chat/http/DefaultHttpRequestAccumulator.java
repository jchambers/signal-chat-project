package com.eatthepath.signal.chat.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultHttpRequestAccumulator implements HttpRequestAccumulator {

    private final ConcurrentMap<Channel, ByteBuffer> accumulationBuffersByChannel = new ConcurrentHashMap<>();

    private final HttpRequestParser requestParser;
    private final HttpRequestHandler requestHandler;

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private static final Logger log = LoggerFactory.getLogger(DefaultHttpRequestAccumulator.class);

    DefaultHttpRequestAccumulator(final HttpRequestHandler requestHandler) {
        this(requestHandler, new DefaultHttpRequestParser());
    }

    // Visible for testing
    DefaultHttpRequestAccumulator(final HttpRequestHandler requestHandler, final HttpRequestParser requestParser) {
        this.requestHandler = requestHandler;
        this.requestParser = requestParser;
    }

    @Override
    public void accumulateHttpRequest(final AsynchronousSocketChannel channel) {
        final ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);

        channel.read(buffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(final Integer bytesRead, final Void attachment) {
                buffer.flip();

                accumulationBuffersByChannel.merge(channel, buffer, (existingBuffer, newBuffer) -> {
                    final ByteBuffer combinedBuffer = ByteBuffer.allocate(existingBuffer.limit() + newBuffer.limit());
                    combinedBuffer.put(existingBuffer);
                    combinedBuffer.put(newBuffer);

                    return combinedBuffer;
                });

                try {
                    final HttpRequest request = requestParser.parseHttpRequest(accumulationBuffersByChannel.get(channel));

                    // Clear out the accumulation buffer now that we have a complete request
                    accumulationBuffersByChannel.remove(channel);

                    requestHandler.handleHttpRequest(request, channel);
                } catch (final IncompleteHttpRequestException e) {
                    // Keep waiting for more data
                    accumulateHttpRequest(channel);
                } catch (final InvalidHttpRequestException e) {
                    // TODO
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(final Throwable throwable, final Void attachment) {
                log.error("Failed to read from channel", throwable);

                try {
                    channel.close();
                } catch (final IOException e) {
                    e.addSuppressed(throwable);
                    log.error("Failed to close channel after a read failure", e);
                }
            }
        });
    }
}
