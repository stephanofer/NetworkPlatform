package com.stephanofer.networkplatform.database;

import com.stephanofer.networkplatform.database.internal.TablePrefixValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record DatabaseMigrationConfig(
    boolean enabled,
    List<String> locations,
    String historyTable,
    boolean baselineOnMigrate,
    boolean validateOnMigrate
) {

    private static final String DEFAULT_HISTORY_TABLE = "${prefix}schema_history";

    public DatabaseMigrationConfig {
        locations = normalizeLocations(locations);
        historyTable = normalizeHistoryTable(historyTable);
    }

    public static DatabaseMigrationConfig defaults() {
        return new Builder().build();
    }

    public Builder toBuilder() {
        return new Builder()
            .enabled(this.enabled)
            .locations(this.locations)
            .historyTable(this.historyTable)
            .baselineOnMigrate(this.baselineOnMigrate)
            .validateOnMigrate(this.validateOnMigrate);
    }

    public String resolvedHistoryTable(final String prefix) {
        final String resolvedPrefix = TablePrefixValidator.normalize(prefix);
        final String resolved = this.historyTable.replace("${prefix}", resolvedPrefix).trim();
        if (resolved.isEmpty()) {
            throw new DatabaseModuleConfigException("resolved migration history table cannot be blank");
        }
        if (!resolved.matches("[A-Za-z0-9_]+")) {
            throw new DatabaseModuleConfigException("migration history table contains unsupported characters: " + resolved);
        }
        return resolved;
    }

    public static final class Builder {

        private boolean enabled = true;
        private List<String> locations = List.of("classpath:db/migration");
        private String historyTable = DEFAULT_HISTORY_TABLE;
        private boolean baselineOnMigrate = false;
        private boolean validateOnMigrate = true;

        public Builder enabled(final boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder locations(final String... locations) {
            this.locations = List.of(locations);
            return this;
        }

        public Builder locations(final List<String> locations) {
            this.locations = List.copyOf(locations);
            return this;
        }

        public Builder historyTable(final String historyTable) {
            this.historyTable = historyTable;
            return this;
        }

        public Builder baselineOnMigrate(final boolean baselineOnMigrate) {
            this.baselineOnMigrate = baselineOnMigrate;
            return this;
        }

        public Builder validateOnMigrate(final boolean validateOnMigrate) {
            this.validateOnMigrate = validateOnMigrate;
            return this;
        }

        public DatabaseMigrationConfig build() {
            return new DatabaseMigrationConfig(
                this.enabled,
                this.locations,
                this.historyTable,
                this.baselineOnMigrate,
                this.validateOnMigrate
            );
        }
    }

    private static List<String> normalizeLocations(final List<String> locations) {
        Objects.requireNonNull(locations, "locations");
        final List<String> normalized = new ArrayList<>(locations.size());
        for (final String location : locations) {
            Objects.requireNonNull(location, "migration location");
            final String trimmed = location.trim();
            if (trimmed.isEmpty()) {
                throw new DatabaseModuleConfigException("migration location cannot be blank");
            }
            normalized.add(trimmed);
        }
        if (normalized.isEmpty()) {
            throw new DatabaseModuleConfigException("at least one migration location is required");
        }
        return List.copyOf(normalized);
    }

    private static String normalizeHistoryTable(final String historyTable) {
        Objects.requireNonNull(historyTable, "historyTable");
        final String normalized = historyTable.trim();
        if (normalized.isEmpty()) {
            return DEFAULT_HISTORY_TABLE;
        }
        return normalized;
    }
}
