package com.eatthepath.signal.chat.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultHttpRequestAccumulator implements HttpRequestAccumulator {

    private final ConcurrentMap<Channel, ByteBuffer> accumulationBuffersByChannel = new ConcurrentHashMap<>();

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private static final Pattern REQUEST_LINE_PATTERN = Pattern.compile("^([a-zA-Z]+) ([^\\s]+) (HTTP/.+)$");

    private static final Logger log = LoggerFactory.getLogger(DefaultHttpRequestAccumulator.class);

    private static class IncompleteHttpRequestException extends Exception {}

    private static class InvalidHttpRequestException extends Exception {}

    @Override
    public void accumulateHttpRequest(final AsynchronousSocketChannel channel) {
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

                try {
                    extractHttpRequest(accumulationBuffersByChannel.get(channel));
                } catch (final IncompleteHttpRequestException e) {
                    // Keep waiting for more data
                    accumulateHttpRequest(channel);
                } catch (InvalidHttpRequestException e) {
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

    static HttpRequest extractHttpRequest(final ByteBuffer buffer) throws IncompleteHttpRequestException, InvalidHttpRequestException {
        // This is memory-inefficient and GC-heavy, but it's pretty quick to implement. Given a 4-6 hour window to put
        // together an HTTP server, this is a case where I'll flag that there's lots of room for improvement and plan
        // to revisit it later.
        final byte[] requestBytes = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(requestBytes);

        final String requestString = new String(requestBytes);

        try (final BufferedReader reader = new BufferedReader(new StringReader(requestString))) {
            final HttpRequestMethod requestMethod;
            final String path;
            final String httpVersion;
            {
                final String requestLine = reader.readLine();

                if (requestLine == null) {
                    throw new IncompleteHttpRequestException();
                }

                final Matcher requestLineMatcher = REQUEST_LINE_PATTERN.matcher(requestLine);

                if (requestLineMatcher.matches()) {
                    requestMethod =
                            HttpRequestMethod.valueOf(requestLineMatcher.group(1).toUpperCase().trim());

                    path = requestLineMatcher.group(2);

                    httpVersion = requestLineMatcher.group(3);
                } else {
                    throw new InvalidHttpRequestException();
                }
            }

            final Map<String, String> headers = new HashMap<>();
            {
                String line = reader.readLine();

                while (line != null && line.length() > 0) {
                    final String[] headerPieces = line.split(":", 2);

                    if (headerPieces.length != 2) {
                        throw new InvalidHttpRequestException();
                    }

                    headers.put(headerPieces[0], headerPieces[1].trim());

                    line = reader.readLine();
                }
            }

            // TODO Parse message body

            return new DefaultHttpRequest(requestMethod, path, httpVersion, headers, null);
        } catch (final IOException e) {
            // This should never happen for string readers
            log.error("Exception while reading request string", e);
        }

        throw new InvalidHttpRequestException();
    }
}
