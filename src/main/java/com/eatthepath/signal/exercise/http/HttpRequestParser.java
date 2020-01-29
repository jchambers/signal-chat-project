package com.eatthepath.signal.exercise.http;

import java.nio.ByteBuffer;

/**
 * An HTTP request parser examines the contents of a byte buffer and extracts a complete HTTP request if one is present.
 */
interface HttpRequestParser {

    /**
     * Extracts an {@link HttpRequest} from the given buffer.
     *
     * @param buffer the buffer from which to extract an HTTP request
     * @return the HTTP request contained within the given buffer
     * @throws IncompleteHttpRequestException if the given buffer does not contain a complete HTTP request, but may in
     * the future as more data arrives
     * @throws InvalidHttpRequestException if the given buffer contains data that cannot be parsed as an HTTP request
     * even if more data arrives
     */
    HttpRequest parseHttpRequest(final ByteBuffer buffer) throws IncompleteHttpRequestException, InvalidHttpRequestException;
}
