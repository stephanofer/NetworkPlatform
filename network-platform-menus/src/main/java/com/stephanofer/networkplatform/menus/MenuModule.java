package com.stephanofer.networkplatform.menus;

import com.stephanofer.networkplatform.menus.internal.ZMenuMenuService;
import com.stephanofer.networkplatform.paper.NetworkPlatform;
import com.stephanofer.networkplatform.paper.module.PlatformModule;
import java.util.Objects;

public final class MenuModule {

    public static final String MODULE_ID = "menus";

    private MenuModule() {
    }

    public static MenuService install(final NetworkPlatform platform) {
        return install(platform, MenuModuleConfig.defaults());
    }

    public static MenuService install(final NetworkPlatform platform, final MenuModuleConfig config) {
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(config, "config");
        platform.ensureActive();

        final PlatformModule existing = platform.modules().find(MODULE_ID).orElse(null);
        if (existing != null) {
            if (existing instanceof MenuService menuService) {
                return menuService;
            }

            throw new IllegalStateException("NetworkPlatform module already installed with incompatible type: " + MODULE_ID);
        }

        final ZMenuMenuService service = new ZMenuMenuService(platform, config);
        platform.modules().register(service);
        platform.lifecycle().onShutdown(service::close);

        if (config.autoLoadOnInstall()) {
            service.loader().loadPatterns(config.patternsFolder());
            service.loader().loadInventories(config.inventoriesFolder());
            if (!config.dialogsFolder().isBlank()) {
                service.loader().loadDialogs(config.dialogsFolder());
            }
        }

        return service;
    }
}
