package com.stephanofer.networkplatform.menus;

import fr.maxlego08.menu.api.DialogInventory;
import fr.maxlego08.menu.api.DialogManager;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import org.bukkit.entity.Player;

public interface DialogRegistry {

    boolean available();

    DialogOpenResult open(Player player, DialogKey key);

    DialogOpenResult open(Player player, DialogKey key, OpenOptions options);

    Optional<DialogInventory> find(DialogKey key);

    Collection<DialogInventory> all();

    Collection<DialogKey> keys();

    LoadedDialogs load(String folder);

    LoadedDialogs load(Path folder);

    Optional<DialogManager> nativeManager();
}
