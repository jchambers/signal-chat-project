package com.eatthepath.signal.exercise.http;

class InvalidHttpRequestException extends Exception {

    InvalidHttpRequestException(final String message) {
        super(message);
    }

    InvalidHttpRequestException(final Throwable cause) {
        super(cause);
    }
}
