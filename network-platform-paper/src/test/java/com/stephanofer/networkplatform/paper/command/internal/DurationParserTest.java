package com.stephanofer.networkplatform.paper.command.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class DurationParserTest {

    @Test
    void shouldParseSupportedUnits() throws CommandSyntaxException {
        assertEquals(Duration.ofSeconds(30), DurationParser.parse("30s"));
        assertEquals(Duration.ofMinutes(5), DurationParser.parse("5m"));
        assertEquals(Duration.ofHours(2), DurationParser.parse("2h"));
        assertEquals(Duration.ofDays(7), DurationParser.parse("7d"));
    }

    @Test
    void shouldRejectInvalidInput() {
        assertThrows(CommandSyntaxException.class, () -> DurationParser.parse("hello"));
        assertThrows(CommandSyntaxException.class, () -> DurationParser.parse("10x"));
    }
}
