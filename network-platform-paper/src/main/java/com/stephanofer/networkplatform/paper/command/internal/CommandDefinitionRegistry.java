package com.stephanofer.networkplatform.paper.command.internal;

import com.stephanofer.networkplatform.paper.command.CommandSpec;
import com.stephanofer.networkplatform.paper.command.RawCommandRegistration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class CommandDefinitionRegistry {

    private final Map<String, CommandSpec> specsByRoot = new LinkedHashMap<>();
    private final List<RawCommandRegistration> rawRegistrations = new ArrayList<>();
    private final Set<String> labels = new LinkedHashSet<>();

    public synchronized void registerSpec(final CommandSpec spec) {
        Objects.requireNonNull(spec, "spec");
        final List<String> normalizedLabels = collectLabels(spec.name(), spec.aliases());
        ensureAvailable(normalizedLabels);
        this.specsByRoot.put(normalize(spec.name()), spec);
        this.labels.addAll(normalizedLabels);
    }

    public synchronized void registerRaw(final RawCommandRegistration registration) {
        Objects.requireNonNull(registration, "registration");
        final List<String> normalizedLabels = collectLabels(registration.node().getLiteral(), registration.aliases());
        ensureAvailable(normalizedLabels);
        this.rawRegistrations.add(registration);
        this.labels.addAll(normalizedLabels);
    }

    public synchronized Optional<CommandSpec> find(final String rootName) {
        return Optional.ofNullable(this.specsByRoot.get(normalize(rootName)));
    }

    public synchronized boolean isRegistered(final String label) {
        return this.labels.contains(normalize(label));
    }

    public synchronized List<CommandSpec> registeredSpecs() {
        return List.copyOf(this.specsByRoot.values());
    }

    public synchronized List<RawCommandRegistration> rawRegistrations() {
        return List.copyOf(this.rawRegistrations);
    }

    public synchronized void clear() {
        this.specsByRoot.clear();
        this.rawRegistrations.clear();
        this.labels.clear();
    }

    private void ensureAvailable(final Collection<String> normalizedLabels) {
        for (final String label : normalizedLabels) {
            if (this.labels.contains(label)) {
                throw new IllegalStateException("Command label already registered: " + label);
            }
        }
    }

    private static List<String> collectLabels(final String root, final Collection<String> aliases) {
        final List<String> labels = new ArrayList<>();
        labels.add(normalize(root));
        for (final String alias : aliases) {
            final String normalized = normalize(alias);
            if (!labels.contains(normalized)) {
                labels.add(normalized);
            }
        }
        return labels;
    }

    private static String normalize(final String input) {
        Objects.requireNonNull(input, "input");
        final String normalized = input.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("command label cannot be blank");
        }
        return normalized;
    }
}
