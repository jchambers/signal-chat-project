package com.eatthepath.signal.chat.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class DefaultHttpRequestParserTest {

    private DefaultHttpRequestParser parser;

    @BeforeEach
    void setUp() {
        parser = new DefaultHttpRequestParser();
    }

    @Test
    void parseHttpRequest() throws Exception {
        {
            final HttpRequest request = parser.parseHttpRequest(wrapString("GET / HTTP/1.1\r\nUser-Agent: Test\r\n\r\n"));

            assertEquals(HttpRequestMethod.GET, request.getRequestMethod());
            assertEquals("/", request.getPath());
            assertEquals("HTTP/1.1", request.getHttpVersion());
            assertEquals(Collections.singletonMap("User-Agent", "Test"), request.getHeaders());
            assertFalse(request.getRequestBody().isPresent());
        }

        {
            final HttpRequest request = parser.parseHttpRequest(wrapString("POST / HTTP/1.1\r\nContent-Length: 11\r\n\r\n{body:json}"));

            assertEquals(HttpRequestMethod.POST, request.getRequestMethod());
            assertEquals("/", request.getPath());
            assertEquals("HTTP/1.1", request.getHttpVersion());
            assertEquals(Collections.singletonMap("Content-Length", "11"), request.getHeaders());
            assertTrue(request.getRequestBody().isPresent());
            assertEquals("{body:json}", request.getRequestBody().get());
        }
    }

    @Test
    void parseHttpRequestIncompleteRequestLine() {
        assertThrows(IncompleteHttpRequestException.class, () -> {
            parser.parseHttpRequest(wrapString("GET / HTTP"));
        });
    }

    @Test
    void parseHttpRequestInvalidRequestLine() {
        assertThrows(InvalidHttpRequestException.class, () -> {
            parser.parseHttpRequest(wrapString("This is an invalid request line.\r\n"));
        });
    }

    @Test
    void parseHttpRequestInvalidHeader() {
        assertThrows(InvalidHttpRequestException.class, () -> {
            parser.parseHttpRequest(wrapString("GET / HTTP/1.1\r\nThis is an invalid header line\r\n\r\n"));
        });
    }

    @Test
    void parseHttpRequestMissingContentLength() {
        assertThrows(InvalidHttpRequestException.class, () -> {
            parser.parseHttpRequest(wrapString("POST / HTTP/1.1\r\nUser-Agent: Test\r\n\r\n{body:json}"));
        });
    }

    @Test
    void parseHttpRequestIncompleteBody() {
        assertThrows(IncompleteHttpRequestException.class, () -> {
            parser.parseHttpRequest(wrapString("POST / HTTP/1.1\r\nContent-Length: 1024\r\n\r\n{body:json}"));
        });
    }

    static ByteBuffer wrapString(final String string) {
        return ByteBuffer.wrap(string.getBytes());
    }
}