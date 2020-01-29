package com.eatthepath.signal.exercise.http;

import com.eatthepath.signal.exercise.controller.Controller;
import com.eatthepath.signal.exercise.model.ErrorMessage;
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

public class HttpServer implements HttpRequestHandler, AutoCloseable {

    private final int port;
    private final int threadCount;

    private final HttpRequestAccumulator requestAccumulator;
    private final HttpResponseWriter responseWriter;

    private final AsynchronousChannelGroup channelGroup;
    private final AsynchronousServerSocketChannel serverSocketChannel;

    private final List<Controller> controllers = Collections.synchronizedList(new ArrayList<>());

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    public HttpServer(final int port) throws IOException {
        this(port, Runtime.getRuntime().availableProcessors());
    }

    public HttpServer(final int port, final int threadCount) throws IOException {
        this(port, threadCount, null, new DefaultHttpResponseWriter());
    }

    // Visible for testing
    HttpServer(final int port,
               final int threadCount,
               final HttpRequestAccumulator requestAccumulator,
               final HttpResponseWriter responseWriter) throws IOException {

        this.port = port;
        this.threadCount = threadCount;

        // This is pretty gross, but it's a hacky way to resolve "can't reference this before calling supertype
        // constructor" issues.
        this.requestAccumulator = requestAccumulator != null ? requestAccumulator : new DefaultHttpRequestAccumulator(this);

        this.responseWriter = responseWriter;

        channelGroup = AsynchronousChannelGroup.withFixedThreadPool(threadCount, Executors.defaultThreadFactory());
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

        log.info("Started server on port {} with {} IO threads.", port, threadCount);
    }

    @Override
    public void close() {
        log.info("Shutting down.");
        channelGroup.shutdown();
    }

    public void registerController(final Controller controller) {
        controllers.add(controller);
    }

    @Override
    public void handleHttpRequest(final HttpRequest request, final AsynchronousSocketChannel channel) {
        log.debug("Received request: {} {}", request.getRequestMethod(), request.getPath());

        boolean handled = false;

        for (final Controller controller : controllers) {
            if (controller.canHandlePath(request.getPath())) {
                controller.handleRequest(request, channel, responseWriter);
                handled = true;

                break;
            }
        }

        if (!handled) {
            responseWriter.writeResponse(channel, HttpResponseCode.NOT_FOUND, new ErrorMessage("No controller found for given path"));
        }
    }
}
