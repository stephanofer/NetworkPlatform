package com.stephanofer.networkplatform.database.internal;

import com.stephanofer.networkplatform.database.DatabaseConfig;
import com.stephanofer.networkplatform.database.DatabasePoolConfig;
import com.stephanofer.networkplatform.paper.NetworkPlatform;
import com.zaxxer.hikari.HikariConfig;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HikariConfigFactory {

    private HikariConfigFactory() {
    }

    public static HikariConfig create(final NetworkPlatform platform, final DatabaseConfig config) {
        final HikariConfig hikari = new HikariConfig();
        final DatabasePoolConfig pool = config.pool();

        hikari.setPoolName(platform.plugin().getName() + "-MySQL");
        hikari.setJdbcUrl("jdbc:mysql://" + config.host() + ':' + config.port() + '/' + config.database());
        hikari.setUsername(config.username());
        hikari.setPassword(config.password());
        hikari.setMaximumPoolSize(pool.maximumPoolSize());
        hikari.setMinimumIdle(pool.minimumIdle());
        hikari.setConnectionTimeout(pool.connectionTimeout().toMillis());
        hikari.setValidationTimeout(pool.validationTimeout().toMillis());
        hikari.setIdleTimeout(pool.idleTimeout().toMillis());
        hikari.setMaxLifetime(pool.maxLifetime().toMillis());
        hikari.setKeepaliveTime(pool.keepaliveTime().toMillis());
        hikari.setAutoCommit(true);

        if (!pool.leakDetectionThreshold().isZero()) {
            hikari.setLeakDetectionThreshold(pool.leakDetectionThreshold().toMillis());
        }

        for (final Map.Entry<String, String> entry : defaultDataSourceProperties().entrySet()) {
            hikari.addDataSourceProperty(entry.getKey(), entry.getValue());
        }
        for (final Map.Entry<String, String> entry : config.properties().entrySet()) {
            hikari.addDataSourceProperty(entry.getKey(), entry.getValue());
        }

        return hikari;
    }

    private static Map<String, String> defaultDataSourceProperties() {
        final Map<String, String> properties = new LinkedHashMap<>();
        properties.put("cachePrepStmts", "true");
        properties.put("prepStmtCacheSize", "250");
        properties.put("prepStmtCacheSqlLimit", "2048");
        properties.put("useServerPrepStmts", "true");
        properties.put("useLocalSessionState", "true");
        properties.put("rewriteBatchedStatements", "true");
        properties.put("cacheResultSetMetadata", "true");
        properties.put("cacheServerConfiguration", "true");
        properties.put("elideSetAutoCommits", "true");
        properties.put("maintainTimeStats", "false");
        properties.put("connectTimeout", "10000");
        properties.put("socketTimeout", "30000");
        properties.put("tcpKeepAlive", "true");
        return properties;
    }
}
