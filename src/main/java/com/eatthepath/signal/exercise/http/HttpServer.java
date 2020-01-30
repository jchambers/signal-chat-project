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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An HTTP server listens for new connections, reads HTTP requests, and routes them to appropriate {@link Controller}
 * instances. This HTTP server reserves persistent resources on construction, and must be shut down via its
 * {@link #close()} method to release those resources.
 */
public class HttpServer implements HttpRequestHandler, AutoCloseable {

    private final int port;
    private final int threadCount;

    private final HttpRequestAccumulator requestAccumulator;
    private final HttpResponseWriter responseWriter;

    private final AsynchronousChannelGroup channelGroup;
    private final AsynchronousServerSocketChannel serverSocketChannel;

    private final List<Controller> controllers = Collections.synchronizedList(new ArrayList<>());

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    private static class IOThreadFactory implements ThreadFactory {

        private final AtomicInteger threadCounter = new AtomicInteger(1);

        @Override
        public Thread newThread(final Runnable runnable) {
            return new Thread(runnable, "IOThread-" + threadCounter.getAndIncrement());
        }
    }

    /**
     * Constructs a new HTTP server that listens for new connections on the given port and creates an IO thread pool of
     * a default size.
     *
     * @param port the port on which to listen for incoming connections
     * @throws IOException if the server could not create IO threads or bind to the given port for any reason
     */
    public HttpServer(final int port) throws IOException {
        this(port, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Constructs a new HTTP server that listens for new connections on the given port and creates an IO thread pool of
     * the given size.
     *
     * @param port the port on which to listen for incoming connections
     * @param threadCount the number of threads to include in this server's IO thread pool
     * @throws IOException if the server could not create IO threads or bind to the given port for any reason
     */
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

        channelGroup = AsynchronousChannelGroup.withFixedThreadPool(threadCount, new IOThreadFactory());
        serverSocketChannel = AsynchronousServerSocketChannel.open();
    }

    /**
     * Starts listening for new connections and serving requests.
     *
     * @throws IOException if the server could not bind to its port for any reason
     */
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

    /**
     * Shuts down the server and releases persistent resources.
     */
    @Override
    public void close() {
        log.info("Shutting down.");
        channelGroup.shutdown();
    }

    /**
     * Registers a new controller with this server. When a request reaches the server, the server will check its
     * registered controllers to find one that can handle the request. If multiple controllers could handle the same
     * requests, the controller that will actually handle the request is undefined.
     *
     * @param controller the controller to add to this server
     */
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
