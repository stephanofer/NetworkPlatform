package com.stephanofer.networkplatform.paper.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.bukkit.entity.Player;

/** Registers declarative command specs and optional raw Brigadier nodes. */
public interface CommandService {

    void register(CommandSpec spec);

    void registerAll(Collection<CommandSpec> specs);

    void registerRaw(LiteralCommandNode<CommandSourceStack> node);

    void registerRaw(LiteralCommandNode<CommandSourceStack> node, String description, Collection<String> aliases);

    boolean isRegistered(String label);

    Optional<CommandSpec> find(String rootName);

    List<CommandSpec> registeredSpecs();

    void refreshPlayerCommands(Player player);

    void refreshPlayerCommands(Collection<? extends Player> players);

    void clear();
}
