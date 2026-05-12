package com.stephanofer.networkplatform.paper.config;

public interface ConfigService {

    ConfigHandle file(String relativePath);

    ConfigHandle file(ConfigTemplate template);

    <T> TypedConfigHandle<T> file(ConfigTemplate template, Class<T> type);

    boolean isLoaded(String relativePath);

    void preload(ConfigTemplate... templates);

    void shutdown();
}
