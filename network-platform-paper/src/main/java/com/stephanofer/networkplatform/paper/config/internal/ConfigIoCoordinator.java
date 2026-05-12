package com.stephanofer.networkplatform.paper.config.internal;

import com.stephanofer.networkplatform.paper.config.ConfigException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public final class ConfigIoCoordinator {

    private final ConcurrentMap<Path, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final ExecutorService executor;

    public ConfigIoCoordinator() {
        final AtomicInteger counter = new AtomicInteger();
        final ThreadFactory factory = runnable -> {
            final Thread thread = new Thread(runnable, "network-platform-config-io-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
        this.executor = Executors.newCachedThreadPool(factory);
    }

    public <T> T execute(final Path path, final ThrowingSupplier<T> action) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(action, "action");
        final ReentrantLock lock = this.locks.computeIfAbsent(path.normalize(), ignored -> new ReentrantLock());
        lock.lock();
        try {
            return action.get();
        } catch (final ConfigException exception) {
            throw exception;
        } catch (final Exception exception) {
            throw new ConfigException("configuration I/O failed for " + path, exception);
        } finally {
            lock.unlock();
        }
    }

    public <T> CompletableFuture<T> executeAsync(final Path path, final ThrowingSupplier<T> action) {
        return CompletableFuture.supplyAsync(() -> execute(path, action), this.executor);
    }

    public void shutdown() {
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(5, TimeUnit.SECONDS)) {
                this.executor.shutdownNow();
            }
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            this.executor.shutdownNow();
        }
        this.locks.clear();
    }
}
