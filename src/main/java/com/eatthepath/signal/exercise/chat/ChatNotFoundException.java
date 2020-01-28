package com.eatthepath.signal.exercise.chat;

public class ChatNotFoundException extends Exception {

    public ChatNotFoundException(final long chatId) {
        super(String.format("No chat found with ID %d.", chatId));
    }

    public ChatNotFoundException(final long firstUserId, final long secondUserId) {
        super(String.format("No active chat found between users %d and %d.", firstUserId, secondUserId));
    }
}
