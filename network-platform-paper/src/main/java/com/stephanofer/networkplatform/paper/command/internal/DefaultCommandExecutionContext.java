package com.stephanofer.networkplatform.paper.command.internal;

import com.mojang.brigadier.context.CommandContext;
import com.stephanofer.networkplatform.paper.command.CommandExecutionContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Map;
import java.util.Objects;

final class DefaultCommandExecutionContext implements CommandExecutionContext {

    private final CommandContext<CommandSourceStack> brigadier;
    private final Map<String, Object> arguments;

    DefaultCommandExecutionContext(
        final CommandContext<CommandSourceStack> brigadier,
        final Map<String, Object> arguments
    ) {
        this.brigadier = Objects.requireNonNull(brigadier, "brigadier");
        this.arguments = Map.copyOf(Objects.requireNonNull(arguments, "arguments"));
    }

    @Override
    public CommandContext<CommandSourceStack> brigadier() {
        return this.brigadier;
    }

    @Override
    public CommandSourceStack source() {
        return this.brigadier.getSource();
    }

    @Override
    public <T> T argument(final String name, final Class<T> type) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        final Object value = this.arguments.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Unknown argument: " + name);
        }
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException("Argument '" + name + "' is not of type " + type.getName());
        }
        return type.cast(value);
    }

    @Override
    public boolean hasArgument(final String name) {
        return this.arguments.containsKey(name);
    }
}
