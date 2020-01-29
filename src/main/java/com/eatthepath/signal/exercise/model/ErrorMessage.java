package com.eatthepath.signal.exercise.model;

/**
 * An error message is a wrapper for a human-readable explanation of the reason a request from a client to the
 * application failed.
 */
public class ErrorMessage {

    private final String error;

    /**
     * Creates a new error message.
     *
     * @param error a human-readable explanation of the reason a request from a client to the application failed
     */
    public ErrorMessage(final String error) {
        this.error = error;
    }

    /**
     * Returns a human-readable explanation of the reason a request from a client to the application failed.
     *
     * @return a human-readable explanation of the reason a request from a client to the application failed
     */
    public String getError() {
        return error;
    }
}
