package com.eatthepath.signal.chat.http;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

class DefaultHttpRequest implements HttpRequest {

    private final HttpRequestMethod requestMethod;
    private final String path;
    private final String httpVersion;

    private final Map<String, String> headers;

    private final String body;

    DefaultHttpRequest(final HttpRequestMethod requestMethod,
                       final String path,
                       final String httpVersion,
                       final Map<String, String> headers,
                       final String body) {

        this.requestMethod = requestMethod;
        this.path = path;
        this.httpVersion = httpVersion;
        this.headers = Collections.unmodifiableMap(headers);
        this.body = body;
    }

    @Override
    public HttpRequestMethod getRequestMethod() {
        return requestMethod;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getHttpVersion() {
        return httpVersion;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public Optional<String> getRequestBody() {
        return Optional.ofNullable(body);
    }
}
