package com.stephanofer.networkplatform.menus;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record LoadedMenus(Path folder, List<MenuKey> loaded, List<LoadFailure> failures) {

    public LoadedMenus {
        Objects.requireNonNull(folder, "folder");
        loaded = List.copyOf(Objects.requireNonNull(loaded, "loaded"));
        failures = List.copyOf(Objects.requireNonNull(failures, "failures"));
    }
}
