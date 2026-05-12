package com.stephanofer.networkplatform.database;

import com.stephanofer.networkplatform.paper.module.PlatformModule;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.sql.DataSource;

public interface DatabaseService extends PlatformModule, AutoCloseable {

    DatabaseConfig config();

    DataSource dataSource();

    Executor executor();

    boolean isShutdown();

    void useConnection(SqlTask task);

    <T> T withConnection(SqlExecutor<T> executor);

    void transaction(SqlTask task);

    <T> T transaction(SqlExecutor<T> executor);

    CompletableFuture<Void> executeAsync(SqlTask task);

    <T> CompletableFuture<T> queryAsync(SqlExecutor<T> executor);

    void shutdown();

    @Override
    void close();
}
