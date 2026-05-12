package com.stephanofer.networkplatform.paper;

import com.stephanofer.networkplatform.paper.command.CommandService;
import com.stephanofer.networkplatform.paper.command.internal.PaperCommandService;
import com.stephanofer.networkplatform.paper.config.ConfigService;
import com.stephanofer.networkplatform.paper.config.internal.DefaultConfigService;
import com.stephanofer.networkplatform.paper.lifecycle.Lifecycle;
import com.stephanofer.networkplatform.paper.lifecycle.PlatformLifecycle;
import com.stephanofer.networkplatform.paper.module.InstalledModuleRegistry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Lightweight entry point for plugins that consume NetworkPlatform.
 *
 * <p>This class is deliberately not a Bukkit/Paper plugin. Consumer plugins create their own
 * instance from their {@link JavaPlugin} and decide which optional modules to install.</p>
 */
public final class NetworkPlatform {

    private final PlatformContext context;
    private final PlatformLifecycle lifecycle;
    private final InstalledModuleRegistry modules;
    private final CommandService commands;
    private final ConfigService configs;
    private final AtomicBoolean shutdown;

    private NetworkPlatform(final JavaPlugin plugin) {
        this.context = new PlatformContext(plugin);
        this.lifecycle = new PlatformLifecycle(this.context.logger());
        this.modules = new InstalledModuleRegistry();
        this.commands = new PaperCommandService(plugin, this.context.logger());
        this.configs = new DefaultConfigService(
            this.context.dataDirectory(),
            plugin::getResource,
            this.context.logger(),
            this.lifecycle
        );
        this.shutdown = new AtomicBoolean(false);

        this.lifecycle.onShutdown(this.commands::clear);
    }

    public static NetworkPlatform create(final JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        return new NetworkPlatform(plugin);
    }

    public JavaPlugin plugin() {
        return this.context.plugin();
    }

    public PlatformContext context() {
        return this.context;
    }

    public Lifecycle lifecycle() {
        return this.lifecycle;
    }

    public InstalledModuleRegistry modules() {
        return this.modules;
    }

    public CommandService commands() {
        return this.commands;
    }

    public ConfigService configs() {
        return this.configs;
    }

    public boolean isShutdown() {
        return this.shutdown.get();
    }

    public void ensureActive() {
        if (isShutdown()) {
            throw new IllegalStateException("NetworkPlatform is already shut down");
        }
    }

    public void shutdown() {
        if (!this.shutdown.compareAndSet(false, true)) {
            return;
        }

        this.lifecycle.shutdown();
    }
}
