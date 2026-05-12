package com.stephanofer.networkplatform.paper.command.internal;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.stephanofer.networkplatform.paper.command.CommandService;
import com.stephanofer.networkplatform.paper.command.CommandSpec;
import com.stephanofer.networkplatform.paper.command.RawCommandRegistration;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperCommandService implements CommandService {

    private final Logger logger;
    private final CommandDefinitionRegistry registry;
    private final CompiledCommandNodeFactory nodeFactory;

    public PaperCommandService(final JavaPlugin plugin, final Logger logger) {
        Objects.requireNonNull(plugin, "plugin");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.registry = new CommandDefinitionRegistry();
        this.nodeFactory = new CompiledCommandNodeFactory(new CommandExecutionCoordinator(logger));

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final var registrar = event.registrar();
            for (final CommandSpec spec : this.registry.registeredSpecs()) {
                registrar.register(this.nodeFactory.compile(spec), defaultDescription(spec.description()), spec.aliases());
            }
            for (final RawCommandRegistration registration : this.registry.rawRegistrations()) {
                registrar.register(registration.node(), defaultDescription(registration.description()), registration.aliases());
            }
        });
    }

    @Override
    public void register(final CommandSpec spec) {
        this.registry.registerSpec(spec);
    }

    @Override
    public void registerAll(final Collection<CommandSpec> specs) {
        Objects.requireNonNull(specs, "specs");
        for (final CommandSpec spec : specs) {
            register(spec);
        }
    }

    @Override
    public void registerRaw(final LiteralCommandNode<CommandSourceStack> node) {
        this.registry.registerRaw(RawCommandRegistration.of(node));
    }

    @Override
    public void registerRaw(
        final LiteralCommandNode<CommandSourceStack> node,
        final String description,
        final Collection<String> aliases
    ) {
        this.registry.registerRaw(RawCommandRegistration.of(node, description, aliases));
    }

    @Override
    public boolean isRegistered(final String label) {
        return this.registry.isRegistered(label);
    }

    @Override
    public Optional<CommandSpec> find(final String rootName) {
        return this.registry.find(rootName);
    }

    @Override
    public List<CommandSpec> registeredSpecs() {
        return this.registry.registeredSpecs();
    }

    @Override
    public void refreshPlayerCommands(final Player player) {
        Objects.requireNonNull(player, "player");
        player.updateCommands();
    }

    @Override
    public void refreshPlayerCommands(final Collection<? extends Player> players) {
        Objects.requireNonNull(players, "players");
        for (final Player player : players) {
            if (player != null) {
                player.updateCommands();
            }
        }
    }

    @Override
    public void clear() {
        this.registry.clear();
    }

    private static String defaultDescription(final String description) {
        return description == null ? "" : description;
    }
}
