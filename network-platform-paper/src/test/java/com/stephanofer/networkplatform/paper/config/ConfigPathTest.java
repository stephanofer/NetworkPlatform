package com.stephanofer.networkplatform.paper.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ConfigPathTest {

    @Test
    void shouldNormalizeNestedPaths() {
        assertEquals("modules/player/config.yml", ConfigPath.of("modules\\player/./config.yml").value());
    }

    @Test
    void shouldRejectPathTraversal() {
        assertThrows(IllegalArgumentException.class, () -> ConfigPath.of("../secret.yml"));
    }
}
