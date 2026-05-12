package com.stephanofer.networkplatform.menus;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import com.stephanofer.networkplatform.paper.NetworkPlatform;
import com.stephanofer.networkplatform.paper.PlatformContext;
import com.stephanofer.networkplatform.paper.lifecycle.Lifecycle;
import com.stephanofer.networkplatform.paper.module.InstalledModuleRegistry;
import fr.maxlego08.menu.api.ButtonManager;
import fr.maxlego08.menu.api.DialogManager;
import fr.maxlego08.menu.api.InventoryManager;
import fr.maxlego08.menu.api.MenuPlugin;
import fr.maxlego08.menu.api.pattern.PatternManager;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

class MenuModuleTest {

    @Test
    void shouldFailWhenZMenuIsMissing() {
        final JavaPlugin plugin = mockConsumerPlugin();
        final PluginManager pluginManager = plugin.getServer().getPluginManager();
        when(pluginManager.getPlugin("zMenu")).thenReturn(null);

        final NetworkPlatform platform = mockPlatform(plugin);
        assertThrows(IllegalStateException.class, () -> MenuModule.install(platform));
    }

    @Test
    void shouldReturnExistingInstalledService() {
        final JavaPlugin plugin = mockConsumerPlugin();
        final MenuPlugin zMenu = mockZMenu(plugin.getServer());
        when(plugin.getServer().getPluginManager().getPlugin("zMenu")).thenReturn(zMenu);

        final NetworkPlatform platform = mockPlatform(plugin);
        final MenuService first = MenuModule.install(platform);
        final MenuService second = MenuModule.install(platform);

        assertSame(first, second);
    }

    @Test
    void shouldAutoLoadConfiguredFoldersOnInstall() {
        final JavaPlugin plugin = mockConsumerPlugin();
        final MenuPlugin zMenu = mockZMenu(plugin.getServer());
        final InventoryManager inventoryManager = zMenu.getInventoryManager();
        final PatternManager patternManager = zMenu.getPatternManager();
        when(plugin.getServer().getPluginManager().getPlugin("zMenu")).thenReturn(zMenu);

        final File dataFolder = plugin.getDataFolder();
        new File(dataFolder, "patterns").mkdirs();
        new File(dataFolder, "inventories").mkdirs();

        final NetworkPlatform platform = mockPlatform(plugin);
        final MenuModuleConfig config = new MenuModuleConfig(true, "patterns", "inventories", "dialogs");
        final MenuService service = MenuModule.install(platform, config);

        assertInstanceOf(MenuService.class, service);
        verify(patternManager, never()).unregisterPattern(org.mockito.ArgumentMatchers.any());
        verify(inventoryManager, never()).deleteInventories(plugin);
    }

    @Test
    void shouldReturnMissingWhenMenuWasNotLoaded() {
        final JavaPlugin plugin = mockConsumerPlugin();
        final MenuPlugin zMenu = mockZMenu(plugin.getServer());
        when(plugin.getServer().getPluginManager().getPlugin("zMenu")).thenReturn(zMenu);
        when(zMenu.getInventoryManager().getInventory(plugin, "main-menu")).thenReturn(Optional.empty());

        final NetworkPlatform platform = mockPlatform(plugin);
        final MenuService service = MenuModule.install(platform);
        final Player player = mock(Player.class);
        when(player.isOnline()).thenReturn(true);

        final MenuOpenResult result = service.open(player, MenuKey.of("main-menu"));
        assertInstanceOf(MenuOpenResult.Missing.class, result);
    }

    @Test
    void shouldExposeDialogsAsUnavailableWhenZMenuDidNotRegisterDialogManager() {
        final JavaPlugin plugin = mockConsumerPlugin();
        final MenuPlugin zMenu = mockZMenu(plugin.getServer());
        when(plugin.getServer().getPluginManager().getPlugin("zMenu")).thenReturn(zMenu);
        when(zMenu.getDialogManager()).thenReturn(null);

        final NetworkPlatform platform = mockPlatform(plugin);
        final MenuService service = MenuModule.install(platform);
        final Player player = mock(Player.class);

        assertInstanceOf(DialogOpenResult.Unavailable.class, service.dialogs().open(player, DialogKey.of("confirm")));
    }

