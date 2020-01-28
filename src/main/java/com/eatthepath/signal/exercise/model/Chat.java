package com.eatthepath.signal.exercise.model;

import java.util.Arrays;
import java.util.Objects;

public class Chat {
    private final long id;
    private final long[] participantIds;

    public Chat(final long id, final long[] participantIds) {
        this.id = id;
        this.participantIds = participantIds;
    }

    public long getId() {
        return id;
    }

    public long[] getParticipantIds() {
        return participantIds;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Chat chat = (Chat) o;
        return id == chat.id &&
                Arrays.equals(participantIds, chat.participantIds);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id);
        result = 31 * result + Arrays.hashCode(participantIds);
        return result;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id=" + id +
                ", participantIds=" + Arrays.toString(participantIds) +
                '}';
    }
}
