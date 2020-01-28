package com.eatthepath.signal.chat.http;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * An HTTP request accumulator reads data from channels until it has read a complete HTTP request.
 */
interface HttpRequestAccumulator {
    void accumulateHttpRequest(final AsynchronousSocketChannel channel);
}
