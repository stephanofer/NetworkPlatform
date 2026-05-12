package com.stephanofer.networkplatform.menus;

import java.util.Objects;

public record MenuModuleConfig(
    boolean autoLoadOnInstall,
    String patternsFolder,
    String inventoriesFolder,
    String dialogsFolder
) {

    public MenuModuleConfig {
        patternsFolder = normalizeFolder(patternsFolder, "patternsFolder");
        inventoriesFolder = normalizeFolder(inventoriesFolder, "inventoriesFolder");
        dialogsFolder = normalizeOptionalFolder(dialogsFolder);
    }

    public static MenuModuleConfig defaults() {
        return new MenuModuleConfig(false, "patterns", "inventories", "dialogs");
    }

    private static String normalizeFolder(final String folder, final String name) {
        Objects.requireNonNull(folder, name);
        final String normalized = folder.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return normalized;
    }

    private static String normalizeOptionalFolder(final String folder) {
        Objects.requireNonNull(folder, "dialogsFolder");
        return folder.trim();
    }
}
