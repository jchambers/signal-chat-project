package com.eatthepath.signal.exercise.http;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * An HTTP request handler processes complete HTTP requests.
 */
interface HttpRequestHandler {

    /**
     * Handle a complete HTTP request.
     *
     * @param request the request to be processed
     * @param channel the channel on which the request was received (and a response should be sent)
     */
    void handleHttpRequest(HttpRequest request, AsynchronousSocketChannel channel);
}
