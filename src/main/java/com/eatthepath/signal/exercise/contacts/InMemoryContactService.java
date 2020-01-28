package com.eatthepath.signal.exercise.contacts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryContactService implements ContactService {

    private final Map<Long, Set<Long>> contactsByUserId;

    public InMemoryContactService() throws IOException {
        final Gson gson = new GsonBuilder().create();
        final Type type = new TypeToken<Map<String, long[]>>() {}.getType();

        try (final InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("contacts.json"))) {
            final Map<String, long[]> contactMap = gson.fromJson(reader, type);

            contactsByUserId = new HashMap<>(contactMap.size());

            contactMap.forEach((userId, contacts) -> contactsByUserId.put(Long.parseLong(userId, 10),
                    Arrays.stream(contacts).boxed().collect(Collectors.toSet())));
        }
    }

    // Visible for testing
    InMemoryContactService(final Map<Long, Set<Long>> contactsByUserId) {
        this.contactsByUserId = contactsByUserId;
    }

    @Override
    public boolean usersAreMutualContacts(final long firstUserId, final long secondUserId) {
        final Set<Long> firstUserContacts = contactsByUserId.getOrDefault(firstUserId, Collections.emptySet());
        final Set<Long> secondUserContacts = contactsByUserId.getOrDefault(secondUserId, Collections.emptySet());

        return firstUserContacts.contains(secondUserId) && secondUserContacts.contains(firstUserId);
    }
}
