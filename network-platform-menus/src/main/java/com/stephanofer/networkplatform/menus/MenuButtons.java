package com.stephanofer.networkplatform.menus;

import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.loader.NoneLoader;
import org.bukkit.plugin.Plugin;

public final class MenuButtons {

    private MenuButtons() {
    }

    public static NoneLoader none(final Plugin plugin, final Class<? extends Button> buttonClass, final String name) {
        return new NoneLoader(plugin, buttonClass, name);
    }
}
