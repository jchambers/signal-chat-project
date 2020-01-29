package com.eatthepath.signal.exercise.http;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * <p>An HTTP request accumulator reads data from channels until it has read a complete HTTP request.</p>
 *
 * <p>Request accumulation is necessary because there's no guarantee that a complete request will arrive on a channel
 * in a single shot. In fact, for non-trivially large requests, it's virtually guaranteed that requests will span
 * multiple channel read operations and will need to be reassembled.</p>
 */
interface HttpRequestAccumulator {

    /**
     * Reads from the given channel until a complete HTTP request has arrived.
     *
     * @param channel the channel from which to read an HTTP request
     */
    void accumulateHttpRequest(final AsynchronousSocketChannel channel);
}
