package com.eatthepath.signal.exercise.http;

import java.util.Map;
import java.util.Optional;

/**
 * An {@code HttpRequest} instance represents an HTTP request from a client.
 */
public interface HttpRequest {
    /**
     * Returns the HTTP method for this request.
     *
     * @return the HTTP method for this request
     */
    HttpRequestMethod getRequestMethod();

    /**
     * Returns the path for this request.
     *
     * @return the path for this request
     */
    String getPath();

    /**
     * Returns the version of HTTP used for this request.
     *
     * @return the version of HTTP used for this request
     */
    String getHttpVersion();

    /**
     * Returns a map of headers associated with this request.
     *
     * @return a map of headers associated with this request
     */
    Map<String, String> getHeaders();

    /**
     * Returns the body of this request.
     *
     * @return the body of this request or empty if the request has no body (e.g. for a {@code GET} request)
     */
    Optional<String> getRequestBody();
}
