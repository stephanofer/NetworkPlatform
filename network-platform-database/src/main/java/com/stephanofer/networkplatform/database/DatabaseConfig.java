package com.stephanofer.networkplatform.database;

import com.stephanofer.networkplatform.database.internal.TablePrefixValidator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public record DatabaseConfig(
    String host,
    int port,
    String database,
    String username,
    String password,
    String tablePrefix,
    DatabasePoolConfig pool,
    DatabaseMigrationConfig migrations,
    Map<String, String> properties
) {

    public DatabaseConfig {
        host = requireText(host, "host");
        database = requireText(database, "database");
        username = requireText(username, "username");
        password = Objects.requireNonNull(password, "password");
        tablePrefix = TablePrefixValidator.normalize(tablePrefix);
        pool = Objects.requireNonNull(pool, "pool");
        migrations = Objects.requireNonNull(migrations, "migrations");
        properties = normalizeProperties(properties);

        if (port < 1 || port > 65_535) {
            throw new DatabaseModuleConfigException("port must be between 1 and 65535");
        }
    }

    public static Builder mysql() {
        return new Builder();
    }

    public String resolvedMigrationHistoryTable() {
        return this.migrations.resolvedHistoryTable(this.tablePrefix);
    }

    public Builder toBuilder() {
        return new Builder()
            .host(this.host)
            .port(this.port)
            .database(this.database)
            .username(this.username)
            .password(this.password)
            .tablePrefix(this.tablePrefix)
            .pool(builder -> Builder.copy(this.pool, builder))
            .migrations(builder -> Builder.copy(this.migrations, builder))
            .properties(this.properties);
    }

    public static final class Builder {

        private String host = "127.0.0.1";
        private int port = 3306;
        private String database = "";
        private String username = "";
        private String password = "";
        private String tablePrefix = "";
        private DatabasePoolConfig pool = DatabasePoolConfig.defaults();
        private DatabaseMigrationConfig migrations = DatabaseMigrationConfig.defaults();
        private final Map<String, String> properties = new LinkedHashMap<>();

        public Builder host(final String host) {
            this.host = host;
            return this;
        }

        public Builder port(final int port) {
            this.port = port;
            return this;
        }

        public Builder database(final String database) {
            this.database = database;
            return this;
        }

        public Builder username(final String username) {
            this.username = username;
            return this;
        }

        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        public Builder tablePrefix(final String tablePrefix) {
            this.tablePrefix = tablePrefix;
            return this;
        }

        public Builder pool(final Consumer<DatabasePoolConfig.Builder> consumer) {
            final DatabasePoolConfig.Builder builder = this.pool.toBuilder();
            consumer.accept(builder);
            this.pool = builder.build();
            return this;
        }

        public Builder migrations(final Consumer<DatabaseMigrationConfig.Builder> consumer) {
            final DatabaseMigrationConfig.Builder builder = this.migrations.toBuilder();
            consumer.accept(builder);
            this.migrations = builder.build();
            return this;
        }

        public Builder property(final String key, final String value) {
            this.properties.put(key, value);
            return this;
        }

        public Builder properties(final Map<String, String> properties) {
            this.properties.clear();
            this.properties.putAll(properties);
            return this;
        }

        public DatabaseConfig build() {
            return new DatabaseConfig(
                this.host,
                this.port,
                this.database,
                this.username,
                this.password,
                this.tablePrefix,
                this.pool,
                this.migrations,
                this.properties
            );
        }

        private static void copy(final DatabasePoolConfig source, final DatabasePoolConfig.Builder target) {
            target.maximumPoolSize(source.maximumPoolSize());
            target.minimumIdle(source.minimumIdle());
            target.connectionTimeout(source.connectionTimeout());
            target.validationTimeout(source.validationTimeout());
            target.idleTimeout(source.idleTimeout());
            target.maxLifetime(source.maxLifetime());
            target.keepaliveTime(source.keepaliveTime());
            target.leakDetectionThreshold(source.leakDetectionThreshold());
        }

        private static void copy(final DatabaseMigrationConfig source, final DatabaseMigrationConfig.Builder target) {
            target.enabled(source.enabled());
            target.locations(source.locations());
            target.historyTable(source.historyTable());
            target.baselineOnMigrate(source.baselineOnMigrate());
            target.validateOnMigrate(source.validateOnMigrate());
        }
    }

    private static String requireText(final String value, final String name) {
        Objects.requireNonNull(value, name);
        final String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new DatabaseModuleConfigException(name + " cannot be blank");
        }
        return normalized;
    }

    private static Map<String, String> normalizeProperties(final Map<String, String> properties) {
        Objects.requireNonNull(properties, "properties");
        final Map<String, String> normalized = new LinkedHashMap<>();
        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            final String key = requireText(entry.getKey(), "property key");
            final String value = Objects.requireNonNull(entry.getValue(), "property value").trim();
            normalized.put(key, value);
        }
        return Map.copyOf(normalized);
    }
}
