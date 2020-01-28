package com.eatthepath.signal.exercise;

import com.eatthepath.signal.exercise.contacts.ContactService;
import com.eatthepath.signal.exercise.contacts.InMemoryContactService;
import com.eatthepath.signal.exercise.http.HttpServer;

import java.io.IOException;

public class ChatServerApplication {

    public static void main(final String... args) throws IOException {
        final ContactService contactService = new InMemoryContactService();

        final HttpServer httpServer = new HttpServer(80);
        httpServer.start();
    }
}
