package com.stephanofer.networkplatform.paper.config.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.stephanofer.networkplatform.paper.config.ConfigHandle;
import com.stephanofer.networkplatform.paper.config.ConfigTemplate;
import com.stephanofer.networkplatform.paper.config.TypedConfigHandle;
import com.stephanofer.networkplatform.paper.lifecycle.PlatformLifecycle;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultConfigServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldLoadNestedFilesAndPreserveUserValuesDuringUpdate() throws Exception {
        final Path file = this.tempDir.resolve("modules/player/config.yml");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "file-version: 1\nqueue:\n  max-players: 30\n", StandardCharsets.UTF_8);

        final DefaultConfigService service = service(Map.of(
            "modules/player/config.yml",
            "file-version: 2\nqueue:\n  max-players: 20\n  mode: ranked\n  enabled: true\n"
        ));

        final ConfigHandle handle = service.file("modules/player/config.yml");

        assertEquals(30, handle.getInt("queue.max-players"));
        assertEquals("ranked", handle.getString("queue.mode"));
        assertTrue(handle.getBoolean("queue.enabled"));
        assertTrue(Files.readString(file).contains("file-version: 2"));
        service.shutdown();
    }

    @Test
    void shouldBackupBrokenFilesAndRecoverFromDefaults() throws Exception {
        final Path file = this.tempDir.resolve("config.yml");
        Files.writeString(file, "key: [broken\n", StandardCharsets.UTF_8);

        final DefaultConfigService service = service(Map.of(
            "config.yml",
            "file-version: 1\nkey: fixed\nenabled: true\n"
        ));

        final ConfigHandle handle = service.file("config.yml");

        assertEquals("fixed", handle.getString("key"));
        assertTrue(handle.getBoolean("enabled"));
        try (Stream<Path> files = Files.list(this.tempDir)) {
            assertTrue(files.anyMatch(path -> path.getFileName().toString().contains("broken-")));
        }
        service.shutdown();
    }

    @Test
    void shouldSupportTypedSnapshotsAndRefreshWhenDocumentChanges() {
        final ConfigTemplate template = ConfigTemplate.builder("database.yml")
            .typedSection("database", DatabaseSettings.class, document -> new DatabaseSettings(
                document.getString("host"),
                document.getInt("port")
            ))
            .build();

        final DefaultConfigService service = service(Map.of(
            "database.yml",
            "file-version: 1\ndatabase:\n  host: localhost\n  port: 3306\n"
        ));

        final TypedConfigHandle<DatabaseSettings> handle = service.file(template, DatabaseSettings.class);
        assertEquals(new DatabaseSettings("localhost", 3306), handle.snapshot());

        handle.document().set("database.host", "redis.internal");
        assertEquals(new DatabaseSettings("redis.internal", 3306), handle.snapshot());
        service.shutdown();
    }

    @Test
    void shouldSupportFilesWithoutDefaultsAndAsyncFlush() throws Exception {
        final ConfigTemplate template = ConfigTemplate.builder("runtime/cache.yml")
            .withoutDefaults()
            .createIfAbsent(false)
            .build();

        final DefaultConfigService service = service(Map.of());
        final ConfigHandle handle = service.file(template);

        assertFalse(handle.exists());
        handle.document().set("runtime.enabled", true);
        handle.flushAsync().join();

        assertTrue(handle.exists());
        assertTrue(handle.getBoolean("runtime.enabled"));
        service.shutdown();
    }

    @Test
    void shouldPersistCustomSerializedTypes() {
        final ConfigTemplate template = ConfigTemplate.builder("typed.yml")
            .withoutDefaults()
            .registerType(Position.class, new com.stephanofer.networkplatform.paper.config.ConfigTypeAdapter<>() {
                @Override
                public Map<Object, Object> serialize(final Position object) {
                    final Map<Object, Object> serialized = new LinkedHashMap<>();
                    serialized.put("x", object.x());
                    serialized.put("z", object.z());
                    return serialized;
                }

                @Override
                public Position deserialize(final Map<Object, Object> serialized) {
                    return new Position((int) serialized.get("x"), (int) serialized.get("z"));
                }
            })
            .build();

        DefaultConfigService service = service(Map.of());
        final ConfigHandle writer = service.file(template);
        writer.document().set("spawn", new Position(10, 22));
        writer.flush();
        service.shutdown();

        service = service(Map.of());
        final ConfigHandle reader = service.file(template);
        assertEquals(new Position(10, 22), reader.get("spawn"));
        service.shutdown();
    }

    private DefaultConfigService service(final Map<String, String> resources) {
        return new DefaultConfigService(
            this.tempDir,
            resourcePath -> open(resources, resourcePath),
            Logger.getLogger("config-test"),
            new PlatformLifecycle(Logger.getLogger("config-test-lifecycle"))
        );
    }

    private static InputStream open(final Map<String, String> resources, final String resourcePath) {
        final String contents = resources.get(resourcePath);
        return contents == null ? null : new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
    }

    private record DatabaseSettings(String host, int port) {
    }

    private record Position(int x, int z) {
    }
}
