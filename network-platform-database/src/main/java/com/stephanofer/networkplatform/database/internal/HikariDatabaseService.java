package com.stephanofer.networkplatform.database.internal;

import com.stephanofer.networkplatform.database.DatabaseConfig;
import com.stephanofer.networkplatform.database.DatabaseException;
import com.stephanofer.networkplatform.database.DatabaseModule;
import com.stephanofer.networkplatform.database.DatabaseService;
import com.stephanofer.networkplatform.database.SqlExecutor;
import com.stephanofer.networkplatform.database.SqlTask;
import com.stephanofer.networkplatform.database.TransactionHandle;
import com.stephanofer.networkplatform.paper.NetworkPlatform;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.sql.DataSource;

public final class HikariDatabaseService implements DatabaseService {

    private static final long EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS = 5L;

    private final NetworkPlatform platform;
    private final DatabaseConfig config;
    private final DataSource dataSource;
    private final AutoCloseable dataSourceCloser;
    private final ExecutorService executorService;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    public HikariDatabaseService(
        final NetworkPlatform platform,
        final DatabaseConfig config,
        final DataSource dataSource,
        final AutoCloseable dataSourceCloser,
        final ExecutorService executorService
    ) {
        this.platform = Objects.requireNonNull(platform, "platform");
        this.config = Objects.requireNonNull(config, "config");
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
        this.dataSourceCloser = Objects.requireNonNull(dataSourceCloser, "dataSourceCloser");
        this.executorService = Objects.requireNonNull(executorService, "executorService");
    }

    public HikariDatabaseService(
        final NetworkPlatform platform,
        final DatabaseConfig config,
        final AutoCloseable dataSource,
        final ExecutorService executorService
    ) {
        this(platform, config, (DataSource) dataSource, dataSource, executorService);
    }

    public static ExecutorService createDefaultExecutor(final NetworkPlatform platform, final DatabaseConfig config) {
        final int threads = Math.max(2, Math.min(4, config.pool().maximumPoolSize()));
        return Executors.newFixedThreadPool(threads, new DatabaseThreadFactory(platform.plugin().getName()));
    }

    @Override
    public String id() {
        return DatabaseModule.MODULE_ID;
    }

    @Override
    public DatabaseConfig config() {
        return this.config;
    }

    @Override
    public DataSource dataSource() {
        return this.dataSource;
    }

    @Override
    public Executor executor() {
        return this.executorService;
    }

    @Override
    public boolean isShutdown() {
        return this.shutdown.get();
    }

    @Override
    public void useConnection(final SqlTask task) {
        withConnection(connection -> {
            task.execute(connection);
            return null;
        });
    }

    @Override
    public <T> T withConnection(final SqlExecutor<T> executor) {
        Objects.requireNonNull(executor, "executor");
        ensureActive();
        try (Connection connection = this.dataSource.getConnection()) {
            return executor.execute(connection);
        } catch (final SQLException exception) {
            throw new DatabaseException("Database operation failed", exception);
        } catch (final RuntimeException exception) {
            throw exception;
        } catch (final Exception exception) {
            throw new DatabaseException("Database operation failed", exception);
        }
    }

    @Override
    public void transaction(final SqlTask task) {
        transaction(connection -> {
            task.execute(connection);
            return null;
        });
    }

    @Override
    public <T> T transaction(final SqlExecutor<T> executor) {
        Objects.requireNonNull(executor, "executor");
        ensureActive();
        try (Connection connection = this.dataSource.getConnection()) {
            final boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                final T result = executor.execute(connection);
                connection.commit();
                return result;
            } catch (final Exception exception) {
                rollbackQuietly(connection, exception);
                if (exception instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new DatabaseException("Database transaction failed", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (final SQLException exception) {
            throw new DatabaseException("Database transaction failed", exception);
        }
    }

    @Override
    public CompletableFuture<Void> executeAsync(final SqlTask task) {
        Objects.requireNonNull(task, "task");
        return CompletableFuture.runAsync(() -> useConnection(task), this.executorService);
    }

    @Override
    public <T> CompletableFuture<T> queryAsync(final SqlExecutor<T> executor) {
        Objects.requireNonNull(executor, "executor");
        return CompletableFuture.supplyAsync(() -> withConnection(executor), this.executorService);
    }

    @Override
    public void shutdown() {
        if (!this.shutdown.compareAndSet(false, true)) {
            return;
        }

        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow();
            }
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            this.executorService.shutdownNow();
        }

        try {
            this.dataSourceCloser.close();
        } catch (final Exception exception) {
            this.platform.context().logger().log(Level.SEVERE, "Database shutdown failed", exception);
        }
    }

    @Override
    public void close() {
        shutdown();
    }

    private void ensureActive() {
        this.platform.ensureActive();
        if (isShutdown()) {
            throw new IllegalStateException("DatabaseService is already shut down");
        }
    }

    private static void rollbackQuietly(final Connection connection, final Exception originalException) {
        try {
            connection.rollback();
        } catch (final SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }

    private record DefaultTransactionHandle(Connection connection) implements TransactionHandle {
    }
}
