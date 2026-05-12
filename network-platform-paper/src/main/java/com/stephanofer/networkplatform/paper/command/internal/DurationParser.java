package com.stephanofer.networkplatform.paper.command.internal;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.time.Duration;

public final class DurationParser {

    private static final SimpleCommandExceptionType INVALID_DURATION = new SimpleCommandExceptionType(
        new LiteralMessage("Invalid duration. Use formats like 30s, 5m, 2h, 7d.")
    );

    private DurationParser() {
    }

    public static Duration parse(final String input) throws CommandSyntaxException {
        if (input == null || input.isBlank() || input.length() < 2) {
            throw INVALID_DURATION.create();
        }

        final String numberPart = input.substring(0, input.length() - 1);
        final char unit = Character.toLowerCase(input.charAt(input.length() - 1));
        final long amount;
        try {
            amount = Long.parseLong(numberPart);
        } catch (final NumberFormatException exception) {
            throw INVALID_DURATION.create();
        }

        if (amount < 0) {
            throw INVALID_DURATION.create();
        }

        return switch (unit) {
            case 's' -> Duration.ofSeconds(amount);
            case 'm' -> Duration.ofMinutes(amount);
            case 'h' -> Duration.ofHours(amount);
            case 'd' -> Duration.ofDays(amount);
            default -> throw INVALID_DURATION.create();
        };
    }
}
