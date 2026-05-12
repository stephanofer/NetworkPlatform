package com.stephanofer.networkplatform.menus;

import fr.maxlego08.menu.api.Inventory;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public interface MenuRegistry {

    Optional<Inventory> find(MenuKey key);

    boolean contains(MenuKey key);

    Collection<Inventory> all();

    Collection<MenuKey> keys();

    LoadedMenus load(String folder);

    LoadedMenus load(Path folder);
}
