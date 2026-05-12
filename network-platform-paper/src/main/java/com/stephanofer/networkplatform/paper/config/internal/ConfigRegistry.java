package com.stephanofer.networkplatform.paper.config.internal;

import com.stephanofer.networkplatform.paper.config.ConfigTemplate;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ConfigRegistry {

    private final ConcurrentMap<String, DefaultConfigHandle> handles = new ConcurrentHashMap<>();

    public Optional<DefaultConfigHandle> find(final String relativePath) {
        return Optional.ofNullable(this.handles.get(Objects.requireNonNull(relativePath, "relativePath")));
    }

    public DefaultConfigHandle register(final ConfigTemplate template, final DefaultConfigHandle handle) {
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(handle, "handle");
        final DefaultConfigHandle existing = this.handles.putIfAbsent(template.path().value(), handle);
        if (existing != null) {
            if (!existing.template().equals(template)) {
                throw new IllegalStateException("configuration file already registered with different template: " + template.path().value());
            }
            return existing;
        }
        return handle;
    }

    public boolean isLoaded(final String relativePath) {
        return this.handles.containsKey(Objects.requireNonNull(relativePath, "relativePath"));
    }

    public void clear() {
        this.handles.clear();
    }
}
