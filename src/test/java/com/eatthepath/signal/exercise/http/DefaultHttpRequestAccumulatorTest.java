package com.eatthepath.signal.exercise.http;

import com.eatthepath.signal.exercise.model.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class DefaultHttpRequestAccumulatorTest {

    @Mock
    private HttpRequestParser parser;

    @Mock
    private HttpResponseWriter responseWriter;

    @Mock
    private HttpRequestHandler handler;

    private AsynchronousSocketChannel channel;

    @InjectMocks
    private DefaultHttpRequestAccumulator accumulator;

    private static final String BUFFER_CONTENTS = "Hello!";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        channel = mock(AsynchronousSocketChannel.class);

        //noinspection unchecked
        doAnswer(invocation -> {
            final ByteBuffer byteBuffer = invocation.getArgument(0, ByteBuffer.class);
            byteBuffer.put(BUFFER_CONTENTS.getBytes());

            //noinspection unchecked
            invocation.getArgument(2, CompletionHandler.class).completed(BUFFER_CONTENTS.getBytes().length, null);

            return null;
        }).when(channel).read(any(ByteBuffer.class), isNull(), any(CompletionHandler.class));
    }

    @Test
    void accumulateHttpRequestCompleteRequest() throws Exception {
        final HttpRequest request = mock(HttpRequest.class);
        when(parser.parseHttpRequest(any(ByteBuffer.class))).thenReturn(request);

        accumulator.accumulateHttpRequest(channel);

        verify(handler).handleHttpRequest(request, channel);
    }

    @Test
    void accumulateHttpRequestMultipleReads() throws Exception {
        final HttpRequest request = mock(HttpRequest.class);

        when(parser.parseHttpRequest(any(ByteBuffer.class))).thenAnswer(new Answer<HttpRequest>() {
            private int invocations = 0;

            @Override
            public HttpRequest answer(final InvocationOnMock invocationOnMock) throws Throwable {
                if (invocations++ < 1) {
                    throw new IncompleteHttpRequestException();
                } else {
                    return request;
                }
            }
        });

        accumulator.accumulateHttpRequest(channel);
        verify(handler).handleHttpRequest(request, channel);
    }

    @Test
    void accumulateHttpRequestBadRequest() throws Exception {
        when(parser.parseHttpRequest(any(ByteBuffer.class))).thenThrow(new InvalidHttpRequestException("OH NO"));

        accumulator.accumulateHttpRequest(channel);

        verify(responseWriter).writeResponse(eq(channel), eq(HttpResponseCode.BAD_REQUEST), any(ErrorMessage.class));
    }
}