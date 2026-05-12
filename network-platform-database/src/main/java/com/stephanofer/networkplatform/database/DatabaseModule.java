package com.stephanofer.networkplatform.database;

import com.stephanofer.networkplatform.database.internal.FlywayMigrationRunner;
import com.stephanofer.networkplatform.database.internal.HikariConfigFactory;
import com.stephanofer.networkplatform.database.internal.HikariDatabaseService;
import com.stephanofer.networkplatform.paper.NetworkPlatform;
import com.stephanofer.networkplatform.paper.module.PlatformModule;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public final class DatabaseModule {

    public static final String MODULE_ID = "database";

    private DatabaseModule() {
    }

    public static DatabaseService install(final NetworkPlatform platform, final DatabaseConfig config) {
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(config, "config");
        platform.ensureActive();

        final PlatformModule existing = platform.modules().find(MODULE_ID).orElse(null);
        if (existing != null) {
            throw new IllegalStateException("NetworkPlatform module already installed: " + MODULE_ID);
        }

        final HikariDataSource dataSource = createDataSource(platform, config);
        final ExecutorService executor = HikariDatabaseService.createDefaultExecutor(platform, config);
        final HikariDatabaseService service = new HikariDatabaseService(platform, config, dataSource, executor);

        try {
            FlywayMigrationRunner.migrate(platform, config, dataSource);
            platform.modules().register(service);
            platform.lifecycle().onShutdown(service::shutdown);
            return service;
        } catch (final Throwable throwable) {
            service.shutdown();
            throw throwable;
        }
    }

    private static HikariDataSource createDataSource(final NetworkPlatform platform, final DatabaseConfig config) {
        try {
            return new HikariDataSource(HikariConfigFactory.create(platform, config));
        } catch (final RuntimeException exception) {
            throw new DatabaseException(
                "Could not initialize MySQL pool for plugin " + platform.plugin().getName(),
                exception
            );
        }
    }
}
