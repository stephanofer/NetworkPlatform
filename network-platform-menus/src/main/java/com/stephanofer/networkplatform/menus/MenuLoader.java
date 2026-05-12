package com.stephanofer.networkplatform.menus;

import fr.maxlego08.menu.api.ButtonManager;
import java.nio.file.Path;
import java.util.function.Consumer;

public interface MenuLoader {

    MenuLoader registerButtons(Consumer<ButtonManager> registration);

    MenuLoader registerActions(Consumer<ButtonManager> registration);

    MenuLoader registerPermissibles(Consumer<ButtonManager> registration);

    LoadedMenus loadInventories(String folder);

    LoadedMenus loadInventories(Path folder);

    LoadedDialogs loadDialogs(String folder);

    LoadedDialogs loadDialogs(Path folder);

    LoadedPatterns loadPatterns(String folder);

    LoadedPatterns loadPatterns(Path folder);

    void reload();
}
