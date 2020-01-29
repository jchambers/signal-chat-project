package com.eatthepath.signal.exercise.controller;

import com.eatthepath.signal.exercise.chat.ChatAlreadyExistsException;
import com.eatthepath.signal.exercise.chat.ChatService;
import com.eatthepath.signal.exercise.chat.IllegalParticipantCountException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The chat controller is responsible for handling requests to create and list chat sessions.
 */
public class ChatController implements Controller {

    private final ChatService chatService;

    private static final Pattern CHAT_PATH_PATTERN = Pattern.compile("^/chats(\\?userId=([0-9]+))?$");

    private static final Gson GSON = new GsonBuilder().create();

    public ChatController(final ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public boolean canHandlePath(final String path) {
        return CHAT_PATH_PATTERN.matcher(path).matches();
    }

    @Override
    public void handleRequest(final HttpRequest request, final AsynchronousSocketChannel channel, final HttpResponseWriter responseWriter) {
        if (request.getRequestMethod() == HttpRequestMethod.GET) {
            final Matcher pathMatcher = CHAT_PATH_PATTERN.matcher(request.getPath());

            if (pathMatcher.matches() && pathMatcher.groupCount() == 2) {
                final long userId = Integer.parseInt(pathMatcher.group(2), 10);
                handleListChatsRequest(userId, channel, responseWriter);
            } else {
                responseWriter.writeResponse(channel, HttpResponseCode.BAD_REQUEST, new ErrorMessage("Could not parse user ID"));
            }
        } else if (request.getRequestMethod() == HttpRequestMethod.POST) {
            try {
                final Chat chat = request.getRequestBody().map(body -> GSON.fromJson(body, Chat.class))
                        .orElseThrow(() -> new JsonParseException("Could not parse request body as a chat object."));

                handleCreateChatRequest(chat, channel, responseWriter);
            } catch (final JsonParseException e) {
                responseWriter.writeResponse(channel, HttpResponseCode.BAD_REQUEST, new ErrorMessage("Could not parse request body as a chat object."));
            }
        } else {
            responseWriter.writeResponse(channel, HttpResponseCode.METHOD_NOT_ALLOWED, new ErrorMessage("Unsupported request method"));
        }
    }

    void handleCreateChatRequest(final Chat chat, final AsynchronousSocketChannel channel, final HttpResponseWriter responseWriter) {
        try {
            chatService.createChat(chat);
            responseWriter.writeResponse(channel, HttpResponseCode.CREATED, null);
        } catch (final UsersAreNotMutualContactsException e) {
            responseWriter.writeResponse(channel, HttpResponseCode.FORBIDDEN, new ErrorMessage("Users in chat request are not mutual contacts."));
        } catch (final ChatAlreadyExistsException e) {
            responseWriter.writeResponse(channel, HttpResponseCode.CONFLICT, new ErrorMessage("Chat already exists"));
        } catch (final IllegalParticipantCountException e) {
            responseWriter.writeResponse(channel, HttpResponseCode.BAD_REQUEST, new ErrorMessage("Chat requests must contain exactly two user IDs"));
        }
    }

    void handleListChatsRequest(final long userId, final AsynchronousSocketChannel channel, final HttpResponseWriter responseWriter) {
        responseWriter.writeResponse(channel, HttpResponseCode.OKAY, chatService.getChatsForUser(userId));
    }
}
