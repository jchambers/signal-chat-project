package com.eatthepath.signal.chat.http;

import java.nio.channels.AsynchronousSocketChannel;

interface HttpRequestHandler {
    void handleHttpRequest(HttpRequest request, AsynchronousSocketChannel channel);
}
