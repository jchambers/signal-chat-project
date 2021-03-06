package com.eatthepath.signal.exercise;

import com.eatthepath.signal.exercise.chat.ChatService;
import com.eatthepath.signal.exercise.chat.InMemoryChatService;
import com.eatthepath.signal.exercise.contacts.ContactService;
import com.eatthepath.signal.exercise.contacts.InMemoryContactService;
import com.eatthepath.signal.exercise.controller.ChatController;
import com.eatthepath.signal.exercise.controller.MessageController;
import com.eatthepath.signal.exercise.http.HttpServer;

import java.io.IOException;

/**
 * A simple application that constructs and starts an HTTP server that handles chat REST API requests.
 */
public class ChatServerApplication {

    public static void main(final String... args) throws IOException {
        final ContactService contactService = new InMemoryContactService();
        final ChatService chatService = new InMemoryChatService(contactService);

        final ChatController chatController = new ChatController(chatService);
        final MessageController messageController = new MessageController(chatService);

        final HttpServer httpServer = new HttpServer(80);
        httpServer.registerController(chatController);
        httpServer.registerController(messageController);
        httpServer.start();
    }
}
