package com.eatthepath.signal.exercise.contacts;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryContactServiceTest {

    @Test
    void usersAreMutualContacts() {
        final InMemoryContactService contactService;
        {
            final Map<Long, Set<Long>> contactsByUserId = new HashMap<>();

            contactsByUserId.put(1L, new HashSet<>(Arrays.asList(2L, 3L)));
            contactsByUserId.put(2L, new HashSet<>(Arrays.asList(1L, 3L)));

            contactService = new InMemoryContactService(contactsByUserId);
        }

        assertTrue(contactService.usersAreMutualContacts(1, 2));
        assertTrue(contactService.usersAreMutualContacts(2, 1));
        assertFalse(contactService.usersAreMutualContacts(1, 3));
        assertFalse(contactService.usersAreMutualContacts(5, 6));
    }
}