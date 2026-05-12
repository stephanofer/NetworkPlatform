package com.stephanofer.networkplatform.paper.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ConfigHandle {

    Path path();

    ConfigTemplate template();

    ConfigDocument document();

    boolean exists();

    boolean isDirty();

    void markDirty();

    void reload();

    CompletableFuture<Void> reloadAsync();

    void save();

    CompletableFuture<Void> saveAsync();

    void flush();

    CompletableFuture<Void> flushAsync();

    boolean update();

    CompletableFuture<Boolean> updateAsync();

    boolean contains(String path);

    Object get(String path);

    Object get(String path, Object defaultValue);

    Optional<String> getOptionalString(String path);

    String getString(String path);

    String getString(String path, String defaultValue);

    Optional<Integer> getOptionalInt(String path);

    int getInt(String path);

    int getInt(String path, int defaultValue);

    Optional<Long> getOptionalLong(String path);

    long getLong(String path);

    long getLong(String path, long defaultValue);

    Optional<Double> getOptionalDouble(String path);

    double getDouble(String path);

    double getDouble(String path, double defaultValue);

    Optional<Boolean> getOptionalBoolean(String path);

    boolean getBoolean(String path);

    boolean getBoolean(String path, boolean defaultValue);

    List<String> getStringList(String path);

    List<String> getStringList(String path, List<String> defaultValue);

    <T> T map(ConfigDataMapper<T> mapper);

    <T> T mapSection(String sectionPath, ConfigDataMapper<T> mapper);

    <T> TypedConfigHandle<T> typed(Class<T> type, ConfigDataMapper<T> mapper);

    <T> TypedConfigHandle<T> typedSection(String sectionPath, Class<T> type, ConfigDataMapper<T> mapper);
}
