package com.stephanofer.networkplatform.menus.internal;

import com.stephanofer.networkplatform.menus.DialogRegistry;
import com.stephanofer.networkplatform.menus.MenuKey;
import com.stephanofer.networkplatform.menus.MenuLoader;
import com.stephanofer.networkplatform.menus.MenuModule;
import com.stephanofer.networkplatform.menus.MenuModuleConfig;
import com.stephanofer.networkplatform.menus.MenuOpenResult;
import com.stephanofer.networkplatform.menus.MenuRegistry;
import com.stephanofer.networkplatform.menus.MenuService;
import com.stephanofer.networkplatform.menus.OpenOptions;
import com.stephanofer.networkplatform.paper.NetworkPlatform;
import fr.maxlego08.menu.api.ButtonManager;
import fr.maxlego08.menu.api.DialogManager;
import fr.maxlego08.menu.api.Inventory;
import fr.maxlego08.menu.api.InventoryManager;
import fr.maxlego08.menu.api.MenuPlugin;
import fr.maxlego08.menu.api.pattern.PatternManager;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

public final class ZMenuMenuService implements MenuService {

    private final NetworkPlatform platform;
    private final Plugin consumerPlugin;
    private final MenuModuleConfig config;
    private final ZMenuHook hook;
    private final ZMenuMenuLoader loader;
    private final MenuRegistry menuRegistry;
    private final DialogRegistry dialogRegistry;
    private final MenuTrackingListener trackingListener;
    private final Set<UUID> openMenus = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public ZMenuMenuService(final NetworkPlatform platform, final MenuModuleConfig config) {
        this.platform = Objects.requireNonNull(platform, "platform");
        this.consumerPlugin = platform.plugin();
        this.config = Objects.requireNonNull(config, "config");
        this.hook = ZMenuHook.create(platform);
        final PatternManager patternManager = this.hook.menuPlugin().getPatternManager();
        this.loader = new ZMenuMenuLoader(
            this.consumerPlugin,
            this.platform.context().logger(),
            this.hook.buttonManager(),
            this.hook.inventoryManager(),
            patternManager,
            this.hook.dialogManager()
        );
        this.menuRegistry = new ZMenuMenuRegistry(this.consumerPlugin, this.hook.inventoryManager(), this.loader);
        this.dialogRegistry = new ZMenuDialogRegistry(this.hook.menuPlugin(), this.hook.dialogManager(), this.loader);
        this.trackingListener = new MenuTrackingListener(this.consumerPlugin, this.openMenus);
        this.consumerPlugin.getServer().getPluginManager().registerEvents(this.trackingListener, this.consumerPlugin);
    }

    @Override
    public String id() {
        return MenuModule.MODULE_ID;
    }

    @Override
    public boolean available() {
        return !this.closed.get() && this.hook.available();
    }

    @Override
    public MenuRegistry menus() {
        return this.menuRegistry;
    }

    @Override
    public DialogRegistry dialogs() {
        return this.dialogRegistry;
    }

    @Override
    public MenuLoader loader() {
        return this.loader;
    }

    @Override
    public MenuOpenResult open(final Player player, final MenuKey key) {
        return open(player, key, OpenOptions.defaults());
    }

    @Override
    public MenuOpenResult open(final Player player, final MenuKey key, final OpenOptions options) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(options, "options");
        if (!available()) {
            return new MenuOpenResult.Unavailable("zMenu integration is unavailable or already closed");
        }
        if (!player.isOnline()) {
            return new MenuOpenResult.Unavailable("Player is offline");
        }

        final Optional<Inventory> inventory = this.hook.inventoryManager().getInventory(this.consumerPlugin, key.value());
        if (inventory.isEmpty()) {
            return new MenuOpenResult.Missing(key);
        }

        try {
            MenuArgumentApplier.apply(this.hook.menuPlugin(), player, options.arguments());
            final Runnable operation = () -> {
                if (options.preserveHistory()) {
                    this.hook.inventoryManager().openInventoryWithOldInventories(player, inventory.get(), options.page());
                } else {
                    this.hook.inventoryManager().openInventory(player, inventory.get(), options.page());
                }
            };

            if (options.requireMainThread() && !this.hook.menuPlugin().getScheduler().isGlobalTickThread()) {
                this.hook.menuPlugin().getScheduler().runNextTick(task -> operation.run());
            } else {
                operation.run();
            }
            return new MenuOpenResult.Opened(key);
        } catch (final Throwable throwable) {
            return new MenuOpenResult.Failed(key, throwable);
        }
    }

    @Override
    public void close(final Player player) {
        Objects.requireNonNull(player, "player");
        this.openMenus.remove(player.getUniqueId());
        player.closeInventory();
    }

    @Override
    public boolean hasOpenMenu(final Player player) {
        Objects.requireNonNull(player, "player");
        return this.openMenus.contains(player.getUniqueId());
    }

    @Override
    public void reload() {
        ensureOpen();
        this.loader.reload();
    }

    @Override
    public MenuPlugin zMenu() {
        return this.hook.menuPlugin();
    }

    @Override
    public InventoryManager inventoryManager() {
        return this.hook.inventoryManager();
    }

    @Override
    public ButtonManager buttonManager() {
        return this.hook.buttonManager();
    }

    @Override
    public Optional<DialogManager> dialogManager() {
        return this.hook.dialogManager();
    }

    @Override
    public void close() {
        if (!this.closed.compareAndSet(false, true)) {
            return;
        }

        for (final UUID uuid : Set.copyOf(this.openMenus)) {
            final Player player = this.consumerPlugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.closeInventory();
            }
        }
        this.openMenus.clear();
        this.loader.shutdown();
        HandlerList.unregisterAll(this.trackingListener);
    }

    private void ensureOpen() {
        this.platform.ensureActive();
        if (this.closed.get()) {
            throw new IllegalStateException("MenuService is already closed");
        }
    }
}
