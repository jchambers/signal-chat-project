package com.eatthepath.signal.exercise.chat;

/**
 * Indicates that the users identified in a chat creation request are not mutual contacts.
 */
public class UsersAreNotMutualContactsException extends Exception {

    public UsersAreNotMutualContactsException(final long firstUserId, final long secondUserId) {
        super(String.format("Users %d and %d are not mutual contacts", firstUserId, secondUserId));
    }
}
