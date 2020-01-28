package com.eatthepath.signal.exercise.http;

import java.nio.channels.AsynchronousSocketChannel;

interface HttpRequestHandler {
    void handleHttpRequest(HttpRequest request, AsynchronousSocketChannel channel);
}
