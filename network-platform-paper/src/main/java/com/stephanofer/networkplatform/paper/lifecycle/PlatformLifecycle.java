package com.stephanofer.networkplatform.paper.lifecycle;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlatformLifecycle implements Lifecycle {

    private final Logger logger;
    private final Deque<Runnable> shutdownActions;
    private final AtomicBoolean shutdown;

    public PlatformLifecycle(final Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.shutdownActions = new ArrayDeque<>();
        this.shutdown = new AtomicBoolean(false);
    }

    @Override
    public synchronized void onShutdown(final Runnable action) {
        Objects.requireNonNull(action, "action");
        if (this.shutdown.get()) {
            throw new IllegalStateException("Cannot register shutdown action after lifecycle shutdown");
        }

        this.shutdownActions.addLast(action);
    }

    @Override
    public void shutdown() {
        if (!this.shutdown.compareAndSet(false, true)) {
            return;
        }

        while (true) {
            final Runnable action;
            synchronized (this) {
                action = this.shutdownActions.pollLast();
            }

            if (action == null) {
                return;
            }

            try {
                action.run();
            } catch (final Throwable throwable) {
                this.logger.log(Level.SEVERE, "NetworkPlatform shutdown action failed", throwable);
            }
        }
    }
}
