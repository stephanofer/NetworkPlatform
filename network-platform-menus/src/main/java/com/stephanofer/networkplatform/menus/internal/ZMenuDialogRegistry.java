package com.stephanofer.networkplatform.menus.internal;

import com.stephanofer.networkplatform.menus.DialogKey;
import com.stephanofer.networkplatform.menus.DialogOpenResult;
import com.stephanofer.networkplatform.menus.DialogRegistry;
import com.stephanofer.networkplatform.menus.LoadedDialogs;
import com.stephanofer.networkplatform.menus.OpenOptions;
import fr.maxlego08.menu.api.DialogInventory;
import fr.maxlego08.menu.api.DialogManager;
import fr.maxlego08.menu.api.MenuPlugin;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

final class ZMenuDialogRegistry implements DialogRegistry {

    private final MenuPlugin menuPlugin;
    private final Optional<DialogManager> dialogManager;
    private final ZMenuMenuLoader loader;

    ZMenuDialogRegistry(final MenuPlugin menuPlugin, final Optional<DialogManager> dialogManager, final ZMenuMenuLoader loader) {
        this.menuPlugin = menuPlugin;
        this.dialogManager = dialogManager;
        this.loader = loader;
    }

    @Override
    public boolean available() {
        return this.dialogManager.isPresent();
    }

    @Override
    public DialogOpenResult open(final Player player, final DialogKey key) {
        return open(player, key, OpenOptions.defaults());
    }

    @Override
    public DialogOpenResult open(final Player player, final DialogKey key, final OpenOptions options) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(options, "options");
        final DialogManager manager = this.dialogManager.orElse(null);
        if (manager == null) {
            return new DialogOpenResult.Unavailable("zMenu dialogs are unavailable on this runtime or configuration");
        }

        final Optional<DialogInventory> dialog = find(key);
        if (dialog.isEmpty()) {
            return new DialogOpenResult.Missing(key);
        }

        try {
            MenuArgumentApplier.apply(this.menuPlugin, player, options.arguments());
            if (options.requireMainThread() && !this.menuPlugin.getScheduler().isGlobalTickThread()) {
                this.menuPlugin.getScheduler().runNextTick(task -> manager.openDialog(player, dialog.get()));
            } else {
                manager.openDialog(player, dialog.get());
            }
            return new DialogOpenResult.Opened(key);
        } catch (final Throwable throwable) {
            return new DialogOpenResult.Failed(key, throwable);
        }
    }

    @Override
    public Optional<DialogInventory> find(final DialogKey key) {
        return this.dialogManager.flatMap(manager -> manager.getDialog(this.menuPlugin, key.value()));
    }

    @Override
    public Collection<DialogInventory> all() {
        return this.dialogManager
            .map(DialogManager::getDialogs)
            .stream()
            .flatMap(Collection::stream)
            .filter(dialog -> dialog.getPlugin().equals(this.menuPlugin))
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<DialogKey> keys() {
        return all().stream()
            .filter(dialog -> dialog.getPlugin().equals(this.menuPlugin))
            .map(dialog -> DialogKey.of(dialog.getFileName()))
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public LoadedDialogs load(final String folder) {
        return this.loader.loadDialogs(folder);
    }

    @Override
    public LoadedDialogs load(final Path folder) {
        return this.loader.loadDialogs(folder);
    }

    @Override
    public Optional<DialogManager> nativeManager() {
        return this.dialogManager;
    }
}
