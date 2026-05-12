package com.stephanofer.networkplatform.database.internal;

import com.stephanofer.networkplatform.database.DatabaseConfig;
import com.stephanofer.networkplatform.database.DatabaseException;
import com.stephanofer.networkplatform.paper.NetworkPlatform;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

public final class FlywayMigrationRunner {

    private FlywayMigrationRunner() {
    }

    public static void migrate(final NetworkPlatform platform, final DatabaseConfig config, final DataSource dataSource) {
        if (!config.migrations().enabled()) {
            return;
        }

        try {
            final Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(config.migrations().locations().toArray(String[]::new))
                .table(config.resolvedMigrationHistoryTable())
                .baselineOnMigrate(config.migrations().baselineOnMigrate())
                .validateOnMigrate(config.migrations().validateOnMigrate())
                .cleanDisabled(true)
                .outOfOrder(false)
                .placeholders(java.util.Map.of("prefix", config.tablePrefix()))
                .load();

            final MigrateResult result = flyway.migrate();
            platform.context().logger().info(
                () -> "Database migrations completed for " + platform.plugin().getName()
                    + ": executed=" + result.migrationsExecuted
                    + ", historyTable=" + config.resolvedMigrationHistoryTable()
            );
        } catch (final RuntimeException exception) {
            throw new DatabaseException(
                "Database migration failed for plugin " + platform.plugin().getName(),
                exception
            );
        }
    }
}
