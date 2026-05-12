package com.stephanofer.networkplatform.menus;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record LoadedPatterns(Path folder, List<String> loaded, List<LoadFailure> failures) {

    public LoadedPatterns {
        Objects.requireNonNull(folder, "folder");
        loaded = List.copyOf(Objects.requireNonNull(loaded, "loaded"));
        failures = List.copyOf(Objects.requireNonNull(failures, "failures"));
    }
}
