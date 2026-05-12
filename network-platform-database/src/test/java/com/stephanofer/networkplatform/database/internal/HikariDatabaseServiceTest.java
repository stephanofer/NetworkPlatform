package com.stephanofer.networkplatform.database.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stephanofer.networkplatform.database.DatabaseConfig;
import com.stephanofer.networkplatform.paper.NetworkPlatform;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

class HikariDatabaseServiceTest {

    @Test
    void shouldRejectQueriesAfterShutdown() {
        final TestFixture fixture = createFixture();
        fixture.service.shutdown();

        assertThrows(IllegalStateException.class, () -> fixture.service.withConnection(connection -> null));
        assertTrue(fixture.closed.get());
    }

    @Test
    void shouldExecuteTransactionAndRestoreConnection() {
        final TestFixture fixture = createFixture();

        final String result = fixture.service.transaction(connection -> {
            connection.createStatement();
            return "ok";
        });

        assertEquals("ok", result);
    }

    private static TestFixture createFixture() {
        final JavaPlugin plugin = mock(JavaPlugin.class, RETURNS_DEEP_STUBS);
        when(plugin.getName()).thenReturn("PracticePlugin");
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        when(plugin.getDataFolder()).thenReturn(new File("build/tmp/database-service-test"));

        final NetworkPlatform platform = NetworkPlatform.create(plugin);
        final DatabaseConfig config = DatabaseConfig.mysql()
            .host("127.0.0.1")
            .database("network")
            .username("root")
            .password("secret")
            .build();

        final Connection connection = mock(Connection.class);
        try {
            when(connection.getAutoCommit()).thenReturn(true);
        } catch (final SQLException exception) {
            throw new RuntimeException(exception);
        }

        final DataSource dataSource = mock(DataSource.class);
        try {
            when(dataSource.getConnection()).thenReturn(connection);
        } catch (final SQLException exception) {
            throw new RuntimeException(exception);
        }

        final AtomicBoolean closed = new AtomicBoolean(false);
        final AutoCloseable closeable = () -> closed.set(true);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final HikariDatabaseService service = new HikariDatabaseService(platform, config, dataSource, closeable, executor);
        return new TestFixture(service, closed);
    }

    private record TestFixture(HikariDatabaseService service, AtomicBoolean closed) {
    }
}
