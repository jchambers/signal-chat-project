package com.eatthepath.signal.exercise.http;

import java.nio.channels.AsynchronousSocketChannel;

public interface HttpResponseWriter {

    void writeResponse(AsynchronousSocketChannel channel, HttpResponseCode responseCode, Object responseObject);
}
