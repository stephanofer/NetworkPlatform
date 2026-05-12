package com.stephanofer.networkplatform.menus;

import java.nio.file.Path;
import java.util.Objects;

public record LoadFailure(Path path, String reason) {

    public LoadFailure {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(reason, "reason");
    }
}
