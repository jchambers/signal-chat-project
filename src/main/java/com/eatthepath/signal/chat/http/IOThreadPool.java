package com.eatthepath.signal.chat.http;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicInteger;

class IOThreadPool {

    private final IOThread[] threads;
    private AtomicInteger nextThreadIndex = new AtomicInteger(0);

    /**
     * Creates a pool of threads for non-blocking I/O operations.
     *
     * @param threadCount the number of threads to be included in the pool
     */
    IOThreadPool(final int threadCount) throws IOException {
        if (threadCount < 1) {
            throw new IllegalArgumentException("Thread count must be positive.");
        }

        threads = new IOThread[threadCount];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new IOThread();
        }
    }

    IOThread getNextThread() {
        return threads[nextThreadIndex.getAndIncrement() % threads.length];
    }

    void start() {
        for (final IOThread thread : threads) {
            thread.start();
        }
    }

    void shutDown() {
        for (final IOThread thread : threads) {
            thread.shutDown();;
        }
    }
}
