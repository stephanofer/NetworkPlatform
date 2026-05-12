package com.stephanofer.networkplatform.database;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stephanofer.networkplatform.paper.NetworkPlatform;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

class DatabaseModuleTest {

    @Test
    void shouldRejectDuplicateModuleInstallationBeforeConnecting() {
        final JavaPlugin plugin = mock(JavaPlugin.class, RETURNS_DEEP_STUBS);
        when(plugin.getName()).thenReturn("PracticePlugin");
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        when(plugin.getDataFolder()).thenReturn(new File("build/tmp/database-module-test"));

        final NetworkPlatform platform = NetworkPlatform.create(plugin);
        platform.modules().register(() -> DatabaseModule.MODULE_ID);

        final DatabaseConfig config = DatabaseConfig.mysql()
            .host("127.0.0.1")
            .database("network")
            .username("root")
            .password("secret")
            .build();

        assertThrows(IllegalStateException.class, () -> DatabaseModule.install(platform, config));
    }
}
