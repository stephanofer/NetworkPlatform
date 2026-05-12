package com.stephanofer.networkplatform.paper.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record RawCommandRegistration(
    LiteralCommandNode<CommandSourceStack> node,
    String description,
    List<String> aliases
) {

    public RawCommandRegistration {
        Objects.requireNonNull(node, "node");
        aliases = aliases == null ? List.of() : List.copyOf(aliases);
    }

    public static RawCommandRegistration of(final LiteralCommandNode<CommandSourceStack> node) {
        return new RawCommandRegistration(node, "", List.of());
    }

    public static RawCommandRegistration of(
        final LiteralCommandNode<CommandSourceStack> node,
        final String description,
        final Collection<String> aliases
    ) {
        return new RawCommandRegistration(node, description == null ? "" : description, aliases == null ? List.of() : List.copyOf(aliases));
    }
}
