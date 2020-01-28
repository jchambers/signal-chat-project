package com.eatthepath.signal.exercise.model;

import java.time.Instant;

public class Message {
    private final String id;
    private final Instant timestamp;

    private final String message;

    private final long sourceUserId;
    private final long destinationUserId;

    public Message(final String id, final Instant timestamp, final String message, final long sourceUserId, final long destinationUserId) {
        this.id = id;
        this.timestamp = timestamp;
        this.message = message;
        this.sourceUserId = sourceUserId;
        this.destinationUserId = destinationUserId;
    }

    public String getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public long getSourceUserId() {
        return sourceUserId;
    }

    public long getDestinationUserId() {
        return destinationUserId;
    }
}
