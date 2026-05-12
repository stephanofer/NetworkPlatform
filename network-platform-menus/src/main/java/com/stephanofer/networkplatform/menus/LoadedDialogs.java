package com.stephanofer.networkplatform.menus;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record LoadedDialogs(Path folder, List<DialogKey> loaded, List<LoadFailure> failures) {

    public LoadedDialogs {
        Objects.requireNonNull(folder, "folder");
        loaded = List.copyOf(Objects.requireNonNull(loaded, "loaded"));
        failures = List.copyOf(Objects.requireNonNull(failures, "failures"));
    }
}
