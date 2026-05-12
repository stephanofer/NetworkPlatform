package com.stephanofer.networkplatform.database.internal;

import com.stephanofer.networkplatform.database.DatabaseModuleConfigException;
import java.util.Objects;

public final class TablePrefixValidator {

    private TablePrefixValidator() {
    }

    public static String normalize(final String prefix) {
        Objects.requireNonNull(prefix, "tablePrefix");
        final String normalized = prefix.trim();
        if (!normalized.matches("[A-Za-z0-9_]*")) {
            throw new DatabaseModuleConfigException(
                "tablePrefix contains unsupported characters: " + normalized
            );
        }
        return normalized;
    }
}
