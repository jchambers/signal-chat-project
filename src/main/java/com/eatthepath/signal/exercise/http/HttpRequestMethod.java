package com.eatthepath.signal.exercise.http;

/**
 * An enumeration of HTTP request methods.
 *
 * @see <a href="https://tools.ietf.org/html/rfc2616#section-5.1.1">RFC 2616: Hypertext Transfer Protocol -- HTTP/1.1, Section 5.1.1 - Method</a>
 */
public enum HttpRequestMethod {
    OPTIONS,
    GET,
    HEAD,
    POST,
    PUT,
    DELETE,
    TRACE,
    CONNECT
}
