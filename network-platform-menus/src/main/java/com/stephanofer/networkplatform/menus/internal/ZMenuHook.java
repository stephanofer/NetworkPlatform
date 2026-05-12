package com.stephanofer.networkplatform.menus.internal;

import com.stephanofer.networkplatform.paper.NetworkPlatform;
import fr.maxlego08.menu.api.ButtonManager;
import fr.maxlego08.menu.api.DialogManager;
import fr.maxlego08.menu.api.InventoryManager;
import fr.maxlego08.menu.api.MenuPlugin;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

final class ZMenuHook {

    private final MenuPlugin menuPlugin;
    private final InventoryManager inventoryManager;
    private final ButtonManager buttonManager;
    private final Optional<DialogManager> dialogManager;

    private ZMenuHook(
        final MenuPlugin menuPlugin,
        final InventoryManager inventoryManager,
        final ButtonManager buttonManager,
        final Optional<DialogManager> dialogManager
    ) {
        this.menuPlugin = menuPlugin;
        this.inventoryManager = inventoryManager;
        this.buttonManager = buttonManager;
        this.dialogManager = dialogManager;
    }

    static ZMenuHook create(final NetworkPlatform platform) {
        Objects.requireNonNull(platform, "platform");
        final Server server = platform.context().server();
        final Logger logger = platform.context().logger();
        final PluginManager pluginManager = server.getPluginManager();
        final Plugin plugin = pluginManager.getPlugin("zMenu");
        if (!(plugin instanceof MenuPlugin menuPlugin)) {
            throw new IllegalStateException("zMenu plugin is not installed or does not expose MenuPlugin");
        }

        if (!plugin.isEnabled()) {
            throw new IllegalStateException("zMenu plugin is installed but not enabled");
        }

        final ServicesManager services = server.getServicesManager();
        final InventoryManager inventoryManager = resolveService(services, InventoryManager.class)
            .orElseGet(menuPlugin::getInventoryManager);
        final ButtonManager buttonManager = resolveService(services, ButtonManager.class)
            .orElseGet(menuPlugin::getButtonManager);
        final Optional<DialogManager> dialogManager = resolveService(services, DialogManager.class)
            .or(() -> Optional.ofNullable(menuPlugin.getDialogManager()));

        if (dialogManager.isEmpty()) {
            logger.fine("zMenu dialogs are unavailable on this runtime or current zMenu configuration");
        }

        return new ZMenuHook(menuPlugin, inventoryManager, buttonManager, dialogManager);
    }

    private static <T> Optional<T> resolveService(final ServicesManager servicesManager, final Class<T> type) {
        final RegisteredServiceProvider<T> registration = servicesManager.getRegistration(type);
        if (registration == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(registration.getProvider());
    }

    MenuPlugin menuPlugin() {
        return this.menuPlugin;
    }

    InventoryManager inventoryManager() {
        return this.inventoryManager;
    }

    ButtonManager buttonManager() {
        return this.buttonManager;
    }

    Optional<DialogManager> dialogManager() {
        return this.dialogManager;
    }

    boolean available() {
        return this.menuPlugin.isEnabled();
    }
}
