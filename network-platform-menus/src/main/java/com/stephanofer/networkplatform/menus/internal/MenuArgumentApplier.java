package com.stephanofer.networkplatform.menus.internal;

import fr.maxlego08.menu.api.MenuPlugin;
import fr.maxlego08.menu.api.command.CommandManager;
import java.util.List;
import java.util.Objects;
import org.bukkit.entity.Player;

final class MenuArgumentApplier {

    private MenuArgumentApplier() {
    }

    static void apply(final MenuPlugin menuPlugin, final Player player, final List<String> arguments) {
        Objects.requireNonNull(menuPlugin, "menuPlugin");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(arguments, "arguments");

        if (arguments.isEmpty()) {
            return;
        }

        final CommandManager commandManager = menuPlugin.getCommandManager();
        for (int index = 0; index < arguments.size(); index++) {
            String key = String.valueOf(index - 4);
            String value = menuPlugin.parse(player, arguments.get(index));
            if (value.contains(":")) {
                final String[] values = value.split(":", 2);
                key = menuPlugin.parse(player, values[0]);
                value = menuPlugin.parse(player, values[1]);
            }

            commandManager.setPlayerArgument(player, key, value);
        }
    }
}