    @Test
    void shouldCleanupPluginScopedRegistrationsOnClose() {
        final JavaPlugin plugin = mockConsumerPlugin();
        final MenuPlugin zMenu = mockZMenu(plugin.getServer());
        when(plugin.getServer().getPluginManager().getPlugin("zMenu")).thenReturn(zMenu);

        final NetworkPlatform platform = mockPlatform(plugin);
        final MenuService service = MenuModule.install(platform);
        service.close();

        verify(zMenu.getInventoryManager()).deleteInventories(plugin);
        verify(zMenu.getButtonManager()).unregisters(plugin);
    }

    private static JavaPlugin mockConsumerPlugin() {
        final JavaPlugin plugin = mock(JavaPlugin.class, RETURNS_DEEP_STUBS);
        final Server server = mock(Server.class);
        final PluginManager pluginManager = mock(PluginManager.class);
        final ServicesManager servicesManager = mock(ServicesManager.class);
        final File dataFolder = new File("build/tmp/test-consumer-" + System.nanoTime());
        dataFolder.mkdirs();

        when(plugin.getServer()).thenReturn(server);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        when(plugin.getDataFolder()).thenReturn(dataFolder);
        when(plugin.getName()).thenReturn("ConsumerPlugin");
        when(plugin.getResource(org.mockito.ArgumentMatchers.anyString())).thenReturn(null);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(server.getServicesManager()).thenReturn(servicesManager);
        return plugin;
    }

    private static NetworkPlatform mockPlatform(final JavaPlugin plugin) {
        final NetworkPlatform platform = mock(NetworkPlatform.class);
        final Lifecycle lifecycle = mock(Lifecycle.class);
        final InstalledModuleRegistry registry = mock(InstalledModuleRegistry.class);
        final PlatformContext context = new PlatformContext(plugin);
        final Map<String, Object> modules = new HashMap<>();

        when(platform.plugin()).thenReturn(plugin);
        when(platform.context()).thenReturn(context);
        when(platform.lifecycle()).thenReturn(lifecycle);
        when(platform.modules()).thenReturn(registry);
        when(registry.find(any())).thenAnswer(invocation -> Optional.ofNullable(modules.get(invocation.getArgument(0))));
        when(registry.register(any())).thenAnswer(invocation -> {
            final Object module = invocation.getArgument(0);
            if (module instanceof com.stephanofer.networkplatform.paper.module.PlatformModule platformModule) {
                modules.put(platformModule.id(), module);
            }
            return module;
        });

        return platform;
    }

    private static MenuPlugin mockZMenu(final Server server) {
        final MenuPlugin menuPlugin = mock(MenuPlugin.class);
        final InventoryManager inventoryManager = mock(InventoryManager.class);
        final ButtonManager buttonManager = mock(ButtonManager.class);
        final PatternManager patternManager = mock(PatternManager.class);
        final DialogManager dialogManager = mock(DialogManager.class);
        final fr.maxlego08.menu.api.command.CommandManager commandManager = mock(fr.maxlego08.menu.api.command.CommandManager.class);
        final fr.maxlego08.menu.hooks.folialib.impl.PlatformScheduler scheduler = mock(fr.maxlego08.menu.hooks.folialib.impl.PlatformScheduler.class);

        when(menuPlugin.isEnabled()).thenReturn(true);
        when(menuPlugin.getServer()).thenReturn(server);
        when(menuPlugin.getInventoryManager()).thenReturn(inventoryManager);
        when(menuPlugin.getButtonManager()).thenReturn(buttonManager);
        when(menuPlugin.getPatternManager()).thenReturn(patternManager);
        when(menuPlugin.getCommandManager()).thenReturn(commandManager);
        when(menuPlugin.getScheduler()).thenReturn(scheduler);
        when(menuPlugin.getDialogManager()).thenReturn(dialogManager);
        when(scheduler.isGlobalTickThread()).thenReturn(true);
        when(buttonManager.getPermissibles()).thenReturn(java.util.Map.of());
        return menuPlugin;
    }
}
