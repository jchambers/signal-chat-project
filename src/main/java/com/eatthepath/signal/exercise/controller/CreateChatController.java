package com.eatthepath.signal.exercise.controller;

import com.eatthepath.signal.exercise.http.HttpRequest;
import com.eatthepath.signal.exercise.http.HttpRequestMethod;
import com.eatthepath.signal.exercise.http.HttpResponseWriter;

import java.nio.channels.AsynchronousSocketChannel;

public class CreateChatController implements Controller {

    @Override
    public boolean canHandlePath(final String path) {
        return "/chats".equals(path);
    }

    @Override
    public boolean canHandleRequestMethod(final HttpRequestMethod requestMethod) {
        return requestMethod == HttpRequestMethod.POST;
    }

    @Override
    public void handleRequest(final HttpRequest request, final AsynchronousSocketChannel channel, final HttpResponseWriter responseWriter) {

    }
}
