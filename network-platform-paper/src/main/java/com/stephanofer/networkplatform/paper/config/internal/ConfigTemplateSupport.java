package com.stephanofer.networkplatform.paper.config.internal;

import com.stephanofer.networkplatform.paper.config.ConfigException;
import com.stephanofer.networkplatform.paper.config.ConfigMigration;
import com.stephanofer.networkplatform.paper.config.ConfigTemplate;
import com.stephanofer.networkplatform.paper.config.ConfigTypeAdapter;
import com.stephanofer.networkplatform.paper.config.ConfigValueMapper;
import dev.dejvokep.boostedyaml.serialization.standard.StandardSerializer;
import dev.dejvokep.boostedyaml.serialization.standard.TypeAdapter;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.settings.updater.ValueMapper;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import java.util.List;
import java.util.Map;

final class ConfigTemplateSupport {

    private ConfigTemplateSupport() {
    }

    static GeneralSettings buildGeneralSettings(final ConfigTemplate template) {
        final StandardSerializer serializer = new StandardSerializer(StandardSerializer.DEFAULT_SERIALIZED_TYPE_KEY);
        registerTypeAdapters(template, serializer);
        return GeneralSettings.builder()
            .setUseDefaults(false)
            .setSerializer(serializer)
            .build();
    }

    static LoaderSettings buildLoaderSettings(final ConfigTemplate template, final String label) {
        return LoaderSettings.builder()
            .setCreateFileIfAbsent(template.createIfAbsent())
            .setAutoUpdate(false)
            .setAllowDuplicateKeys(template.allowDuplicateKeys())
            .setDetailedErrors(true)
            .setErrorLabel(label)
            .build();
    }

    static DumperSettings buildDumperSettings() {
        return DumperSettings.builder()
            .setIndentation(2)
            .build();
    }

    static UpdaterSettings buildUpdaterSettings(final DefaultConfigHandle handle) {
        final ConfigTemplate template = handle.template();
        final UpdaterSettings.Builder builder = UpdaterSettings.builder()
            .setAutoSave(false)
            .setKeepAll(template.keepAll())
            .setEnableDowngrading(template.allowDowngrading())
            .setOptionSorting(template.sortByDefaults()
                ? UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS
                : UpdaterSettings.OptionSorting.NONE);

        if (template.versionKey() != null) {
            builder.setVersioning(new BasicVersioning(template.versionKey()));
        }

        template.ignoredRoutes().forEach((version, routes) -> builder.addIgnoredRoutes(version, routes, '.'));
        template.relocations().forEach((version, routes) -> builder.addRelocations(version, routes, '.'));
        template.valueMappers().forEach((version, routes) -> builder.addMappers(version, mapValueMappers(handle, routes), '.'));
        template.migrations().forEach((version, migrations) -> builder.addCustomLogic(version, document -> runMigrations(handle, migrations)));
        return builder.build();
    }

    private static Map<String, ValueMapper> mapValueMappers(
        final DefaultConfigHandle handle,
        final Map<String, ConfigValueMapper> routes
    ) {
        final java.util.LinkedHashMap<String, ValueMapper> mapped = new java.util.LinkedHashMap<>();
        routes.forEach((route, mapper) -> mapped.put(route, ValueMapper.section((section, ignoredRoute) -> {
            final Object currentValue = section.get(route, null);
            return mapper.map(handle.document(), route, currentValue);
        })));
        return mapped;
    }

    private static void runMigrations(final DefaultConfigHandle handle, final List<ConfigMigration> migrations) {
        for (final ConfigMigration migration : migrations) {
            try {
                migration.migrate(handle.document());
            } catch (final RuntimeException exception) {
                throw new ConfigException("configuration migration failed for " + handle.path(), exception);
            }
        }
    }

    private static void registerTypeAdapters(final ConfigTemplate template, final StandardSerializer serializer) {
        template.typeAdapters().forEach((type, adapter) -> register(serializer, type, adapter));
        template.typeAliases().forEach(serializer::register);
    }

    @SuppressWarnings("unchecked")
    private static <T> void register(
        final StandardSerializer serializer,
        final Class<?> type,
        final ConfigTypeAdapter<?> adapter
    ) {
        serializer.register((Class<T>) type, new TypeAdapter<>() {
            @Override
            public java.util.Map<Object, Object> serialize(final T object) {
                return ((ConfigTypeAdapter<T>) adapter).serialize(object);
            }

            @Override
            public T deserialize(final java.util.Map<Object, Object> map) {
                return ((ConfigTypeAdapter<T>) adapter).deserialize(map);
            }
        });
    }
}
