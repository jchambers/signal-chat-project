package com.eatthepath.signal.exercise.model;

import java.time.Instant;
import java.util.Objects;

/**
 * A {@code Message} instance represents a message sent as part of a chat session.
 */
public class Message {
    private final String id;
    private final Instant timestamp;

    private final String message;

    private final long sourceUserId;
    private final long destinationUserId;

    /**
     * Creates a new message.
     *
     * @param id the ID of the message
     * @param timestamp the time at which the message was created
     * @param message the content of the message
     * @param sourceUserId the ID of the user who sent the message
     * @param destinationUserId the ID of the user to whom the message was sent
     */
    public Message(final String id, final Instant timestamp, final String message, final long sourceUserId, final long destinationUserId) {
        this.id = id;
        this.timestamp = timestamp;
        this.message = message;
        this.sourceUserId = sourceUserId;
        this.destinationUserId = destinationUserId;
    }

    /**
     * Returns the ID of this message.
     *
     * @return the ID of this message
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the time at which this message was created. Note that this may be distinct from the time it was received
     * by the server or transmitted to its destination.
     *
     * @return the time at which this message was created
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the content of the message.
     *
     * @return the content of the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the ID of the user who sent this message.
     *
     * @return the ID of the user who sent this message
     */
    public long getSourceUserId() {
        return sourceUserId;
    }

    /**
     * Returns the ID of the user who received this message.
     *
     * @return the ID of the user who received this message.
     */
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
