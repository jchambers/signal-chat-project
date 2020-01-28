package com.eatthepath.signal.exercise.model;

import java.time.Instant;
import java.util.Objects;

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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Message message1 = (Message) o;
        return sourceUserId == message1.sourceUserId &&
                destinationUserId == message1.destinationUserId &&
                Objects.equals(id, message1.id) &&
                Objects.equals(timestamp, message1.timestamp) &&
                Objects.equals(message, message1.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp, message, sourceUserId, destinationUserId);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                ", sourceUserId=" + sourceUserId +
                ", destinationUserId=" + destinationUserId +
                '}';
    }
}
