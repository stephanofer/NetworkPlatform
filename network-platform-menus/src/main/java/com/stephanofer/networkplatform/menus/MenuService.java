package com.stephanofer.networkplatform.menus;

import com.stephanofer.networkplatform.paper.module.PlatformModule;
import fr.maxlego08.menu.api.ButtonManager;
import fr.maxlego08.menu.api.DialogManager;
import fr.maxlego08.menu.api.InventoryManager;
import fr.maxlego08.menu.api.MenuPlugin;
import java.util.Optional;
import org.bukkit.entity.Player;

public interface MenuService extends PlatformModule, AutoCloseable {

    boolean available();

    MenuRegistry menus();

    DialogRegistry dialogs();

    MenuLoader loader();

    MenuOpenResult open(Player player, MenuKey key);

    MenuOpenResult open(Player player, MenuKey key, OpenOptions options);

    void close(Player player);

    boolean hasOpenMenu(Player player);

    void reload();

    MenuPlugin zMenu();

    InventoryManager inventoryManager();

    ButtonManager buttonManager();

    Optional<DialogManager> dialogManager();

    @Override
    void close();
}
