package com.stephanofer.networkplatform.paper;

import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

/** Shared context exposed to base services and optional modules. */
public record PlatformContext(JavaPlugin plugin) {

    public PlatformContext {
        Objects.requireNonNull(plugin, "plugin");
    }

    public Server server() {
        return this.plugin.getServer();
    }

    public Logger logger() {
        return this.plugin.getLogger();
    }

    public Path dataDirectory() {
        return this.plugin.getDataFolder().toPath();
    }
}
