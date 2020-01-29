package com.eatthepath.signal.exercise.controller;

import com.eatthepath.signal.exercise.http.HttpRequest;
import com.eatthepath.signal.exercise.http.HttpRequestMethod;
import com.eatthepath.signal.exercise.http.HttpResponseWriter;

import java.nio.channels.AsynchronousSocketChannel;

public interface Controller {

    boolean canHandlePath(String path);

    void handleRequest(HttpRequest request, AsynchronousSocketChannel channel, HttpResponseWriter responseWriter);
}
