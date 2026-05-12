package com.stephanofer.networkplatform.paper.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ConfigTemplate {

    private final ConfigPath path;
    private final String resourcePath;
    private final String versionKey;
    private final boolean createIfAbsent;
    private final boolean updateOnLoad;
    private final boolean keepAll;
    private final boolean allowDowngrading;
    private final boolean allowDuplicateKeys;
    private final boolean backupOnInvalidLoad;
    private final boolean backupBeforeUpdate;
    private final boolean sortByDefaults;
    private final Map<String, Set<String>> ignoredRoutes;
    private final Map<String, Map<String, String>> relocations;
    private final Map<String, Map<String, ConfigValueMapper>> valueMappers;
    private final Map<String, List<ConfigMigration>> migrations;
    private final List<ConfigHook> afterLoadHooks;
    private final List<ConfigHook> beforeSaveHooks;
    private final List<ConfigHook> afterUpdateHooks;
    private final Map<Class<?>, ConfigTypeAdapter<?>> typeAdapters;
    private final Map<String, Class<?>> typeAliases;
    private final TypedDefinition<?> typedDefinition;

    private ConfigTemplate(final Builder builder) {
        this.path = builder.path;
        this.resourcePath = builder.resourcePath;
        this.versionKey = builder.versionKey;
        this.createIfAbsent = builder.createIfAbsent;
        this.updateOnLoad = builder.updateOnLoad;
        this.keepAll = builder.keepAll;
        this.allowDowngrading = builder.allowDowngrading;
        this.allowDuplicateKeys = builder.allowDuplicateKeys;
        this.backupOnInvalidLoad = builder.backupOnInvalidLoad;
        this.backupBeforeUpdate = builder.backupBeforeUpdate;
        this.sortByDefaults = builder.sortByDefaults;
        this.ignoredRoutes = immutableNestedSet(builder.ignoredRoutes);
        this.relocations = immutableNestedMap(builder.relocations);
        this.valueMappers = immutableNestedMap(builder.valueMappers);
        this.migrations = immutableNestedList(builder.migrations);
        this.afterLoadHooks = List.copyOf(builder.afterLoadHooks);
        this.beforeSaveHooks = List.copyOf(builder.beforeSaveHooks);
        this.afterUpdateHooks = List.copyOf(builder.afterUpdateHooks);
        this.typeAdapters = Collections.unmodifiableMap(new LinkedHashMap<>(builder.typeAdapters));
        this.typeAliases = Collections.unmodifiableMap(new LinkedHashMap<>(builder.typeAliases));
        this.typedDefinition = builder.typedDefinition;
    }

    public static Builder builder(final String relativePath) {
        return new Builder(ConfigPath.of(relativePath));
    }

    public static ConfigTemplate of(final String relativePath) {
        return builder(relativePath).build();
    }

    public ConfigPath path() {
        return this.path;
    }

    public String resourcePath() {
        return this.resourcePath;
    }

    public String versionKey() {
        return this.versionKey;
    }

    public boolean createIfAbsent() {
        return this.createIfAbsent;
    }

    public boolean updateOnLoad() {
        return this.updateOnLoad;
    }

    public boolean keepAll() {
        return this.keepAll;
    }

    public boolean allowDowngrading() {
        return this.allowDowngrading;
    }

    public boolean allowDuplicateKeys() {
        return this.allowDuplicateKeys;
    }

    public boolean backupOnInvalidLoad() {
        return this.backupOnInvalidLoad;
    }

    public boolean backupBeforeUpdate() {
        return this.backupBeforeUpdate;
    }

    public boolean sortByDefaults() {
        return this.sortByDefaults;
    }

    public Map<String, Set<String>> ignoredRoutes() {
        return this.ignoredRoutes;
    }

    public Map<String, Map<String, String>> relocations() {
        return this.relocations;
    }

    public Map<String, Map<String, ConfigValueMapper>> valueMappers() {
        return this.valueMappers;
    }

    public Map<String, List<ConfigMigration>> migrations() {
        return this.migrations;
    }

    public List<ConfigHook> afterLoadHooks() {
        return this.afterLoadHooks;
    }

    public List<ConfigHook> beforeSaveHooks() {
        return this.beforeSaveHooks;
    }

    public List<ConfigHook> afterUpdateHooks() {
        return this.afterUpdateHooks;
    }

    public Map<Class<?>, ConfigTypeAdapter<?>> typeAdapters() {
        return this.typeAdapters;
    }

    public Map<String, Class<?>> typeAliases() {
        return this.typeAliases;
    }

    public boolean hasTypedDefinition() {
        return this.typedDefinition != null;
    }

    TypedDefinition<?> typedDefinition() {
        return this.typedDefinition;
    }

    public <T> TypedDefinition<T> typedDefinition(final Class<T> type) {
        Objects.requireNonNull(type, "type");
        if (this.typedDefinition == null) {
            throw new IllegalStateException("template does not declare a typed mapping: " + this.path.value());
        }
        if (!type.equals(this.typedDefinition.type())) {
            throw new IllegalArgumentException("template typed mapping is registered for "
                + this.typedDefinition.type().getName() + ", not " + type.getName());
        }
        @SuppressWarnings("unchecked") final TypedDefinition<T> cast = (TypedDefinition<T>) this.typedDefinition;
        return cast;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ConfigTemplate that)) {
            return false;
        }
        return this.createIfAbsent == that.createIfAbsent
            && this.updateOnLoad == that.updateOnLoad
            && this.keepAll == that.keepAll
            && this.allowDowngrading == that.allowDowngrading
            && this.allowDuplicateKeys == that.allowDuplicateKeys
            && this.backupOnInvalidLoad == that.backupOnInvalidLoad
            && this.backupBeforeUpdate == that.backupBeforeUpdate
            && this.sortByDefaults == that.sortByDefaults
            && this.path.equals(that.path)
            && Objects.equals(this.resourcePath, that.resourcePath)
            && Objects.equals(this.versionKey, that.versionKey)
            && this.ignoredRoutes.equals(that.ignoredRoutes)
            && this.relocations.equals(that.relocations)
            && this.valueMappers.equals(that.valueMappers)
            && this.migrations.equals(that.migrations)
            && this.afterLoadHooks.equals(that.afterLoadHooks)
            && this.beforeSaveHooks.equals(that.beforeSaveHooks)
            && this.afterUpdateHooks.equals(that.afterUpdateHooks)
            && this.typeAdapters.equals(that.typeAdapters)
            && this.typeAliases.equals(that.typeAliases)
            && Objects.equals(this.typedDefinition, that.typedDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.path,
            this.resourcePath,
            this.versionKey,
            this.createIfAbsent,
            this.updateOnLoad,
            this.keepAll,
            this.allowDowngrading,
            this.allowDuplicateKeys,
            this.backupOnInvalidLoad,
            this.backupBeforeUpdate,
            this.sortByDefaults,
            this.ignoredRoutes,
            this.relocations,
            this.valueMappers,
            this.migrations,
            this.afterLoadHooks,
            this.beforeSaveHooks,
            this.afterUpdateHooks,
            this.typeAdapters,
            this.typeAliases,
            this.typedDefinition
        );
    }

    public static final class TypedDefinition<T> {

        private final Class<T> type;
        private final String sectionPath;
        private final ConfigDataMapper<T> mapper;

        private TypedDefinition(final Class<T> type, final String sectionPath, final ConfigDataMapper<T> mapper) {
            this.type = Objects.requireNonNull(type, "type");
            this.sectionPath = sectionPath == null || sectionPath.isBlank() ? "" : sectionPath.trim();
            this.mapper = Objects.requireNonNull(mapper, "mapper");
        }

        public Class<T> type() {
            return this.type;
        }

        public String sectionPath() {
            return this.sectionPath;
        }

        public ConfigDataMapper<T> mapper() {
            return this.mapper;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TypedDefinition<?> that)) {
                return false;
            }
            return this.type.equals(that.type) && this.sectionPath.equals(that.sectionPath) && this.mapper.equals(that.mapper);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.type, this.sectionPath, this.mapper);
        }
    }

    public static final class Builder {

        private final ConfigPath path;
        private String resourcePath;
        private String versionKey = "file-version";
        private boolean createIfAbsent = true;
        private boolean updateOnLoad = true;
        private boolean keepAll = true;
        private boolean allowDowngrading = true;
        private boolean allowDuplicateKeys = false;
        private boolean backupOnInvalidLoad = true;
        private boolean backupBeforeUpdate = false;
        private boolean sortByDefaults = true;
        private final Map<String, Set<String>> ignoredRoutes = new LinkedHashMap<>();
        private final Map<String, Map<String, String>> relocations = new LinkedHashMap<>();
        private final Map<String, Map<String, ConfigValueMapper>> valueMappers = new LinkedHashMap<>();
        private final Map<String, List<ConfigMigration>> migrations = new LinkedHashMap<>();
        private final List<ConfigHook> afterLoadHooks = new ArrayList<>();
        private final List<ConfigHook> beforeSaveHooks = new ArrayList<>();
        private final List<ConfigHook> afterUpdateHooks = new ArrayList<>();
        private final Map<Class<?>, ConfigTypeAdapter<?>> typeAdapters = new LinkedHashMap<>();
        private final Map<String, Class<?>> typeAliases = new LinkedHashMap<>();
        private TypedDefinition<?> typedDefinition;

        private Builder(final ConfigPath path) {
            this.path = path;
            this.resourcePath = path.value();
        }

        public Builder resourcePath(final String resourcePath) {
            this.resourcePath = resourcePath == null || resourcePath.isBlank() ? null : resourcePath.trim().replace('\\', '/');
            return this;
        }

        public Builder withoutDefaults() {
            this.resourcePath = null;
            this.versionKey = null;
            return this;
        }

        public Builder versionKey(final String versionKey) {
            this.versionKey = versionKey == null || versionKey.isBlank() ? null : versionKey.trim();
            return this;
        }

        public Builder createIfAbsent(final boolean createIfAbsent) {
            this.createIfAbsent = createIfAbsent;
            return this;
        }

        public Builder updateOnLoad(final boolean updateOnLoad) {
            this.updateOnLoad = updateOnLoad;
            return this;
        }

        public Builder keepAll(final boolean keepAll) {
            this.keepAll = keepAll;
            return this;
        }

        public Builder allowDowngrading(final boolean allowDowngrading) {
            this.allowDowngrading = allowDowngrading;
            return this;
        }

        public Builder allowDuplicateKeys(final boolean allowDuplicateKeys) {
            this.allowDuplicateKeys = allowDuplicateKeys;
            return this;
        }

        public Builder backupOnInvalidLoad(final boolean backupOnInvalidLoad) {
            this.backupOnInvalidLoad = backupOnInvalidLoad;
            return this;
        }

        public Builder backupBeforeUpdate(final boolean backupBeforeUpdate) {
            this.backupBeforeUpdate = backupBeforeUpdate;
            return this;
        }

        public Builder sortByDefaults(final boolean sortByDefaults) {
            this.sortByDefaults = sortByDefaults;
            return this;
        }

        public Builder ignoreRoute(final String versionId, final String route) {
            return ignoreRoutes(versionId, Set.of(route));
        }

        public Builder ignoreRoutes(final String versionId, final Collection<String> routes) {
            final String normalizedVersion = requireVersion(versionId);
            final Set<String> normalizedRoutes = this.ignoredRoutes.computeIfAbsent(normalizedVersion, ignored -> new LinkedHashSet<>());
            for (final String route : routes) {
                normalizedRoutes.add(requireRoute(route));
            }
            return this;
        }

        public Builder relocate(final String versionId, final String fromRoute, final String toRoute) {
            final String normalizedVersion = requireVersion(versionId);
            this.relocations.computeIfAbsent(normalizedVersion, ignored -> new LinkedHashMap<>())
                .put(requireRoute(fromRoute), requireRoute(toRoute));
            return this;
        }

        public Builder mapValue(final String versionId, final String route, final ConfigValueMapper mapper) {
            final String normalizedVersion = requireVersion(versionId);
            this.valueMappers.computeIfAbsent(normalizedVersion, ignored -> new LinkedHashMap<>())
                .put(requireRoute(route), Objects.requireNonNull(mapper, "mapper"));
            return this;
        }

        public Builder migrate(final String versionId, final ConfigMigration migration) {
            final String normalizedVersion = requireVersion(versionId);
            this.migrations.computeIfAbsent(normalizedVersion, ignored -> new ArrayList<>())
                .add(Objects.requireNonNull(migration, "migration"));
            return this;
        }

        public Builder afterLoad(final ConfigHook hook) {
            this.afterLoadHooks.add(Objects.requireNonNull(hook, "hook"));
            return this;
        }

        public Builder beforeSave(final ConfigHook hook) {
            this.beforeSaveHooks.add(Objects.requireNonNull(hook, "hook"));
            return this;
        }

        public Builder afterUpdate(final ConfigHook hook) {
            this.afterUpdateHooks.add(Objects.requireNonNull(hook, "hook"));
            return this;
        }

        public <T> Builder registerType(final Class<T> type, final ConfigTypeAdapter<T> adapter) {
            this.typeAdapters.put(Objects.requireNonNull(type, "type"), Objects.requireNonNull(adapter, "adapter"));
            return this;
        }

        public Builder registerTypeAlias(final String alias, final Class<?> type) {
            this.typeAliases.put(requireVersion(alias), Objects.requireNonNull(type, "type"));
            return this;
        }

        public <T> Builder typed(final Class<T> type, final ConfigDataMapper<T> mapper) {
            this.typedDefinition = new TypedDefinition<>(type, "", mapper);
            return this;
        }

        public <T> Builder typedSection(final String sectionPath, final Class<T> type, final ConfigDataMapper<T> mapper) {
            this.typedDefinition = new TypedDefinition<>(type, requireRoute(sectionPath), mapper);
            return this;
        }

        public ConfigTemplate build() {
            if (this.resourcePath == null) {
                this.versionKey = null;
            }
            return new ConfigTemplate(this);
        }

        private static String requireVersion(final String versionId) {
            Objects.requireNonNull(versionId, "versionId");
            final String normalized = versionId.trim();
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException("version id cannot be blank");
            }
            return normalized;
        }

        private static String requireRoute(final String route) {
            Objects.requireNonNull(route, "route");
            final String normalized = route.trim();
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException("route cannot be blank");
            }
            return normalized;
        }
    }

    private static <T> Map<String, List<T>> immutableNestedList(final Map<String, List<T>> source) {
        final Map<String, List<T>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        return Collections.unmodifiableMap(copy);
    }

    private static Map<String, Set<String>> immutableNestedSet(final Map<String, Set<String>> source) {
        final Map<String, Set<String>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, Set.copyOf(value)));
        return Collections.unmodifiableMap(copy);
    }

    private static <T> Map<String, Map<String, T>> immutableNestedMap(final Map<String, Map<String, T>> source) {
        final Map<String, Map<String, T>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, Collections.unmodifiableMap(new LinkedHashMap<>(value))));
        return Collections.unmodifiableMap(copy);
    }
}
