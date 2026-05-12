package com.stephanofer.networkplatform.paper.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Locale;
import java.util.Objects;

public record CommandSuggestionContext(
    CommandContext<CommandSourceStack> brigadier,
    SuggestionsBuilder builder
) {

    public CommandSuggestionContext {
        Objects.requireNonNull(brigadier, "brigadier");
        Objects.requireNonNull(builder, "builder");
    }

    public CommandSourceStack source() {
        return this.brigadier.getSource();
    }

    public String input() {
        return this.builder.getInput();
    }

    public String remaining() {
        return this.builder.getRemaining();
    }

    public String remainingLowerCase() {
        return this.builder.getRemainingLowerCase().toLowerCase(Locale.ROOT);
    }
}
