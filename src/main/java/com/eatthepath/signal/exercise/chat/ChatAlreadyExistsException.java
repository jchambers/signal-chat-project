package com.eatthepath.signal.exercise.chat;

/**
 * Indicates that an attempt to create a new chat has failed because a chat with the given ID already exists.
 */
public class ChatAlreadyExistsException extends Exception {

    public ChatAlreadyExistsException(final long chatId) {
        super(String.format("Chat %d already exists and cannot be re-created.", chatId));
    }
}
