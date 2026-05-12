package com.stephanofer.networkplatform.menus.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.maxlego08.menu.api.MenuPlugin;
import fr.maxlego08.menu.api.command.CommandManager;
import java.util.List;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class MenuArgumentApplierTest {

    @Test
    void shouldMapPositionalAndNamedArgumentsToZMenuCommandArguments() {
        final MenuPlugin menuPlugin = mock(MenuPlugin.class);
        final CommandManager commandManager = mock(CommandManager.class);
        final Player player = mock(Player.class);

        when(menuPlugin.getCommandManager()).thenReturn(commandManager);
        when(menuPlugin.parse(player, "profile")).thenReturn("profile");
        when(menuPlugin.parse(player, "target:Stephanofer")).thenReturn("target:Stephanofer");
        when(menuPlugin.parse(player, "target")).thenReturn("target");
        when(menuPlugin.parse(player, "Stephanofer")).thenReturn("Stephanofer");

        MenuArgumentApplier.apply(menuPlugin, player, List.of("profile", "target:Stephanofer"));

        verify(commandManager).setPlayerArgument(player, "-4", "profile");
        verify(commandManager).setPlayerArgument(player, "target", "Stephanofer");
    }
}
