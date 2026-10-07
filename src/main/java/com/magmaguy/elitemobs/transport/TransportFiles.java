package com.magmaguy.elitemobs.transport;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

/** Loads and saves authored route definitions. */
final class TransportFiles {
    final Path routes;
    final Path networks;
    private final Map<String, TransportRoute> loaded = new TreeMap<>();
    private final Map<String, TransportNetwork> loadedNetworks = new TreeMap<>();
    private com.magmaguy.elitemobs.config.transport.TransportRoutesConfig configuration;
    private com.magmaguy.elitemobs.config.transport.TransportNetworksConfig networkConfiguration;
    TransportFiles(Path data, Logger logger) throws IOException {
        routes = data.resolve("transport_routes");
        Files.createDirectories(routes);
        networks = data.resolve("transport_networks");
        Files.createDirectories(networks);
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
        Map<String, TransportNetwork> nextNetworks = new TreeMap<>();
        networkConfiguration = new com.magmaguy.elitemobs.config.transport.TransportNetworksConfig();
        for (var fields : networkConfiguration.getCustomConfigFieldsHashMap().values()) {
            if (!fields.isEnabled()) continue;
            TransportNetwork network = ((com.magmaguy.elitemobs.config.transport.TransportNetworksConfigFields) fields).getNetwork();
            if (network != null) nextNetworks.put(network.id(), network);
        }
        loadedNetworks.clear(); loadedNetworks.putAll(nextNetworks);
    }
    TransportRoute get(String id) { return loaded.get(id); }
    boolean hasDefinition(String id) { return configuration.getCustomConfigFieldsHashMap().containsKey(id + ".yml"); }
    List<String> ids() { return List.copyOf(loaded.keySet()); }
    TransportNetwork network(String id) { return loadedNetworks.get(id); }
    List<String> networkIds() { return List.copyOf(loadedNetworks.keySet()); }
    void save(TransportRoute route) throws IOException {
        var fields = configuration.getCustomConfigFieldsHashMap().get(route.id() + ".yml");
        // The configuration loader owns duplicate selection and nested DLC file identity.
        Path target = fields == null ? routes.resolve(route.id() + ".yml") : fields.getFile().toPath();
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.options().parseComments(true);
        try { if (Files.exists(target)) yaml.load(target.toFile()); }
        catch (org.bukkit.configuration.InvalidConfigurationException invalid) { throw new IOException("Invalid route YAML: " + target, invalid); }
        route.write(yaml, !Files.exists(target));
        if (yaml.getComments("transportEntity").isEmpty())
            yaml.setComments("transportEntity", List.of("Enabled file in custombosses/. Configure its appearance and powers in that file."));
        atomic(target, yaml);
        reload();
        TransportRoute published = get(route.id());
        if (published == null || !published.points().equals(route.points())
                || Float.compare(published.arrivalYaw(), route.arrivalYaw()) != 0)
            throw new IOException("Route file was written but could not be loaded as saved. Your draft is retained: " + target);
    }
    void saveNetwork(TransportNetwork network) throws IOException {
        var fields = networkConfiguration.getCustomConfigFieldsHashMap().get(network.id() + ".yml");
        Path target = fields == null ? networks.resolve(network.id() + ".yml") : fields.getFile().toPath();
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.options().parseComments(true);
        try { if (Files.exists(target)) yaml.load(target.toFile()); }
        catch (org.bukkit.configuration.InvalidConfigurationException invalid) { throw new IOException("Invalid network YAML: " + target, invalid); }
        network.write(yaml, !Files.exists(target));
        atomic(target, yaml);
        reload();
        if (network(network.id()) == null)
            throw new IOException("Network file was written but did not load. Your draft is retained: " + target);
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
