package com.eatthepath.signal.exercise.http;

import com.eatthepath.signal.exercise.model.InstantTypeConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

class DefaultHttpResponseWriter implements HttpResponseWriter {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantTypeConverter())
            .create();

    private static final int DEFAULT_HEADER_BUFFER_SIZE = 1024;

    private static final Logger log = LoggerFactory.getLogger(DefaultHttpResponseWriter.class);

    @Override
    public void writeResponse(final AsynchronousSocketChannel channel, final HttpResponseCode responseCode, final Object responseObject) {
        final ByteBuffer headerBuffer = ByteBuffer.allocateDirect(DEFAULT_HEADER_BUFFER_SIZE);
        headerBuffer.put(String.format("HTTP/1.1 %d %s\r\n", responseCode.getStatusCode(), responseCode.getMessage()).getBytes(StandardCharsets.UTF_8));

        writeHeader(headerBuffer, "Connection", "close");

        final ByteBuffer bodyBuffer;
        {
            final byte[] bodyBytes = responseObject != null
                    ? GSON.toJson(responseObject).getBytes(StandardCharsets.UTF_8)
                    : new byte[0];

            bodyBuffer = ByteBuffer.wrap(bodyBytes);
        }

        writeHeader(headerBuffer, "Content-Type", "application/json; charset=utf-8");
        writeHeader(headerBuffer, "Content-Length", String.valueOf(bodyBuffer.limit()));

        headerBuffer.put("\r\n".getBytes(StandardCharsets.UTF_8));
        headerBuffer.flip();

        channel.write(new ByteBuffer[] {headerBuffer, bodyBuffer}, 0, 2, 1, TimeUnit.SECONDS, null,
                new CompletionHandler<Long, Void>() {
                    @Override
                    public void completed(final Long result, final Void attachment) {
                        closeChannel();
                    }

                    @Override
                    public void failed(final Throwable throwable, final Void attachment) {
                        log.warn("Failed to write response", throwable);
                        closeChannel();
                    }

                    private void closeChannel() {
                        try {
                            channel.close();
                        } catch (final IOException e) {
                            log.error("Failed to close channel after sending reply.");
                        }
                    }
                });
    }

    private static void writeHeader(final ByteBuffer buffer, final String key, final String value) {
        buffer.put(String.format("%s: %s\r\n", key, value).getBytes(StandardCharsets.UTF_8));
    }
}
