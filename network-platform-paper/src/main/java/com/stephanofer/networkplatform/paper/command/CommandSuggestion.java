package com.stephanofer.networkplatform.paper.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import java.util.Objects;

public record CommandSuggestion(String value, Message tooltip) {

    public CommandSuggestion {
        Objects.requireNonNull(value, "value");
    }

    public static CommandSuggestion of(final String value) {
        return new CommandSuggestion(value, null);
    }

    public static CommandSuggestion of(final String value, final String tooltip) {
        Objects.requireNonNull(tooltip, "tooltip");
        return new CommandSuggestion(value, new LiteralMessage(tooltip));
    }
}
