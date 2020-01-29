package com.eatthepath.signal.exercise.model;

import java.util.List;
import java.util.Objects;

/**
 * A {@code Chat} instance represents an existing chat session or a request to create a chat session.
 */
public class Chat {
    private final long id;
    private final List<Long> participantIds;

    /**
     * Creates a new chat model object.
     *
     * @param id the ID of the chat session
     * @param participantIds the IDs of the users participating in this chat session; chats should always have two
     *                       distinct participants, but that requirement is not enforced at the model level
     */
    public Chat(final long id, final List<Long> participantIds) {
        this.id = id;
        this.participantIds = participantIds;
    }

    /**
     * Returns the ID of this chat session.
     *
     * @return the ID of this chat session
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the IDs of the users participating in this chat session.
     *
     * @return the IDs of the users participating in this chat session
     */
    public List<Long> getParticipantIds() {
        return participantIds;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Chat chat = (Chat) o;
        return id == chat.id &&
                Objects.equals(participantIds, chat.participantIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, participantIds);
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id=" + id +
                ", participantIds=" + participantIds +
                '}';
    }
}
