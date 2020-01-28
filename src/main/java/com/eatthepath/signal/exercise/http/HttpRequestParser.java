package com.eatthepath.signal.exercise.http;

import java.nio.ByteBuffer;

interface HttpRequestParser {
    HttpRequest parseHttpRequest(final ByteBuffer buffer) throws IncompleteHttpRequestException, InvalidHttpRequestException;
}
