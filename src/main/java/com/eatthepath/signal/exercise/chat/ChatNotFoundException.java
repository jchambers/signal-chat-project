package com.eatthepath.signal.exercise.chat;

/**
 * Indicates that an operation on a chat failed because no chat could be found with the given ID.
 */
public class ChatNotFoundException extends Exception {

    public ChatNotFoundException(final long chatId) {
        super(String.format("No chat found with ID %d.", chatId));
    }
}
