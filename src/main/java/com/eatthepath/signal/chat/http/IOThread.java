package com.eatthepath.signal.chat.http;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.channels.*;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

class IOThread extends Thread {

    private final Selector selector;
    private volatile boolean shouldShutDown = false;

    private final Queue<Pair<SelectableChannel, Integer>> registrationQueue = new ArrayDeque<>();

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);

    IOThread() throws IOException {
        super("IOThread-" + THREAD_COUNTER.getAndIncrement());

        selector = Selector.open();
    }

    void register(final SelectableChannel channel, final int interestSet) {
        synchronized (registrationQueue) {
            registrationQueue.add(Pair.of(channel, interestSet));
        }

        selector.wakeup();
    }

    @Override
    public void run() {
        // TODO Add shutdown mechanism
        while (!shouldShutDown) {
            try {
                synchronized (registrationQueue) {
                    while (!registrationQueue.isEmpty()) {
                        final Pair<SelectableChannel, Integer> channelAndInterestSet = registrationQueue.remove();

                        final SelectableChannel channel = channelAndInterestSet.getLeft();
                        final int interestSet = channelAndInterestSet.getRight();

                        channel.register(selector, interestSet);
                    }
                }

                selector.select();

                for (final SelectionKey key : selector.selectedKeys()) {
                    if (key.isAcceptable()) {
                        ((ServerSocketChannel) key.channel()).accept();
                    }

                    if (key.isReadable()) {
                        // TODO
                    }

                    if (key.isWritable()) {
                        // TODO
                    }
                }
            } catch (final IOException e) {
                // TODO
                e.printStackTrace();
            }
        }
    }

    void shutDown() {
        shouldShutDown = true;
        selector.wakeup();
    }
}
