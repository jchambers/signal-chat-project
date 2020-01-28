package com.eatthepath.signal.exercise.chat;

public class UsersAreNotMutualContactsException extends Exception {

    public UsersAreNotMutualContactsException(final long firstUserId, final long secondUserId) {
        super(String.format("Users %d and %d are not mutual contacts", firstUserId, secondUserId));
    }
}
