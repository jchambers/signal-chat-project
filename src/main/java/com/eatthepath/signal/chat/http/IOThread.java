package com.eatthepath.signal.chat.http;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

class IOThread extends Thread {

    private final Selector selector;
    private volatile boolean shouldShutDown = false;

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);

    IOThread() throws IOException {
        super("IOThread-" + THREAD_COUNTER.getAndIncrement());

        selector = Selector.open();
    }

    Selector getSelector() {
        return selector;
    }

    @Override
    public void run() {
        // TODO Add shutdown mechanism
        while (!shouldShutDown) {
            try {
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
