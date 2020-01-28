package com.eatthepath.signal.exercise.controller;

import com.eatthepath.signal.exercise.chat.ChatAlreadyExistsException;
import com.eatthepath.signal.exercise.chat.ChatService;
import com.eatthepath.signal.exercise.chat.IllegalUserCountException;
import com.eatthepath.signal.exercise.chat.UsersAreNotMutualContactsException;
import com.eatthepath.signal.exercise.http.HttpRequest;
import com.eatthepath.signal.exercise.http.HttpRequestMethod;
import com.eatthepath.signal.exercise.http.HttpResponseCode;
import com.eatthepath.signal.exercise.http.HttpResponseWriter;
import com.eatthepath.signal.exercise.model.Chat;
import com.eatthepath.signal.exercise.model.ErrorMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.nio.channels.AsynchronousSocketChannel;

public class CreateChatController implements Controller {

    private final ChatService chatService;

    private final Gson gson = new GsonBuilder().create();

    public CreateChatController(final ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public boolean canHandlePath(final String path) {
        return "/chats".equals(path);
    }

    @Override
    public boolean canHandleRequestMethod(final HttpRequestMethod requestMethod) {
        return requestMethod == HttpRequestMethod.POST;
    }

    @Override
    public void handleRequest(final HttpRequest request, final AsynchronousSocketChannel channel, final HttpResponseWriter responseWriter) {
        try {
            final Chat chat = request.getRequestBody().map(body -> gson.fromJson(body, Chat.class))
                    .orElseThrow(() -> new JsonParseException("Could not parse request body as a chat object."));

            try {
                chatService.createChat(chat);
                responseWriter.writeResponse(channel, HttpResponseCode.CREATED, null);
            } catch (final UsersAreNotMutualContactsException e) {
                responseWriter.writeResponse(channel, HttpResponseCode.FORBIDDEN, new ErrorMessage("Users in chat request are not mutual contacts."));
            } catch (final ChatAlreadyExistsException e) {
                responseWriter.writeResponse(channel, HttpResponseCode.CONFLICT, new ErrorMessage("Chat already exists"));
            } catch (final IllegalUserCountException e) {
                responseWriter.writeResponse(channel, HttpResponseCode.BAD_REQUEST, new ErrorMessage("Chat requests must contain exactly two user IDs"));
            }
        } catch (final JsonParseException e) {
            responseWriter.writeResponse(channel, HttpResponseCode.BAD_REQUEST, new ErrorMessage("Could not parse request body as a chat object."));
        }
    }
}
