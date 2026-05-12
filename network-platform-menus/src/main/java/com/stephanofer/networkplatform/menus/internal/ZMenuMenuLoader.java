package com.stephanofer.networkplatform.menus.internal;

import com.stephanofer.networkplatform.menus.LoadFailure;
import com.stephanofer.networkplatform.menus.LoadedDialogs;
import com.stephanofer.networkplatform.menus.LoadedMenus;
import com.stephanofer.networkplatform.menus.LoadedPatterns;
import com.stephanofer.networkplatform.menus.MenuKey;
import com.stephanofer.networkplatform.menus.MenuLoader;
import fr.maxlego08.menu.api.ButtonManager;
import fr.maxlego08.menu.api.DialogInventory;
import fr.maxlego08.menu.api.DialogManager;
import fr.maxlego08.menu.api.Inventory;
import fr.maxlego08.menu.api.InventoryManager;
import fr.maxlego08.menu.api.pattern.Pattern;
import fr.maxlego08.menu.api.pattern.PatternManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.bukkit.plugin.Plugin;

final class ZMenuMenuLoader implements MenuLoader {

    private final Plugin plugin;
    private final Logger logger;
    private final ButtonManager buttonManager;
    private final InventoryManager inventoryManager;
    private final PatternManager patternManager;
    private final Optional<DialogManager> dialogManager;
    private final List<Consumer<ButtonManager>> buttonRegistrations = new ArrayList<>();
    private final List<Consumer<ButtonManager>> actionRegistrations = new ArrayList<>();
    private final List<Consumer<ButtonManager>> permissibleRegistrations = new ArrayList<>();
    private final Set<Path> inventoryFolders = new LinkedHashSet<>();
    private final Set<Path> patternFolders = new LinkedHashSet<>();
    private final Set<Path> dialogFolders = new LinkedHashSet<>();
    private final Set<String> actionKeys = new LinkedHashSet<>();
    private final Set<String> permissibleKeys = new LinkedHashSet<>();
    private final List<Pattern> loadedPatterns = new ArrayList<>();

    ZMenuMenuLoader(
        final Plugin plugin,
        final Logger logger,
        final ButtonManager buttonManager,
        final InventoryManager inventoryManager,
        final PatternManager patternManager,
        final Optional<DialogManager> dialogManager
    ) {
        this.plugin = plugin;
        this.logger = logger;
        this.buttonManager = buttonManager;
        this.inventoryManager = inventoryManager;
        this.patternManager = patternManager;
        this.dialogManager = dialogManager;
    }

    @Override
    public MenuLoader registerButtons(final Consumer<ButtonManager> registration) {
        this.buttonRegistrations.add(applyRegistration(registration));
        return this;
    }

    @Override
    public MenuLoader registerActions(final Consumer<ButtonManager> registration) {
        this.actionRegistrations.add(applyRegistration(registration));
        return this;
    }

    @Override
    public MenuLoader registerPermissibles(final Consumer<ButtonManager> registration) {
        this.permissibleRegistrations.add(applyRegistration(registration));
        return this;
    }

    @Override
    public LoadedMenus loadInventories(final String folder) {
        return loadInventories(resolveFolder(folder));
    }

    @Override
    public LoadedMenus loadInventories(final Path folder) {
        final Path normalized = normalizeFolder(folder);
        this.inventoryFolders.add(normalized);
        final List<MenuKey> loaded = new ArrayList<>();
        final List<LoadFailure> failures = new ArrayList<>();
        walkYamlFiles(normalized, file -> {
            try {
                final Inventory inventory = this.inventoryManager.loadInventory(this.plugin, file.toFile());
                loaded.add(MenuKey.of(inventory.getFileName()));
            } catch (final Exception exception) {
                failures.add(new LoadFailure(file, exception.getMessage()));
                this.logger.log(Level.SEVERE, "Failed to load zMenu inventory from " + file, exception);
            }
        }, failures);
        return new LoadedMenus(normalized, loaded, failures);
    }

    @Override
    public LoadedDialogs loadDialogs(final String folder) {
        return loadDialogs(resolveFolder(folder));
    }

    @Override
    public LoadedDialogs loadDialogs(final Path folder) {
        final Path normalized = normalizeFolder(folder);
        this.dialogFolders.add(normalized);
        final DialogManager manager = this.dialogManager.orElse(null);
        if (manager == null) {
            return new LoadedDialogs(normalized, List.of(), List.of(new LoadFailure(normalized, "DialogManager unavailable")));
        }

        final List<com.stephanofer.networkplatform.menus.DialogKey> loaded = new ArrayList<>();
        final List<LoadFailure> failures = new ArrayList<>();
        walkYamlFiles(normalized, file -> {
            try {
                final DialogInventory dialog = manager.loadInventory(this.plugin, file.toFile());
                loaded.add(com.stephanofer.networkplatform.menus.DialogKey.of(dialog.getFileName()));
            } catch (final Exception exception) {
                failures.add(new LoadFailure(file, exception.getMessage()));
                this.logger.log(Level.SEVERE, "Failed to load zMenu dialog from " + file, exception);
            }
        }, failures);
        return new LoadedDialogs(normalized, loaded, failures);
    }

    @Override
    public LoadedPatterns loadPatterns(final String folder) {
        return loadPatterns(resolveFolder(folder));
    }

