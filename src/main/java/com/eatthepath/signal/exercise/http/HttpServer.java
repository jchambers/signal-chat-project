package com.eatthepath.signal.exercise.http;

import com.eatthepath.signal.exercise.controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class HttpServer implements HttpRequestHandler {

    private final int port;

    private final HttpRequestAccumulator requestAccumulator;
    private final HttpResponseWriter responseWriter;

    private final AsynchronousChannelGroup channelGroup;
    private final AsynchronousServerSocketChannel serverSocketChannel;

    private final List<Controller> controllers = Collections.synchronizedList(new ArrayList<>());

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    public HttpServer(final int port) throws IOException {
        this(port, null, new DefaultHttpResponseWriter());
    }

    // Visible for testing
    HttpServer(final int port,
               final HttpRequestAccumulator requestAccumulator,
               final HttpResponseWriter responseWriter) throws IOException {

        this.port = port;

        // This is pretty gross, but it's a hacky way to resolve "can't reference this before calling supertype
        // constructor" issues.
        this.requestAccumulator = requestAccumulator != null ? requestAccumulator : new DefaultHttpRequestAccumulator(this);

        this.responseWriter = responseWriter;

        channelGroup = AsynchronousChannelGroup.withFixedThreadPool(4, Executors.defaultThreadFactory());
        serverSocketChannel = AsynchronousServerSocketChannel.open();
    }

    public void start() throws IOException {
        serverSocketChannel.bind(new InetSocketAddress(port));

        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

            @Override
            public void completed(final AsynchronousSocketChannel channel, final Void attachment) {
                log.trace("Accepted channel: {}", channel);

                requestAccumulator.accumulateHttpRequest(channel);

                // Accept the next incoming channel
                serverSocketChannel.accept(null, this);
            }

            @Override
            public void failed(final Throwable throwable, final Void attachment) {
                log.error("Failed to accept channel", throwable);
            }
        });
    }

    private void shutDown() {
        channelGroup.shutdown();
    }

    void registerController(final Controller controller) {
        controllers.add(controller);
    }

    @Override
    public void handleHttpRequest(final HttpRequest request, final AsynchronousSocketChannel channel) {
        boolean handled = false;

        for (final Controller controller : controllers) {
            if (controller.canHandlePath(request.getPath())) {
                if (controller.canHandleRequestMethod(request.getRequestMethod())) {
                    controller.handleRequest(request, channel, responseWriter);
                } else {
                    responseWriter.writeResponse(HttpResponseCode.METHOD_NOT_ALLOWED,
                            new Error("Controller at path does not support " + request.getRequestMethod()));
                }

                handled = true;
                break;
            }
        }

        if (!handled) {
            responseWriter.writeResponse(HttpResponseCode.NOT_FOUND, new Error("No controller found for given path"));
        }
    }
}
