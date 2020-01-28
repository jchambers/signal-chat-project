package com.eatthepath.signal.exercise.contacts;

/**
 * The contact service managers users' contact lists. While very simple in the scope of this exercise, this is where
 * we'd add things like adding new contacts, getting individual contact lists, and notifying users when somebody else
 * has added or wants to add them to their contact list.
 */
public interface ContactService {

    /**
     * Tests whether two users are mutual contacts (i.e. user A is in user B's contact list and user B is in user A's
     * contact list).
     *
     * @param firstUserId the ID of the first user for whom to test mutual connectivity
     * @param secondUserId the ID of the second user for whom to test mutual connectivity
     * @return {@code true} if the users are mutual contacts or {@code false} otherwise
     */
    boolean usersAreMutualContacts(final long firstUserId, final long secondUserId);
}
