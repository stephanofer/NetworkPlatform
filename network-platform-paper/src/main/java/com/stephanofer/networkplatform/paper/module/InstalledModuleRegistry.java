package com.stephanofer.networkplatform.paper.module;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InstalledModuleRegistry {

    private final Map<String, PlatformModule> modules = new ConcurrentHashMap<>();

    public <T extends PlatformModule> T register(final T module) {
        Objects.requireNonNull(module, "module");
        final String id = requireModuleId(module.id());
        final PlatformModule previous = this.modules.putIfAbsent(id, module);

        if (previous != null) {
            throw new IllegalStateException("NetworkPlatform module already installed: " + id);
        }

        return module;
    }

    public Optional<PlatformModule> find(final String id) {
        return Optional.ofNullable(this.modules.get(requireModuleId(id)));
    }

    public boolean isInstalled(final String id) {
        return this.modules.containsKey(requireModuleId(id));
    }

    private static String requireModuleId(final String id) {
        Objects.requireNonNull(id, "id");
        final String normalized = id.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("module id cannot be blank");
        }

        return normalized;
    }
}
