package com.stephanofer.networkplatform.database.internal;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class DatabaseThreadFactory implements ThreadFactory {

    private final String namePrefix;
    private final AtomicInteger sequence = new AtomicInteger(1);

    public DatabaseThreadFactory(final String pluginName) {
        final String normalized = Objects.requireNonNull(pluginName, "pluginName").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("pluginName cannot be blank");
        }
        this.namePrefix = normalized + "-network-platform-db-";
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        final Thread thread = new Thread(runnable, this.namePrefix + this.sequence.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    }
}
