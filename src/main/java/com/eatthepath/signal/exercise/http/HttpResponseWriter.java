package com.eatthepath.signal.exercise.http;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * An HTTP response writer sends a status code and optional response body to clients. THe response is formatted as a
 * legal HTTP response.
 */
public interface HttpResponseWriter {

    /**
     * Sends an HTTP response to a client via the given channel.
     *
     * @param channel the channel through which to respond to a client
     * @param responseCode the HTTP status code for the response
     * @param responseObject an object to be serialized as a JSON object for the response's body; may be {@code null},
     *                       in which case no response body is sent
     */
    void writeResponse(AsynchronousSocketChannel channel, HttpResponseCode responseCode, Object responseObject);
}
