package com.eatthepath.signal.exercise.controller;

import com.eatthepath.signal.exercise.chat.ChatNotFoundException;
import com.eatthepath.signal.exercise.chat.ChatService;
import com.eatthepath.signal.exercise.chat.IllegalMessageParticipantException;
import com.eatthepath.signal.exercise.http.HttpRequest;
import com.eatthepath.signal.exercise.http.HttpRequestMethod;
import com.eatthepath.signal.exercise.http.HttpResponseCode;
import com.eatthepath.signal.exercise.http.HttpResponseWriter;
import com.eatthepath.signal.exercise.model.ErrorMessage;
import com.eatthepath.signal.exercise.model.InstantTypeConverter;
import com.eatthepath.signal.exercise.model.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.nio.channels.AsynchronousSocketChannel;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The message controller is responsible for handling requests to add messages to a chat session and retrieve messages
 * associated with an existing session.
 */
public class MessageController implements Controller {

    private final ChatService chatService;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantTypeConverter())
            .create();

    private static final Pattern MESSAGE_PATH_PATTERN = Pattern.compile("^/chats/([0-9]+)/messages$");

    public MessageController(final ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public boolean canHandlePath(final String path) {
        return MESSAGE_PATH_PATTERN.matcher(path).matches();
    }

    @Override
    public void handleRequest(final HttpRequest request, final AsynchronousSocketChannel channel, final HttpResponseWriter responseWriter) {
        final long chatId;
        {
            final Matcher pathMatcher = MESSAGE_PATH_PATTERN.matcher(request.getPath());

            if (pathMatcher.matches()) {
                chatId = Integer.parseInt(pathMatcher.group(1), 10);
            } else {
                // This should never happen; we already checked this in `canHandlePath`
                throw new RuntimeException("Could not parse chat ID from request path");
            }
        }

        if (request.getRequestMethod() == HttpRequestMethod.POST) {
            try {
                final Message message = request.getRequestBody().map(body -> GSON.fromJson(body, Message.class))
                        .orElseThrow(() -> new JsonParseException("Could not parse request body as a message"));

                handlePostMessageRequest(chatId, message, channel, responseWriter);
            } catch (final JsonParseException e) {
                responseWriter.writeResponse(channel, HttpResponseCode.BAD_REQUEST,
                        new ErrorMessage("Could not parse request body as a message"));
            }
        } else if (request.getRequestMethod() == HttpRequestMethod.GET) {
            handleListMessagesRequest(chatId, channel, responseWriter);
        } else {
            responseWriter.writeResponse(channel, HttpResponseCode.METHOD_NOT_ALLOWED, new ErrorMessage("Unsupported request method"));
        }
    }

    void handlePostMessageRequest(final long chatId, final Message message, final AsynchronousSocketChannel channel, final HttpResponseWriter responseWriter) {
        try {
            chatService.postMessage(chatId, message);
            responseWriter.writeResponse(channel, HttpResponseCode.CREATED, null);
        } catch (final ChatNotFoundException e) {
            responseWriter.writeResponse(channel, HttpResponseCode.NOT_FOUND, new ErrorMessage("No chat found with ID " + chatId));
        } catch (final IllegalMessageParticipantException e) {
            responseWriter.writeResponse(channel, HttpResponseCode.FORBIDDEN, new ErrorMessage("One or more message participants are not a member of chat " + chatId));
        }
    }

    void handleListMessagesRequest(final long chatId, final AsynchronousSocketChannel channel, final HttpResponseWriter responseWriter) {
        try {
            final List<Message> messages = chatService.getMessagesForChat(chatId);
            responseWriter.writeResponse(channel, HttpResponseCode.OKAY, messages);
        } catch (final ChatNotFoundException e) {
            responseWriter.writeResponse(channel, HttpResponseCode.NOT_FOUND, new ErrorMessage("No chat found with ID " + chatId));
        }
    }
}
