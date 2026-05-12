package com.stephanofer.networkplatform.paper.config;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ConfigDocument {

    String scope();

    ConfigDocument root();

    ConfigDocument view(String relativePath);

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

    Set<String> keys();

    void set(String path, Object value);

    boolean remove(String path);

    void markDirty();

    <T> T unwrap(Class<T> type);
}
