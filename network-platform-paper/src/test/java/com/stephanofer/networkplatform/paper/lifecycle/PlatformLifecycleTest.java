package com.stephanofer.networkplatform.paper.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class PlatformLifecycleTest {

    @Test
    void shouldRunShutdownActionsInReverseRegistrationOrder() {
        final PlatformLifecycle lifecycle = new PlatformLifecycle(Logger.getLogger("test"));
        final List<String> calls = new ArrayList<>();

        lifecycle.onShutdown(() -> calls.add("database"));
        lifecycle.onShutdown(() -> calls.add("redis"));
        lifecycle.onShutdown(() -> calls.add("scoreboards"));

        lifecycle.shutdown();

        assertEquals(List.of("scoreboards", "redis", "database"), calls);
    }

    @Test
    void shouldRejectNewActionsAfterShutdown() {
        final PlatformLifecycle lifecycle = new PlatformLifecycle(Logger.getLogger("test"));
        lifecycle.shutdown();

        assertThrows(IllegalStateException.class, () -> lifecycle.onShutdown(() -> {}));
    }
}
