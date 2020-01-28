package com.eatthepath.signal.chat.http;

class DefaultHttpRequestHandler implements HttpRequestHandler {

    @Override
    public void handleHttpRequest(final HttpRequest request) {
        System.out.println(request);
    }
}
