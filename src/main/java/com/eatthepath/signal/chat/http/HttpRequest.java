package com.eatthepath.signal.chat.http;

import java.util.Map;
import java.util.Optional;

public interface HttpRequest {
    HttpRequestMethod getRequestMethod();
    String getPath();
    String getHttpVersion();

    Map<String, String> getHeaders();

    Optional<String> getRequestBody();
}
