package com.stephanofer.networkplatform.paper.command;

import com.mojang.brigadier.arguments.ArgumentType;
import java.util.Objects;

public final class CommandArgument<T> {

    private final String name;
    private final Class<T> valueType;
    private final ArgumentType<?> argumentType;
    private final CommandArgumentResolver<T> resolver;
    private CommandSuggestionProvider suggestions;

    CommandArgument(
        final String name,
        final Class<T> valueType,
        final ArgumentType<?> argumentType,
        final CommandArgumentResolver<T> resolver
    ) {
        this.name = requireName(name);
        this.valueType = Objects.requireNonNull(valueType, "valueType");
        this.argumentType = Objects.requireNonNull(argumentType, "argumentType");
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    public String name() {
        return this.name;
    }

    public Class<T> valueType() {
        return this.valueType;
    }

    public ArgumentType<?> argumentType() {
        return this.argumentType;
    }

    public CommandArgumentResolver<T> resolver() {
        return this.resolver;
    }

    public CommandSuggestionProvider suggestions() {
        return this.suggestions;
    }

    public CommandArgument<T> suggestions(final CommandSuggestionProvider suggestions) {
        this.suggestions = Objects.requireNonNull(suggestions, "suggestions");
        return this;
    }

    static String requireName(final String name) {
        Objects.requireNonNull(name, "name");
        final String normalized = name.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("argument name cannot be blank");
        }
        return normalized;
    }
}
