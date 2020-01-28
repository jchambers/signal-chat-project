package com.eatthepath.signal.chat;

import com.eatthepath.signal.chat.contacts.ContactService;
import com.eatthepath.signal.chat.contacts.InMemoryContactService;
import com.eatthepath.signal.chat.http.HttpServer;

import java.io.IOException;

public class ChatServerApplication {

    public static void main(final String... args) throws IOException {
        final ContactService contactService = new InMemoryContactService();

        final HttpServer httpServer = new HttpServer(80);
        httpServer.start();
    }
}
