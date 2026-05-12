package com.stephanofer.networkplatform.paper.config.internal;

import com.stephanofer.networkplatform.paper.config.ConfigDocument;
import com.stephanofer.networkplatform.paper.config.ConfigException;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.YamlDocument;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class DefaultConfigDocument implements ConfigDocument {

    private final DefaultConfigHandle handle;
    private final String scope;

    DefaultConfigDocument(final DefaultConfigHandle handle, final String scope) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.scope = scope == null || scope.isBlank() ? "" : scope.trim();
    }

    @Override
    public String scope() {
        return this.scope;
    }

    @Override
    public ConfigDocument root() {
        return this.handle.document();
    }

    @Override
    public ConfigDocument view(final String relativePath) {
        return new DefaultConfigDocument(this.handle, qualify(relativePath));
    }

    @Override
    public boolean contains(final String path) {
        if (path == null || path.isBlank()) {
            return sectionOrNull() != null;
        }
        return rootDocument().contains(qualify(path));
    }

    @Override
    public Object get(final String path) {
        return rootDocument().get(qualify(path));
    }

    @Override
    public Object get(final String path, final Object defaultValue) {
        return rootDocument().get(qualify(path), defaultValue);
    }

    @Override
    public Optional<String> getOptionalString(final String path) {
        return rootDocument().getOptionalString(qualify(path));
    }

    @Override
    public String getString(final String path) {
        return rootDocument().getString(qualify(path));
    }

    @Override
    public String getString(final String path, final String defaultValue) {
        return rootDocument().getString(qualify(path), defaultValue);
    }

    @Override
    public Optional<Integer> getOptionalInt(final String path) {
        return rootDocument().getOptionalInt(qualify(path));
    }

    @Override
    public int getInt(final String path) {
        return rootDocument().getInt(qualify(path));
    }

    @Override
    public int getInt(final String path, final int defaultValue) {
        return rootDocument().getInt(qualify(path), defaultValue);
    }

    @Override
    public Optional<Long> getOptionalLong(final String path) {
        return rootDocument().getOptionalLong(qualify(path));
    }

    @Override
    public long getLong(final String path) {
        return rootDocument().getLong(qualify(path));
    }

    @Override
    public long getLong(final String path, final long defaultValue) {
        return rootDocument().getLong(qualify(path), defaultValue);
    }

    @Override
    public Optional<Double> getOptionalDouble(final String path) {
        return rootDocument().getOptionalDouble(qualify(path));
    }

    @Override
    public double getDouble(final String path) {
        return rootDocument().getDouble(qualify(path));
    }

    @Override
    public double getDouble(final String path, final double defaultValue) {
        return rootDocument().getDouble(qualify(path), defaultValue);
    }

    @Override
    public Optional<Boolean> getOptionalBoolean(final String path) {
        return rootDocument().getOptionalBoolean(qualify(path));
    }

    @Override
    public boolean getBoolean(final String path) {
        return rootDocument().getBoolean(qualify(path));
    }

    @Override
    public boolean getBoolean(final String path, final boolean defaultValue) {
        return rootDocument().getBoolean(qualify(path), defaultValue);
    }

    @Override
    public List<String> getStringList(final String path) {
        return rootDocument().getStringList(qualify(path));
    }

    @Override
    public List<String> getStringList(final String path, final List<String> defaultValue) {
        return rootDocument().getStringList(qualify(path), defaultValue);
    }

    @Override
    public Set<String> keys() {
        final Section section = sectionOrNull();
        if (section == null) {
            return Set.of();
        }

        final Set<String> keys = new LinkedHashSet<>();
        for (final Object key : section.getKeys()) {
            keys.add(String.valueOf(key));
        }
        return Set.copyOf(keys);
    }

    @Override
    public void set(final String path, final Object value) {
        rootDocument().set(qualify(path), value);
        this.handle.touch();
    }

    @Override
    public boolean remove(final String path) {
        final boolean removed = rootDocument().remove(qualify(path));
        if (removed) {
            this.handle.touch();
        }
        return removed;
    }

    @Override
    public void markDirty() {
        this.handle.markDirty();
    }

    @Override
    public <T> T unwrap(final Class<T> type) {
        Objects.requireNonNull(type, "type");
        final Object raw = this.scope.isBlank() ? rootDocument() : sectionOrNull();
        if (raw == null) {
            throw new ConfigException("section does not exist for scope '" + this.scope + "' in " + this.handle.path());
        }
        if (!type.isInstance(raw)) {
            throw new IllegalArgumentException("requested unwrap type " + type.getName() + " does not match " + raw.getClass().getName());
        }
        return type.cast(raw);
    }

    private YamlDocument rootDocument() {
        return this.handle.rawDocument();
    }

    private Section sectionOrNull() {
        return this.scope.isBlank() ? rootDocument() : rootDocument().getSection(this.scope, null);
    }

    private String qualify(final String path) {
        final String normalized = path == null ? "" : path.trim();
        if (normalized.isEmpty()) {
            return this.scope;
        }
        return this.scope.isBlank() ? normalized : this.scope + "." + normalized;
    }
}
