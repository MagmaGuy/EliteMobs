package com.magmaguy.elitemobs.transport;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

/** Route definitions and crash-recovery journals have separate directories and lifecycles. */
final class TransportFiles {
    final Path routes;
    final Path journeys;
    private final Map<String, TransportRoute> loaded = new TreeMap<>();
    private com.magmaguy.elitemobs.config.transport.TransportRoutesConfig configuration;
    TransportFiles(Path data, Logger logger) throws IOException {
        routes = data.resolve("transport_routes"); journeys = data.resolve("transport_journeys");
        Files.createDirectories(routes); Files.createDirectories(journeys);
        reload();
    }
    void reload() throws IOException {
        Map<String, TransportRoute> next = new TreeMap<>();
        configuration = new com.magmaguy.elitemobs.config.transport.TransportRoutesConfig();
        for (var fields : configuration.getCustomConfigFieldsHashMap().values()) {
            if (!fields.isEnabled()) continue;
            TransportRoute route = ((com.magmaguy.elitemobs.config.transport.TransportRoutesConfigFields) fields).getRoute();
            if (route != null) next.put(route.id(), route);
        }
        loaded.clear(); loaded.putAll(next);
    }
    TransportRoute get(String id) { return loaded.get(id); }
    boolean hasDefinition(String id) { return configuration.getCustomConfigFieldsHashMap().containsKey(id + ".yml"); }
    List<String> ids() { return List.copyOf(loaded.keySet()); }
    void save(TransportRoute route) throws IOException {
        Path target = routes.resolve(route.id() + ".yml");
        // Updating a nested DLC definition must not create a duplicate root definition.
        try (var paths = Files.walk(routes)) {
            target = paths.filter(p -> p.getFileName().toString().equals(route.id() + ".yml"))
                    .findFirst().orElse(target);
        }
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.options().parseComments(true);
        try { if (Files.exists(target)) yaml.load(target.toFile()); }
        catch (org.bukkit.configuration.InvalidConfigurationException invalid) { throw new IOException("Invalid route YAML: " + target, invalid); }
        route.write(yaml, !Files.exists(target));
        if (yaml.getComments("transportEntity").isEmpty())
            yaml.setComments("transportEntity", List.of("Enabled file in custombosses/. Configure its appearance and powers in that file."));
        atomic(target, yaml);
        reload();
    }
    static void atomic(Path path, YamlConfiguration yaml) throws IOException {
        Path temp = Files.createTempFile(path.getParent(), ".transport-", ".tmp");
        try {
            try (var channel = java.nio.channels.FileChannel.open(temp, StandardOpenOption.WRITE)) {
                var bytes = java.nio.ByteBuffer.wrap(yaml.saveToString().getBytes(StandardCharsets.UTF_8));
                while (bytes.hasRemaining()) channel.write(bytes);
                channel.force(true);
            }
            try { Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException unsupported) { Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }
}
