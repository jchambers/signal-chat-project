package com.eatthepath.signal.exercise.chat;

public class ChatAlreadyExistsException extends Exception {

    public ChatAlreadyExistsException(final long chatId) {
        super(String.format("Chat %d already exists and cannot be re-created.", chatId));
    }
}
