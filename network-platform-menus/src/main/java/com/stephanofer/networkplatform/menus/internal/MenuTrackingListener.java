package com.stephanofer.networkplatform.menus.internal;

import fr.maxlego08.menu.api.event.events.PlayerOpenInventoryEvent;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

final class MenuTrackingListener implements Listener {

    private final Plugin consumerPlugin;
    private final Set<UUID> openMenus;

    MenuTrackingListener(final Plugin consumerPlugin, final Set<UUID> openMenus) {
        this.consumerPlugin = consumerPlugin;
        this.openMenus = openMenus;
    }

    @EventHandler
    void onMenuOpen(final PlayerOpenInventoryEvent event) {
        if (event.getInventory().getPlugin().equals(this.consumerPlugin)) {
            this.openMenus.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    void onInventoryClose(final InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            this.openMenus.remove(player.getUniqueId());
        }
    }

    @EventHandler
    void onPlayerQuit(final PlayerQuitEvent event) {
        this.openMenus.remove(event.getPlayer().getUniqueId());
    }
}
