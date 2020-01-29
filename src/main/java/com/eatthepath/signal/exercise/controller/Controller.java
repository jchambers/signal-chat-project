package com.eatthepath.signal.exercise.controller;

import com.eatthepath.signal.exercise.http.HttpRequest;
import com.eatthepath.signal.exercise.http.HttpResponseWriter;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * A controller is responsible for handling web requests and acting as a gateway to the application's data and services.
 */
public interface Controller {

    /**
     * Tests whether this controller is capable of handling requests for the given path.
     *
     * @param path the requested path
     *
     * @return {@code true} if this controller can handle requests for the given path or {@code false} otherwise
     */
    boolean canHandlePath(String path);

    /**
     * Handles a web request. Whether or not the request is "successful," controllers must always send some kind of
     * response via the given {@code HttpResponseWriter}.
     *
     * @param request the request to be handled
     * @param channel the channel via which the request was received and a response should be sent
     * @param responseWriter the response writer to be used to write a response for the given request
     */
    void handleRequest(HttpRequest request, AsynchronousSocketChannel channel, HttpResponseWriter responseWriter);
}
