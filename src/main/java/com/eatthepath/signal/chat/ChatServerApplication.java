package com.eatthepath.signal.chat;

import com.eatthepath.signal.chat.http.HttpServer;

import java.io.IOException;

public class ChatServerApplication {

    public static void main(final String... args) throws IOException {
        final HttpServer httpServer = new HttpServer(80);
        httpServer.start();
    }
}
