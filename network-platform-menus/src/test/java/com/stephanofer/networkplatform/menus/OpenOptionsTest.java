package com.stephanofer.networkplatform.menus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class OpenOptionsTest {

    @Test
    void shouldRejectInvalidPage() {
        assertThrows(IllegalArgumentException.class, () -> new OpenOptions(0, true, List.of(), true));
    }

    @Test
    void shouldCreateImmutableArguments() {
        final OpenOptions options = OpenOptions.defaults().args("kits", "ranked");
        assertEquals(List.of("kits", "ranked"), options.arguments());
    }
}
