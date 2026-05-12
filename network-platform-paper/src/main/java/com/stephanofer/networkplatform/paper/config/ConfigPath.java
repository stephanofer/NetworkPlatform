package com.stephanofer.networkplatform.paper.config;

import java.nio.file.Path;
import java.util.Objects;

public final class ConfigPath {

    private final String value;

    private ConfigPath(final String value) {
        this.value = value;
    }

    public static ConfigPath of(final String rawPath) {
        Objects.requireNonNull(rawPath, "rawPath");
        final String sanitized = rawPath.trim().replace('\\', '/');
        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("config path cannot be blank");
        }

        final Path candidate = Path.of(sanitized).normalize();
        if (candidate.isAbsolute()) {
            throw new IllegalArgumentException("config path must be relative: " + rawPath);
        }

        final String normalized = candidate.toString().replace('\\', '/');
        if (normalized.isEmpty() || normalized.equals(".")) {
            throw new IllegalArgumentException("config path cannot resolve to current directory");
        }
        if (normalized.startsWith("../") || normalized.equals("..")) {
            throw new IllegalArgumentException("config path cannot escape plugin directory: " + rawPath);
        }

        return new ConfigPath(normalized);
    }

    public String value() {
        return this.value;
    }

    public Path resolveAgainst(final Path parentDirectory) {
        final Path resolved = Objects.requireNonNull(parentDirectory, "parentDirectory").resolve(this.value).normalize();
        if (!resolved.startsWith(parentDirectory.normalize())) {
            throw new IllegalArgumentException("config path escapes plugin directory: " + this.value);
        }
        return resolved;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ConfigPath that)) {
            return false;
        }
        return this.value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
}
