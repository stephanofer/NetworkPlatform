package com.stephanofer.networkplatform.paper.module;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InstalledModuleRegistryTest {

    @Test
    void shouldRegisterAndFindModule() {
        final InstalledModuleRegistry registry = new InstalledModuleRegistry();
        final PlatformModule module = () -> "database";

        registry.register(module);

        assertTrue(registry.isInstalled("DATABASE"));
        assertSame(module, registry.find("database").orElseThrow());
    }

    @Test
    void shouldRejectDuplicateModuleIds() {
        final InstalledModuleRegistry registry = new InstalledModuleRegistry();
        registry.register(() -> "database");

        assertThrows(IllegalStateException.class, () -> registry.register(() -> "DATABASE"));
    }
}
