package com.eatthepath.signal.chat.http;

public interface HttpResponseWriter {

    void writeResponse(HttpResponseCode responseCode, Object responseObject);
}
