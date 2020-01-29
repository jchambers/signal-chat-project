package com.eatthepath.signal.exercise.controller;

import com.eatthepath.signal.exercise.chat.ChatNotFoundException;
import com.eatthepath.signal.exercise.chat.ChatService;
import com.eatthepath.signal.exercise.chat.IllegalMessageParticipantException;
import com.eatthepath.signal.exercise.http.HttpRequest;
import com.eatthepath.signal.exercise.http.HttpRequestMethod;
import com.eatthepath.signal.exercise.http.HttpResponseCode;
import com.eatthepath.signal.exercise.http.HttpResponseWriter;
import com.eatthepath.signal.exercise.model.ErrorMessage;
import com.eatthepath.signal.exercise.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

class MessageControllerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private MessageController messageController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @ParameterizedTest
    @MethodSource("canHandlePathSource")
    void canHandlePath(final String path, final boolean expectCanHandle) {
        assertEquals(expectCanHandle, messageController.canHandlePath(path));
    }

    private static Stream<Arguments> canHandlePathSource() {
        return Stream.of(arguments("/chats/1234/messages", true),
                arguments("/chats/messages", false),
                arguments("/chats", false),
                arguments("/chats?userId=12", false));
    }

    @ParameterizedTest
    @MethodSource("canHandleRequestMethodSource")
    void canHandleRequestMethod(final HttpRequestMethod requestMethod, final boolean expectCanHandle) {
        assertEquals(expectCanHandle, messageController.canHandleRequestMethod(requestMethod));
    }

    private static Stream<Arguments> canHandleRequestMethodSource() {
        return Stream.of(arguments(HttpRequestMethod.GET, true),
                arguments(HttpRequestMethod.POST, true));
    }

    @Test
    void handleRequest() {
        final HttpRequest request = mock(HttpRequest.class);
        when(request.getRequestMethod()).thenReturn(HttpRequestMethod.POST);
        when(request.getPath()).thenReturn("/chats/1234/messages");
        when(request.getRequestBody()).thenReturn(Optional.of("This is obviously not legal JSON."));

        final AsynchronousSocketChannel channel = mock(AsynchronousSocketChannel.class);
        final HttpResponseWriter responseWriter = mock(HttpResponseWriter.class);

        messageController.handleRequest(request, channel, responseWriter);

        verify(responseWriter).writeResponse(eq(channel), eq(HttpResponseCode.BAD_REQUEST), any(ErrorMessage.class));
    }

    @Test
    void handlePostMessageRequest() throws Exception {
        final int chatId = 1;
        final Message message = mock(Message.class);
        final AsynchronousSocketChannel channel = mock(AsynchronousSocketChannel.class);
        final HttpResponseWriter responseWriter = mock(HttpResponseWriter.class);

        {
            messageController.handlePostMessageRequest(chatId, message, channel, responseWriter);

            verify(responseWriter).writeResponse(channel, HttpResponseCode.CREATED, null);
        }

        {
            doThrow(new ChatNotFoundException(chatId)).when(chatService).postMessage(chatId, message);
            messageController.handlePostMessageRequest(chatId, message, channel, responseWriter);

            verify(responseWriter).writeResponse(eq(channel), eq(HttpResponseCode.NOT_FOUND), any(ErrorMessage.class));
        }

        {
            doThrow(new IllegalMessageParticipantException()).when(chatService).postMessage(chatId, message);
            messageController.handlePostMessageRequest(chatId, message, channel, responseWriter);

            verify(responseWriter).writeResponse(eq(channel), eq(HttpResponseCode.FORBIDDEN), any(ErrorMessage.class));
        }
    }

    @Test
    void handleListMessagesRequest() throws Exception {
        final int chatId = 1;
        final AsynchronousSocketChannel channel = mock(AsynchronousSocketChannel.class);
        final HttpResponseWriter responseWriter = mock(HttpResponseWriter.class);

        {
            final List<Message> messages = Arrays.asList(mock(Message.class), mock(Message.class));

            when(chatService.getMessagesForChat(chatId)).thenReturn(messages);
            messageController.handleListMessagesRequest(chatId, channel, responseWriter);

            verify(responseWriter).writeResponse(channel, HttpResponseCode.OKAY, messages);
        }

        {
            when(chatService.getMessagesForChat(chatId)).thenThrow(new ChatNotFoundException(chatId));
            messageController.handleListMessagesRequest(chatId, channel, responseWriter);

            verify(responseWriter).writeResponse(eq(channel), eq(HttpResponseCode.NOT_FOUND), any(ErrorMessage.class));
        }
    }
}