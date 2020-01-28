package com.eatthepath.signal.exercise.http;

import com.eatthepath.signal.exercise.model.InstantTypeConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class DefaultHttpResponseWriter implements HttpResponseWriter {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantTypeConverter())
            .create();

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    @Override
    public void writeResponse(final AsynchronousSocketChannel channel, final HttpResponseCode responseCode, final Object responseObject) {
        final ByteBuffer responseBuffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
        responseBuffer.put(String.format("HTTP/1.1 %d %s\r\n", responseCode.getStatusCode(), responseCode.getMessage()).getBytes(StandardCharsets.UTF_8));

        writeHeader(responseBuffer, "Connection", "close");

        final byte[] bodyBytes = responseObject != null
                ? GSON.toJson(responseObject).getBytes(StandardCharsets.UTF_8)
                : null;

        writeHeader(responseBuffer, "Content-Type", "application/json; charset=utf-8");
        writeHeader(responseBuffer, "Content-Length", String.valueOf(bodyBytes != null ? bodyBytes.length : 0));

        responseBuffer.put("\r\n".getBytes(StandardCharsets.UTF_8));

        if (bodyBytes != null) {
            responseBuffer.put(bodyBytes);
        }

        responseBuffer.flip();

        channel.write(responseBuffer);
    }

    private static void writeHeader(final ByteBuffer buffer, final String key, final String value) {
        buffer.put(String.format("%s: %s\r\n", key, value).getBytes(StandardCharsets.UTF_8));
    }
}
