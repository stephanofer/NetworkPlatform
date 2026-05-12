package com.stephanofer.networkplatform.database;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DatabaseMigrationConfigTest {

    @Test
    void shouldRejectInvalidResolvedHistoryTable() {
        final DatabaseMigrationConfig config = new DatabaseMigrationConfig(
            true,
            java.util.List.of("classpath:db/migration"),
            "${prefix}schema-history",
            false,
            true
        );

        assertThrows(DatabaseModuleConfigException.class, () -> config.resolvedHistoryTable("practice_"));
    }
}
