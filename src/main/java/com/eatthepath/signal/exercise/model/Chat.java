package com.eatthepath.signal.exercise.model;

import java.util.List;
import java.util.Objects;

public class Chat {
    private final long id;
    private final List<Long> participantIds;

    public Chat(final long id, final List<Long> participantIds) {
        this.id = id;
        this.participantIds = participantIds;
    }

    public long getId() {
        return id;
    }

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
