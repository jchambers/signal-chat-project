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

class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @ParameterizedTest
    @MethodSource("canHandlePathSource")
    void canHandlePath(final String path, final boolean expectCanHandle) {
        assertEquals(expectCanHandle, chatController.canHandlePath(path));
    }

    private static Stream<Arguments> canHandlePathSource() {
        return Stream.of(arguments("/chats", true),
                arguments("/chats?userId=12", true),
                arguments("/chats?userId", false),
                arguments("/chats/1234/messages", false),
                arguments("/chats/messages", false));
    }

    @Test
    void handleRequest() {
        final HttpRequest request = mock(HttpRequest.class);
        when(request.getRequestMethod()).thenReturn(HttpRequestMethod.POST);
        when(request.getPath()).thenReturn("/chats");
        when(request.getRequestBody()).thenReturn(Optional.empty());

        final AsynchronousSocketChannel channel = mock(AsynchronousSocketChannel.class);
        final HttpResponseWriter responseWriter = mock(HttpResponseWriter.class);

        chatController.handleRequest(request, channel, responseWriter);
        verify(responseWriter).writeResponse(eq(channel), eq(HttpResponseCode.BAD_REQUEST), any(ErrorMessage.class));

        when(request.getRequestMethod()).thenReturn(HttpRequestMethod.POST);

        chatController.handleRequest(request, channel, responseWriter);
        verify(responseWriter, times(2)).writeResponse(eq(channel), eq(HttpResponseCode.BAD_REQUEST), any(ErrorMessage.class));

        when(request.getRequestBody()).thenReturn(Optional.of("This is obviously not legal JSON."));

        chatController.handleRequest(request, channel, responseWriter);
        verify(responseWriter, times(3)).writeResponse(eq(channel), eq(HttpResponseCode.BAD_REQUEST), any(ErrorMessage.class));

        when(request.getRequestMethod()).thenReturn(HttpRequestMethod.DELETE);

        chatController.handleRequest(request, channel, responseWriter);
        verify(responseWriter).writeResponse(eq(channel), eq(HttpResponseCode.METHOD_NOT_ALLOWED), any(ErrorMessage.class));
    }

    @Test
    void handleCreateChatRequest() throws Exception {
        final Chat chat = mock(Chat.class);

        final AsynchronousSocketChannel channel = mock(AsynchronousSocketChannel.class);
        final HttpResponseWriter responseWriter = mock(HttpResponseWriter.class);

        chatController.handleCreateChatRequest(chat, channel, responseWriter);
        verify(responseWriter).writeResponse(channel, HttpResponseCode.CREATED, null);

        doThrow(new UsersAreNotMutualContactsException(1, 2)).when(chatService).createChat(chat);

        chatController.handleCreateChatRequest(chat, channel, responseWriter);
        verify(responseWriter).writeResponse(eq(channel), eq(HttpResponseCode.FORBIDDEN), any(ErrorMessage.class));

        doThrow(new ChatAlreadyExistsException(1)).when(chatService).createChat(chat);

        chatController.handleCreateChatRequest(chat, channel, responseWriter);
        verify(responseWriter).writeResponse(eq(channel), eq(HttpResponseCode.CONFLICT), any(ErrorMessage.class));

        doThrow(new IllegalParticipantCountException()).when(chatService).createChat(chat);

        chatController.handleCreateChatRequest(chat, channel, responseWriter);
        verify(responseWriter).writeResponse(eq(channel), eq(HttpResponseCode.BAD_REQUEST), any(ErrorMessage.class));
    }

    @Test
    void handleListChatsRequest() {
        final long userId = 1;
        final List<Chat> chats = Arrays.asList(mock(Chat.class), mock(Chat.class));

        final AsynchronousSocketChannel channel = mock(AsynchronousSocketChannel.class);
        final HttpResponseWriter responseWriter = mock(HttpResponseWriter.class);

        when(chatService.getChatsForUser(userId)).thenReturn(chats);

        chatController.handleListChatsRequest(userId, channel, responseWriter);

        verify(responseWriter).writeResponse(channel, HttpResponseCode.OKAY, chats);
    }
}