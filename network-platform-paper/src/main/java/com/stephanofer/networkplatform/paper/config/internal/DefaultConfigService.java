package com.stephanofer.networkplatform.paper.config.internal;

import com.stephanofer.networkplatform.paper.config.ConfigException;
import com.stephanofer.networkplatform.paper.config.ConfigHandle;
import com.stephanofer.networkplatform.paper.config.ConfigService;
import com.stephanofer.networkplatform.paper.config.ConfigTemplate;
import com.stephanofer.networkplatform.paper.config.TypedConfigHandle;
import com.stephanofer.networkplatform.paper.lifecycle.Lifecycle;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

public final class DefaultConfigService implements ConfigService {

    private final Path dataDirectory;
    private final ConfigResourceResolver resources;
    private final Logger logger;
    private final ConfigRegistry registry;
    private final ConfigIoCoordinator ioCoordinator;

    public DefaultConfigService(
        final Path dataDirectory,
        final ConfigResourceResolver resources,
        final Logger logger,
        final Lifecycle lifecycle
    ) {
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory").normalize();
        this.resources = Objects.requireNonNull(resources, "resources");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.registry = new ConfigRegistry();
        this.ioCoordinator = new ConfigIoCoordinator();
        Objects.requireNonNull(lifecycle, "lifecycle").onShutdown(this::shutdown);
    }

    @Override
    public ConfigHandle file(final String relativePath) {
        return file(ConfigTemplate.of(relativePath));
    }

    @Override
    public ConfigHandle file(final ConfigTemplate template) {
        Objects.requireNonNull(template, "template");
        final DefaultConfigHandle existing = this.registry.find(template.path().value()).orElse(null);
        if (existing != null) {
            if (!existing.template().equals(template)) {
                throw new IllegalStateException("configuration template conflict for " + template.path().value());
            }
            return existing;
        }

        final DefaultConfigHandle created = new DefaultConfigHandle(
            template,
            template.path().resolveAgainst(this.dataDirectory),
            this.resources,
            this.ioCoordinator,
            this.logger
        );
        return this.registry.register(template, created);
    }

    @Override
    public <T> TypedConfigHandle<T> file(final ConfigTemplate template, final Class<T> type) {
        final ConfigHandle handle = file(template);
        final ConfigTemplate.TypedDefinition<T> typedDefinition = template.typedDefinition(type);
        return handle.typedSection(typedDefinition.sectionPath(), type, typedDefinition.mapper());
    }

    @Override
    public boolean isLoaded(final String relativePath) {
        return this.registry.isLoaded(ConfigTemplate.of(relativePath).path().value());
    }

    @Override
    public void preload(final ConfigTemplate... templates) {
        Objects.requireNonNull(templates, "templates");
        for (final ConfigTemplate template : templates) {
            file(template);
        }
    }

    @Override
    public void shutdown() {
        this.ioCoordinator.shutdown();
        this.registry.clear();
    }
}
