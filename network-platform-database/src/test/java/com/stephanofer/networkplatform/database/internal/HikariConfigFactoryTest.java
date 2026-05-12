package com.stephanofer.networkplatform.database.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stephanofer.networkplatform.database.DatabaseConfig;
import com.stephanofer.networkplatform.paper.NetworkPlatform;
import com.zaxxer.hikari.HikariConfig;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

class HikariConfigFactoryTest {

    @Test
    void shouldApplyMysqlPerformanceDefaultsAndCustomOverrides() {
        final JavaPlugin plugin = mock(JavaPlugin.class, RETURNS_DEEP_STUBS);
        when(plugin.getName()).thenReturn("PracticePlugin");
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        when(plugin.getDataFolder()).thenReturn(new File("build/tmp/hikari-config-factory-test"));

        final NetworkPlatform platform = NetworkPlatform.create(plugin);
        final DatabaseConfig config = DatabaseConfig.mysql()
            .host("127.0.0.1")
            .database("network")
            .username("root")
            .password("secret")
            .property("connectTimeout", "5000")
            .build();

        final HikariConfig hikari = HikariConfigFactory.create(platform, config);

        assertEquals("jdbc:mysql://127.0.0.1:3306/network", hikari.getJdbcUrl());
        assertEquals("true", hikari.getDataSourceProperties().getProperty("cachePrepStmts"));
        assertEquals("5000", hikari.getDataSourceProperties().getProperty("connectTimeout"));
    }
}
