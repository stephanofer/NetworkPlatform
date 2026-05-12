package com.stephanofer.networkplatform.paper.config.internal;

import com.stephanofer.networkplatform.paper.config.ConfigDataMapper;
import com.stephanofer.networkplatform.paper.config.ConfigDocument;
import com.stephanofer.networkplatform.paper.config.ConfigException;
import com.stephanofer.networkplatform.paper.config.ConfigHandle;
import com.stephanofer.networkplatform.paper.config.ConfigTemplate;
import com.stephanofer.networkplatform.paper.config.TypedConfigHandle;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DefaultConfigHandle implements ConfigHandle {

    private static final DateTimeFormatter BACKUP_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private final ConfigTemplate template;
    private final Path path;
    private final ConfigResourceResolver resources;
    private final ConfigIoCoordinator ioCoordinator;
    private final Logger logger;
    private final DefaultConfigDocument documentView;
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private final AtomicLong revision = new AtomicLong();
    private volatile YamlDocument document;

    DefaultConfigHandle(
        final ConfigTemplate template,
        final Path path,
        final ConfigResourceResolver resources,
        final ConfigIoCoordinator ioCoordinator,
        final Logger logger
    ) {
        this.template = Objects.requireNonNull(template, "template");
        this.path = Objects.requireNonNull(path, "path");
        this.resources = Objects.requireNonNull(resources, "resources");
        this.ioCoordinator = Objects.requireNonNull(ioCoordinator, "ioCoordinator");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.documentView = new DefaultConfigDocument(this, "");
        reload();
    }

    @Override
    public Path path() {
        return this.path;
    }

    @Override
    public ConfigTemplate template() {
        return this.template;
    }

    @Override
    public ConfigDocument document() {
        return this.documentView;
    }

    YamlDocument rawDocument() {
        return Objects.requireNonNull(this.document, "configuration document not loaded");
    }

    long revision() {
        return this.revision.get();
    }

    void touch() {
        markDirty();
    }

    @Override
    public boolean exists() {
        return Files.exists(this.path);
    }

    @Override
    public boolean isDirty() {
        return this.dirty.get();
    }

    @Override
    public void markDirty() {
        this.dirty.set(true);
        this.revision.incrementAndGet();
    }

    @Override
    public void reload() {
        this.ioCoordinator.execute(this.path, () -> {
            loadIntoMemory();
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> reloadAsync() {
        return this.ioCoordinator.executeAsync(this.path, () -> {
            loadIntoMemory();
            return null;
        });
    }

    @Override
    public void save() {
        this.ioCoordinator.execute(this.path, () -> {
            saveInternal(false);
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> saveAsync() {
        return this.ioCoordinator.executeAsync(this.path, () -> {
            saveInternal(false);
            return null;
        });
    }

    @Override
    public void flush() {
        this.ioCoordinator.execute(this.path, () -> {
            saveInternal(true);
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> flushAsync() {
        return this.ioCoordinator.executeAsync(this.path, () -> {
            saveInternal(true);
            return null;
        });
    }

    @Override
    public boolean update() {
        return this.ioCoordinator.execute(this.path, this::updateInternal);
    }

    @Override
    public CompletableFuture<Boolean> updateAsync() {
        return this.ioCoordinator.executeAsync(this.path, this::updateInternal);
    }

    @Override
    public boolean contains(final String path) {
        return this.documentView.contains(path);
    }

    @Override
    public Object get(final String path) {
        return this.documentView.get(path);
    }

    @Override
    public Object get(final String path, final Object defaultValue) {
        return this.documentView.get(path, defaultValue);
    }

    @Override
    public Optional<String> getOptionalString(final String path) {
        return this.documentView.getOptionalString(path);
    }

    @Override
    public String getString(final String path) {
        return this.documentView.getString(path);
    }

    @Override
    public String getString(final String path, final String defaultValue) {
        return this.documentView.getString(path, defaultValue);
    }

    @Override
    public Optional<Integer> getOptionalInt(final String path) {
        return this.documentView.getOptionalInt(path);
    }

    @Override
    public int getInt(final String path) {
        return this.documentView.getInt(path);
    }

    @Override
    public int getInt(final String path, final int defaultValue) {
        return this.documentView.getInt(path, defaultValue);
    }

    @Override
    public Optional<Long> getOptionalLong(final String path) {
        return this.documentView.getOptionalLong(path);
    }

    @Override
    public long getLong(final String path) {
        return this.documentView.getLong(path);
    }

    @Override
    public long getLong(final String path, final long defaultValue) {
        return this.documentView.getLong(path, defaultValue);
    }

    @Override
    public Optional<Double> getOptionalDouble(final String path) {
        return this.documentView.getOptionalDouble(path);
    }

    @Override
    public double getDouble(final String path) {
        return this.documentView.getDouble(path);
    }

    @Override
    public double getDouble(final String path, final double defaultValue) {
        return this.documentView.getDouble(path, defaultValue);
    }

    @Override
    public Optional<Boolean> getOptionalBoolean(final String path) {
        return this.documentView.getOptionalBoolean(path);
    }

    @Override
    public boolean getBoolean(final String path) {
        return this.documentView.getBoolean(path);
    }

    @Override
    public boolean getBoolean(final String path, final boolean defaultValue) {
        return this.documentView.getBoolean(path, defaultValue);
    }

    @Override
    public List<String> getStringList(final String path) {
        return this.documentView.getStringList(path);
    }

    @Override
    public List<String> getStringList(final String path, final List<String> defaultValue) {
        return this.documentView.getStringList(path, defaultValue);
    }

    @Override
    public <T> T map(final ConfigDataMapper<T> mapper) {
        return Objects.requireNonNull(mapper, "mapper").map(this.documentView);
    }

    @Override
    public <T> T mapSection(final String sectionPath, final ConfigDataMapper<T> mapper) {
        return Objects.requireNonNull(mapper, "mapper").map(this.documentView.view(sectionPath));
    }

    @Override
    public <T> TypedConfigHandle<T> typed(final Class<T> type, final ConfigDataMapper<T> mapper) {
        return new DefaultTypedConfigHandle<>(this, type, "", mapper);
    }

    @Override
    public <T> TypedConfigHandle<T> typedSection(final String sectionPath, final Class<T> type, final ConfigDataMapper<T> mapper) {
        return new DefaultTypedConfigHandle<>(this, type, sectionPath, mapper);
    }

    private void loadIntoMemory() {
        final byte[] original = readFileBytesIfPresent();
        try {
            final LoadResult result = createDocumentAndApplyLoadUpdate(original);
            this.document = result.document();
            this.dirty.set(false);
            this.revision.incrementAndGet();
            runHooks(this.template.afterLoadHooks());
            if (result.updated()) {
                runHooks(this.template.afterUpdateHooks());
            }
        } catch (final Exception exception) {
            recoverFromBrokenFile(exception, original);
        }
    }

    private boolean updateInternal() {
        if (this.template.resourcePath() == null) {
            return false;
        }

        final byte[] original = readFileBytesIfPresent();
        try (InputStream defaults = openDefaults()) {
            final YamlDocument current = rawDocument();
            final UpdaterSettings updaterSettings = ConfigTemplateSupport.buildUpdaterSettings(this);
            final String before = current.dump();
            current.update(defaults, updaterSettings);
            alignVersionKey(current);
            final boolean updated = !before.equals(current.dump());
            if (!updated) {
                return false;
            }

            if (this.template.backupBeforeUpdate() && original != null) {
                createBackup("update", original);
            }

            this.dirty.set(true);
            this.revision.incrementAndGet();
            runHooks(this.template.afterUpdateHooks());
            saveInternal(true);
            return true;
        } catch (final Exception exception) {
            throw new ConfigException("failed to update configuration " + this.path, exception);
        }
    }

    private void saveInternal(final boolean onlyIfDirty) {
        if (onlyIfDirty && !this.dirty.get()) {
            return;
        }

        runHooks(this.template.beforeSaveHooks());
        try {
            writeDocumentToPath(rawDocument());
            this.dirty.set(false);
        } catch (final Exception exception) {
            throw new ConfigException("failed to save configuration " + this.path, exception);
        }
    }

    private LoadResult createDocumentAndApplyLoadUpdate(final byte[] originalBytes) throws Exception {
        ensureParentDirectory();
        final boolean filePreviouslyExisted = Files.exists(this.path);
        final GeneralSettings generalSettings = ConfigTemplateSupport.buildGeneralSettings(this.template);
        final LoaderSettings loaderSettings = ConfigTemplateSupport.buildLoaderSettings(this.template, this.path.toString());
        final DumperSettings dumperSettings = ConfigTemplateSupport.buildDumperSettings();
        final UpdaterSettings updaterSettings = ConfigTemplateSupport.buildUpdaterSettings(this);

        final YamlDocument loaded;
        try (InputStream documentInput = openDocumentInput(); InputStream defaults = openDefaultsNullable()) {
            loaded = defaults == null
                ? YamlDocument.create(documentInput, generalSettings, loaderSettings, dumperSettings, updaterSettings)
                : YamlDocument.create(documentInput, defaults, generalSettings, loaderSettings, dumperSettings, updaterSettings);
        }

        this.document = loaded;

        boolean updated = false;
        if (this.template.updateOnLoad() && this.template.resourcePath() != null) {
            try (InputStream defaults = openDefaults()) {
                final String before = loaded.dump();
                loaded.update(defaults, updaterSettings);
                alignVersionKey(loaded);
                updated = !before.equals(loaded.dump());
            }

            if (updated) {
                if (this.template.backupBeforeUpdate() && originalBytes != null) {
                    createBackup("update", originalBytes);
                }
                writeDocumentToPath(loaded);
            }
        }

        if (!filePreviouslyExisted && this.template.createIfAbsent()) {
            writeDocumentToPath(loaded);
        }

        return new LoadResult(loaded, updated);
    }

    private void recoverFromBrokenFile(final Exception exception, final byte[] originalBytes) {
        if (this.template.backupOnInvalidLoad() && originalBytes != null) {
            try {
                createBackup("broken", originalBytes);
            } catch (final IOException backupException) {
                this.logger.log(Level.SEVERE, "Failed to create backup for broken config " + this.path, backupException);
            }
        }

        this.logger.log(Level.SEVERE, "Failed to load configuration " + this.path + ". Attempting recovery.", exception);
        try {
            rewriteRecoverySeed();
            final LoadResult recovered = createDocumentAndApplyLoadUpdate(null);
            this.document = recovered.document();
            this.dirty.set(false);
            this.revision.incrementAndGet();
            runHooks(this.template.afterLoadHooks());
            if (recovered.updated()) {
                runHooks(this.template.afterUpdateHooks());
            }
        } catch (final Exception recoveryFailure) {
            throw new ConfigException("failed to recover configuration " + this.path, recoveryFailure);
        }
    }

    private byte[] readFileBytesIfPresent() {
        try {
            return Files.exists(this.path) ? Files.readAllBytes(this.path) : null;
        } catch (final IOException exception) {
            throw new ConfigException("failed to read configuration bytes for " + this.path, exception);
        }
    }

    private void ensureParentDirectory() throws IOException {
        final Path parent = this.path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private void runHooks(final List<com.stephanofer.networkplatform.paper.config.ConfigHook> hooks) {
        for (final com.stephanofer.networkplatform.paper.config.ConfigHook hook : hooks) {
            try {
                hook.run(this);
            } catch (final RuntimeException exception) {
                throw new ConfigException("configuration hook failed for " + this.path, exception);
            }
        }
    }

    private InputStream openDefaults() {
        final InputStream input = openDefaultsNullable();
        if (input == null) {
            throw new ConfigException("default resource not found for " + this.template.path().value() + ": " + this.template.resourcePath());
        }
        return input;
    }

    private InputStream openDefaultsNullable() {
        return this.template.resourcePath() == null ? null : this.resources.open(this.template.resourcePath());
    }

    private void createBackup(final String reason, final byte[] bytes) throws IOException {
        ensureParentDirectory();
        final String fileName = this.path.getFileName().toString();
        final Path backup = this.path.resolveSibling(fileName + "." + reason + "-" + LocalDateTime.now().format(BACKUP_TIMESTAMP) + ".bak");
        Files.write(backup, bytes);
    }

    private void rewriteRecoverySeed() throws IOException {
        ensureParentDirectory();
        try (InputStream defaults = openDefaultsNullable()) {
            if (defaults != null) {
                Files.write(this.path, defaults.readAllBytes());
                return;
            }
        }

        Files.write(this.path, new byte[0]);
    }

    private InputStream openDocumentInput() throws IOException {
        if (Files.exists(this.path)) {
            return new java.io.ByteArrayInputStream(Files.readAllBytes(this.path));
        }

        final InputStream defaults = openDefaultsNullable();
        if (defaults != null) {
            try (defaults) {
                return new java.io.ByteArrayInputStream(defaults.readAllBytes());
            }
        }

        return new java.io.ByteArrayInputStream(new byte[0]);
    }

    private void writeDocumentToPath(final YamlDocument document) throws IOException {
        ensureParentDirectory();
        Files.writeString(this.path, document.dump(), java.nio.charset.StandardCharsets.UTF_8);
    }

    private void alignVersionKey(final YamlDocument document) {
        if (this.template.versionKey() == null || document.getDefaults() == null) {
            return;
        }

        final Object latestVersion = document.getDefaults().get(this.template.versionKey(), null);
        if (latestVersion != null) {
            document.set(this.template.versionKey(), latestVersion);
        }
    }

    private record LoadResult(YamlDocument document, boolean updated) {
    }
}
