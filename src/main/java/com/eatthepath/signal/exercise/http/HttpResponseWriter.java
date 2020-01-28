package com.eatthepath.signal.exercise.http;

public interface HttpResponseWriter {

    void writeResponse(HttpResponseCode responseCode, Object responseObject);
}
