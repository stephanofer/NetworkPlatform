package com.stephanofer.networkplatform.menus.internal;

import com.stephanofer.networkplatform.menus.LoadedMenus;
import com.stephanofer.networkplatform.menus.MenuKey;
import com.stephanofer.networkplatform.menus.MenuLoader;
import com.stephanofer.networkplatform.menus.MenuRegistry;
import fr.maxlego08.menu.api.Inventory;
import fr.maxlego08.menu.api.InventoryManager;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.plugin.Plugin;

final class ZMenuMenuRegistry implements MenuRegistry {

    private final Plugin plugin;
    private final InventoryManager inventoryManager;
    private final MenuLoader loader;

    ZMenuMenuRegistry(final Plugin plugin, final InventoryManager inventoryManager, final MenuLoader loader) {
        this.plugin = plugin;
        this.inventoryManager = inventoryManager;
        this.loader = loader;
    }

    @Override
    public Optional<Inventory> find(final MenuKey key) {
        return this.inventoryManager.getInventory(this.plugin, key.value());
    }

    @Override
    public boolean contains(final MenuKey key) {
        return find(key).isPresent();
    }

    @Override
    public Collection<Inventory> all() {
        return List.copyOf(this.inventoryManager.getInventories(this.plugin));
    }

    @Override
    public Collection<MenuKey> keys() {
        return all().stream().map(inventory -> MenuKey.of(inventory.getFileName())).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public LoadedMenus load(final String folder) {
        return this.loader.loadInventories(folder);
    }

    @Override
    public LoadedMenus load(final Path folder) {
        return this.loader.loadInventories(folder);
    }
}
