package com.eatthepath.signal.chat.controller;

import com.eatthepath.signal.chat.http.HttpRequest;
import com.eatthepath.signal.chat.http.HttpRequestMethod;
import com.eatthepath.signal.chat.http.HttpResponseWriter;

import java.nio.channels.AsynchronousSocketChannel;

public interface Controller {

    boolean canHandlePath(String path);

    boolean canHandleRequestMethod(HttpRequestMethod requestMethod);

    void handleRequest(HttpRequest request, AsynchronousSocketChannel channel, HttpResponseWriter responseWriter);
}
