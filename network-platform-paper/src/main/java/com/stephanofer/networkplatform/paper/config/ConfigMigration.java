package com.stephanofer.networkplatform.paper.config;

@FunctionalInterface
public interface ConfigMigration {

    void migrate(ConfigDocument document);
}
