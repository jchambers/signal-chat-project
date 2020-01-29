package com.eatthepath.signal.exercise.http;

/**
 * An enumeration of HTTP response codes.
 *
 * @see <a href="https://tools.ietf.org/html/rfc2616#section-6.1.1">RFC 2616: Hypertext Transfer Protocol -- HTTP/1.1, Section 6.1.1 - Status Code and Reason Phrase</a>
 */
public enum HttpResponseCode {
    OKAY(200, "OK"),
    CREATED(201, "Created"),
    BAD_REQUEST(400, "Bad request"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not found"),
    METHOD_NOT_ALLOWED(405, "Method not allowed"),
    CONFLICT(409, "Conflict");

    private final int statusCode;
    private final String message;

    HttpResponseCode(final int statusCode, final String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
