package com.eatthepath.signal.exercise.http;

import com.eatthepath.signal.exercise.model.InstantTypeConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

class DefaultHttpResponseWriter implements HttpResponseWriter {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantTypeConverter())
            .create();

    private static final int DEFAULT_HEADER_BUFFER_SIZE = 1024;

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
        channel.write(headerBuffer);
        channel.write(bodyBuffer);
    }

    private static void writeHeader(final ByteBuffer buffer, final String key, final String value) {
        buffer.put(String.format("%s: %s\r\n", key, value).getBytes(StandardCharsets.UTF_8));
    }
}
