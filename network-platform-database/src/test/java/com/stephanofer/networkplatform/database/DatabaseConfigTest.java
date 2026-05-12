package com.stephanofer.networkplatform.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class DatabaseConfigTest {

    @Test
    void shouldBuildConfigWithDefaultsAndResolvedHistoryTable() {
        final DatabaseConfig config = DatabaseConfig.mysql()
            .host("127.0.0.1")
            .port(3306)
            .database("network")
            .username("root")
            .password("secret")
            .tablePrefix("practice_")
            .build();

        assertEquals("practice_", config.tablePrefix());
        assertEquals("practice_schema_history", config.resolvedMigrationHistoryTable());
        assertEquals(10, config.pool().maximumPoolSize());
        assertTrue(config.migrations().enabled());
    }

    @Test
    void shouldRejectInvalidTablePrefix() {
        assertThrows(DatabaseModuleConfigException.class, () -> DatabaseConfig.mysql()
            .host("127.0.0.1")
            .database("network")
            .username("root")
            .password("secret")
            .tablePrefix("users; drop table")
            .build());
    }

    @Test
    void shouldRejectExcessivePoolSize() {
        assertThrows(DatabaseModuleConfigException.class, () -> DatabaseConfig.mysql()
            .host("127.0.0.1")
            .database("network")
            .username("root")
            .password("secret")
            .pool(pool -> pool.maximumPoolSize(500))
            .build());
    }

    @Test
    void shouldRejectKeepaliveGreaterThanMaxLifetime() {
        assertThrows(DatabaseModuleConfigException.class, () -> DatabaseConfig.mysql()
            .host("127.0.0.1")
            .database("network")
            .username("root")
            .password("secret")
            .pool(pool -> pool
                .keepaliveTime(Duration.ofMinutes(30))
                .maxLifetime(Duration.ofMinutes(25))
            )
            .build());
    }

    @Test
    void shouldResolveCustomMigrationHistoryTableTemplate() {
        final DatabaseConfig config = DatabaseConfig.mysql()
            .host("127.0.0.1")
            .database("network")
            .username("root")
            .password("secret")
            .tablePrefix("practice_")
            .migrations(migrations -> migrations.historyTable("${prefix}flyway_history"))
            .build();

        assertEquals("practice_flyway_history", config.resolvedMigrationHistoryTable());
    }
}
