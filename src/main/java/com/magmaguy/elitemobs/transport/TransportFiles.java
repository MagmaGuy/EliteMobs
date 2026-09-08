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
    private final Logger logger;
    TransportFiles(Path data, Logger logger) throws IOException {
        routes = data.resolve("transport_routes"); journeys = data.resolve("transport_journeys");
        this.logger = logger;
        Files.createDirectories(routes); Files.createDirectories(journeys);
        reload();
    }
    void reload() throws IOException {
        Map<String, TransportRoute> next = new TreeMap<>();
        try (var paths = Files.walk(routes)) {
            for (Path file : paths.filter(p -> p.toString().endsWith(".yml")).toList()) {
                String id = file.getFileName().toString().replaceFirst("\\.yml$", "");
                try {
                    YamlConfiguration yaml = new YamlConfiguration(); yaml.load(file.toFile());
                    TransportRoute route = TransportRoute.read(id, yaml);
                    if (next.putIfAbsent(id, route) != null) throw new IOException("Duplicate transport route: " + id);
                } catch (IOException duplicate) { throw duplicate; }
                catch (Exception invalid) { logger.warning("Transport route " + file + ": " + invalid.getMessage()); }
            }
        }
        loaded.clear(); loaded.putAll(next);
    }
    TransportRoute get(String id) { return loaded.get(id); }
    List<String> ids() { return List.copyOf(loaded.keySet()); }
    void save(TransportRoute route) throws IOException {
        Path target = routes.resolve(route.id() + ".yml");
        // Updating a nested DLC definition must not create a duplicate root definition.
        try (var paths = Files.walk(routes)) {
            target = paths.filter(p -> p.getFileName().toString().equals(route.id() + ".yml"))
                    .findFirst().orElse(target);
        }
        atomic(target, route.write()); loaded.put(route.id(), route);
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
