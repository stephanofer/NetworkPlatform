package com.stephanofer.networkplatform.paper.config.internal;

import com.stephanofer.networkplatform.paper.config.ConfigDataMapper;
import com.stephanofer.networkplatform.paper.config.TypedConfigHandle;
import java.util.Objects;

public final class DefaultTypedConfigHandle<T> implements TypedConfigHandle<T> {

    private final DefaultConfigHandle owner;
    private final Class<T> type;
    private final String snapshotPath;
    private final ConfigDataMapper<T> mapper;
    private volatile long revision = Long.MIN_VALUE;
    private volatile T cachedSnapshot;

    DefaultTypedConfigHandle(
        final DefaultConfigHandle owner,
        final Class<T> type,
        final String snapshotPath,
        final ConfigDataMapper<T> mapper
    ) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.type = Objects.requireNonNull(type, "type");
        this.snapshotPath = snapshotPath == null || snapshotPath.isBlank() ? "" : snapshotPath.trim();
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public Class<T> type() {
        return this.type;
    }

    @Override
    public String snapshotPath() {
        return this.snapshotPath;
    }

    @Override
    public T snapshot() {
        final long currentRevision = this.owner.revision();
        final T current = this.cachedSnapshot;
        if (current != null && currentRevision == this.revision) {
            return current;
        }
        return refreshSnapshot();
    }

    @Override
    public synchronized T refreshSnapshot() {
        final T snapshot = this.mapper.map(this.snapshotPath.isBlank() ? this.owner.document() : this.owner.document().view(this.snapshotPath));
        this.cachedSnapshot = snapshot;
        this.revision = this.owner.revision();
        return snapshot;
    }

    @Override
    public java.nio.file.Path path() {
        return this.owner.path();
    }

    @Override
    public com.stephanofer.networkplatform.paper.config.ConfigTemplate template() {
        return this.owner.template();
    }

    @Override
    public com.stephanofer.networkplatform.paper.config.ConfigDocument document() {
        return this.owner.document();
    }

    @Override
    public boolean exists() {
        return this.owner.exists();
    }

    @Override
    public boolean isDirty() {
        return this.owner.isDirty();
    }

    @Override
    public void markDirty() {
        this.owner.markDirty();
    }

    @Override
    public void reload() {
        this.owner.reload();
    }

    @Override
    public java.util.concurrent.CompletableFuture<Void> reloadAsync() {
        return this.owner.reloadAsync();
    }

    @Override
    public void save() {
        this.owner.save();
    }

    @Override
    public java.util.concurrent.CompletableFuture<Void> saveAsync() {
        return this.owner.saveAsync();
    }

    @Override
    public void flush() {
        this.owner.flush();
    }

    @Override
    public java.util.concurrent.CompletableFuture<Void> flushAsync() {
        return this.owner.flushAsync();
    }

    @Override
    public boolean update() {
        return this.owner.update();
    }

    @Override
    public java.util.concurrent.CompletableFuture<Boolean> updateAsync() {
        return this.owner.updateAsync();
    }

    @Override
    public boolean contains(final String path) {
        return this.owner.contains(path);
    }

    @Override
    public Object get(final String path) {
        return this.owner.get(path);
    }

    @Override
    public Object get(final String path, final Object defaultValue) {
        return this.owner.get(path, defaultValue);
    }

    @Override
    public java.util.Optional<String> getOptionalString(final String path) {
        return this.owner.getOptionalString(path);
    }

    @Override
    public String getString(final String path) {
        return this.owner.getString(path);
    }

    @Override
    public String getString(final String path, final String defaultValue) {
        return this.owner.getString(path, defaultValue);
    }

    @Override
    public java.util.Optional<Integer> getOptionalInt(final String path) {
        return this.owner.getOptionalInt(path);
    }

    @Override
    public int getInt(final String path) {
        return this.owner.getInt(path);
    }

    @Override
    public int getInt(final String path, final int defaultValue) {
        return this.owner.getInt(path, defaultValue);
    }

    @Override
    public java.util.Optional<Long> getOptionalLong(final String path) {
        return this.owner.getOptionalLong(path);
    }

    @Override
    public long getLong(final String path) {
        return this.owner.getLong(path);
    }

    @Override
    public long getLong(final String path, final long defaultValue) {
        return this.owner.getLong(path, defaultValue);
    }

    @Override
    public java.util.Optional<Double> getOptionalDouble(final String path) {
        return this.owner.getOptionalDouble(path);
    }

    @Override
    public double getDouble(final String path) {
        return this.owner.getDouble(path);
    }

    @Override
    public double getDouble(final String path, final double defaultValue) {
        return this.owner.getDouble(path, defaultValue);
    }

    @Override
    public java.util.Optional<Boolean> getOptionalBoolean(final String path) {
        return this.owner.getOptionalBoolean(path);
    }

    @Override
    public boolean getBoolean(final String path) {
        return this.owner.getBoolean(path);
    }

    @Override
    public boolean getBoolean(final String path, final boolean defaultValue) {
        return this.owner.getBoolean(path, defaultValue);
    }

    @Override
    public java.util.List<String> getStringList(final String path) {
        return this.owner.getStringList(path);
    }

    @Override
    public java.util.List<String> getStringList(final String path, final java.util.List<String> defaultValue) {
        return this.owner.getStringList(path, defaultValue);
    }

    @Override
    public <R> R map(final com.stephanofer.networkplatform.paper.config.ConfigDataMapper<R> mapper) {
        return this.owner.map(mapper);
    }

    @Override
    public <R> R mapSection(final String sectionPath, final com.stephanofer.networkplatform.paper.config.ConfigDataMapper<R> mapper) {
        return this.owner.mapSection(sectionPath, mapper);
    }

    @Override
    public <R> TypedConfigHandle<R> typed(final Class<R> type, final com.stephanofer.networkplatform.paper.config.ConfigDataMapper<R> mapper) {
        return this.owner.typed(type, mapper);
    }

    @Override
    public <R> TypedConfigHandle<R> typedSection(
        final String sectionPath,
        final Class<R> type,
        final com.stephanofer.networkplatform.paper.config.ConfigDataMapper<R> mapper
    ) {
        return this.owner.typedSection(sectionPath, type, mapper);
    }
}
