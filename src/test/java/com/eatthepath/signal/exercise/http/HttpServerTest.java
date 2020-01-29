package com.eatthepath.signal.exercise.http;

import com.eatthepath.signal.exercise.controller.Controller;
import com.eatthepath.signal.exercise.model.ErrorMessage;
import org.junit.jupiter.api.Test;

import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HttpServerTest {

    @Test
    void handleHttpRequest() throws Exception {
        final HttpRequestAccumulator accumulator = mock(HttpRequestAccumulator.class);
        final HttpResponseWriter responseWriter = mock(HttpResponseWriter.class);

        try (final HttpServer httpServer = new HttpServer(8080, 1, accumulator, responseWriter)) {
            final HttpRequest request = mock(HttpRequest.class);
            final AsynchronousSocketChannel channel = mock(AsynchronousSocketChannel.class);

            httpServer.handleHttpRequest(request, channel);

            verify(responseWriter).writeResponse(eq(channel), eq(HttpResponseCode.NOT_FOUND), any(ErrorMessage.class));

            final Controller firstController = mock(Controller.class);
            when(firstController.canHandlePath(any())).thenReturn(false);

            final Controller secondController = mock(Controller.class);
            when(secondController.canHandlePath(any())).thenReturn(true);

            httpServer.registerController(firstController);
            httpServer.registerController(secondController);

            httpServer.handleHttpRequest(request, channel);

            verify(secondController).handleRequest(request, channel, responseWriter);
        }
    }
}