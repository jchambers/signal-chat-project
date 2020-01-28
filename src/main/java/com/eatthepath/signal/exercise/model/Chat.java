package com.eatthepath.signal.exercise.model;

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
}