    @Override
    public LoadedPatterns loadPatterns(final Path folder) {
        final Path normalized = normalizeFolder(folder);
        this.patternFolders.add(normalized);
        final List<String> loaded = new ArrayList<>();
        final List<LoadFailure> failures = new ArrayList<>();
        walkYamlFiles(normalized, file -> {
            try {
                final Pattern pattern = this.patternManager.loadPattern(file.toFile());
                if (pattern != null) {
                    this.loadedPatterns.add(pattern);
                    loaded.add(pattern.name());
                }
            } catch (final Exception exception) {
                failures.add(new LoadFailure(file, exception.getMessage()));
                this.logger.log(Level.SEVERE, "Failed to load zMenu pattern from " + file, exception);
            }
        }, failures);
        return new LoadedPatterns(normalized, loaded, failures);
    }

    @Override
    public void reload() {
        cleanup();
        replayRegistrations(this.permissibleRegistrations);
        replayRegistrations(this.actionRegistrations);
        replayRegistrations(this.buttonRegistrations);
        this.patternFolders.forEach(this::loadPatterns);
        this.inventoryFolders.forEach(this::loadInventories);
        this.dialogFolders.forEach(this::loadDialogs);
    }

    void shutdown() {
        cleanup();
    }

    private Consumer<ButtonManager> applyRegistration(final Consumer<ButtonManager> registration) {
        Objects.requireNonNull(registration, "registration");
        final RegistrationSnapshot before = RegistrationSnapshot.capture(this.buttonManager);
        registration.accept(this.buttonManager);
        final RegistrationSnapshot after = RegistrationSnapshot.capture(this.buttonManager);
        this.actionKeys.addAll(after.newActionKeysComparedTo(before));
        this.permissibleKeys.addAll(after.newPermissibleKeysComparedTo(before));
        return registration;
    }

    private void replayRegistrations(final Collection<Consumer<ButtonManager>> registrations) {
        for (final Consumer<ButtonManager> registration : registrations) {
            final RegistrationSnapshot before = RegistrationSnapshot.capture(this.buttonManager);
            registration.accept(this.buttonManager);
            final RegistrationSnapshot after = RegistrationSnapshot.capture(this.buttonManager);
            this.actionKeys.addAll(after.newActionKeysComparedTo(before));
            this.permissibleKeys.addAll(after.newPermissibleKeysComparedTo(before));
        }
    }

    private void cleanup() {
        this.inventoryManager.deleteInventories(this.plugin);
        this.dialogManager.ifPresent(manager -> manager.deleteDialog(this.plugin));
        this.buttonManager.unregisters(this.plugin);
        cleanupPatterns();
        ReflectiveButtonCleanup.removeActionKeys(this.buttonManager, this.actionKeys, this.logger);
        ReflectiveButtonCleanup.removePermissibleKeys(this.buttonManager, this.permissibleKeys, this.logger);
    }

    private void cleanupPatterns() {
        for (final Pattern pattern : List.copyOf(this.loadedPatterns)) {
            this.patternManager.unregisterPattern(pattern);
        }
        this.loadedPatterns.clear();
    }

    private Path resolveFolder(final String folder) {
        Objects.requireNonNull(folder, "folder");
        return this.plugin.getDataFolder().toPath().resolve(folder.trim());
    }

    private Path normalizeFolder(final Path folder) {
        Objects.requireNonNull(folder, "folder");
        return folder.normalize();
    }

    private void walkYamlFiles(final Path folder, final ThrowingPathConsumer consumer, final List<LoadFailure> failures) {
        try {
            Files.createDirectories(folder);
            try (Stream<Path> stream = Files.walk(folder)) {
                stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".yml"))
                    .sorted()
                    .forEach(path -> {
                        try {
                            consumer.accept(path);
                        } catch (final Exception exception) {
                            failures.add(new LoadFailure(path, exception.getMessage()));
                            this.logger.log(Level.SEVERE, "Failed to load zMenu file from " + path, exception);
                        }
                    });
            }
        } catch (final IOException exception) {
            failures.add(new LoadFailure(folder, exception.getMessage()));
            this.logger.log(Level.SEVERE, "Failed to walk folder " + folder, exception);
        }
    }

    @FunctionalInterface
    private interface ThrowingPathConsumer {
        void accept(Path path) throws Exception;
    }

    private record RegistrationSnapshot(Set<String> actionKeys, Set<String> permissibleKeys) {

        static RegistrationSnapshot capture(final ButtonManager buttonManager) {
            return new RegistrationSnapshot(
                snapshotActionKeys(buttonManager),
                new LinkedHashSet<>(buttonManager.getPermissibles().keySet())
            );
        }

        Set<String> newActionKeysComparedTo(final RegistrationSnapshot before) {
            final Set<String> keys = new LinkedHashSet<>(this.actionKeys);
            keys.removeAll(before.actionKeys);
            return keys;
        }

        Set<String> newPermissibleKeysComparedTo(final RegistrationSnapshot before) {
            final Set<String> keys = new LinkedHashSet<>(this.permissibleKeys);
            keys.removeAll(before.permissibleKeys);
            return keys;
        }

        private static Set<String> snapshotActionKeys(final ButtonManager buttonManager) {
            final Set<String> keys = new LinkedHashSet<>();
            ReflectiveButtonCleanup.collectActionKeys(buttonManager, keys);
            return keys;
        }
    }
}
