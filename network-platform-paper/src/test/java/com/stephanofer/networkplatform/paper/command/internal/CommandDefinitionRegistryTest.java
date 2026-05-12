package com.stephanofer.networkplatform.paper.command.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.stephanofer.networkplatform.paper.command.CommandSpec;
import com.stephanofer.networkplatform.paper.command.RawCommandRegistration;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.junit.jupiter.api.Test;

class CommandDefinitionRegistryTest {

    @Test
    void shouldTrackLabelsCaseInsensitively() {
        final CommandDefinitionRegistry registry = new CommandDefinitionRegistry();
        registry.registerSpec(CommandSpec.builder("party").aliases("p").build());

        assertTrue(registry.isRegistered("PARTY"));
        assertTrue(registry.isRegistered("P"));
        assertEquals("party", registry.find("party").orElseThrow().name());
    }

    @Test
    void shouldRejectDuplicateAliasAcrossDefinitions() {
        final CommandDefinitionRegistry registry = new CommandDefinitionRegistry();
        registry.registerSpec(CommandSpec.builder("party").aliases("p").build());

        assertThrows(IllegalStateException.class, () ->
            registry.registerSpec(CommandSpec.builder("profiles").aliases("P").build())
        );
    }

    @Test
    void shouldRejectRawRegistrationWithDuplicateRoot() {
        final CommandDefinitionRegistry registry = new CommandDefinitionRegistry();
        registry.registerSpec(CommandSpec.builder("party").build());

        final var raw = RawCommandRegistration.of(Commands.literal("party").build());
        assertThrows(IllegalStateException.class, () -> registry.registerRaw(raw));
    }
}
