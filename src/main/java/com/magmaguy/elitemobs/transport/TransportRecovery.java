package com.magmaguy.elitemobs.transport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/** Written and forced to disk before boarding; cleared only after a successful safe teleport. */
final class TransportRecovery {
    private final Path directory;
    TransportRecovery(Path directory) { this.directory = directory; }
    void write(UUID player, Location destination, Location fallback, UUID matchId) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        location(yaml, "destination", destination); location(yaml, "fallback", fallback);
        yaml.set("instanced", matchId != null);
        yaml.set("matchId", matchId == null ? null : matchId.toString());
        TransportFiles.atomic(path(player), yaml);
    }
    Record read(UUID player) throws Exception {
        if (!Files.exists(path(player))) return null;
        YamlConfiguration yaml = new YamlConfiguration(); yaml.load(path(player).toFile());
        String matchId = yaml.getString("matchId");
        return new Record(location(yaml, "destination"), location(yaml, "fallback"), yaml.getBoolean("instanced"),
                matchId == null ? null : UUID.fromString(matchId));
    }
    void clear(UUID player) throws IOException { Files.deleteIfExists(path(player)); }
    private Path path(UUID player) { return directory.resolve(player + ".yml"); }
    private static void location(YamlConfiguration yaml, String key, Location location) {
        if (location == null || location.getWorld() == null) throw new IllegalArgumentException("Missing recovery world");
        yaml.set(key + ".world", location.getWorld().getUID().toString());
        yaml.set(key + ".x", location.getX()); yaml.set(key + ".y", location.getY());
        yaml.set(key + ".z", location.getZ()); yaml.set(key + ".yaw", location.getYaw());
        yaml.set(key + ".pitch", location.getPitch());
    }
    private static Location location(YamlConfiguration yaml, String key) {
        World world = Bukkit.getWorld(UUID.fromString(yaml.getString(key + ".world")));
        return world == null ? null : new Location(world, yaml.getDouble(key + ".x"), yaml.getDouble(key + ".y"),
                yaml.getDouble(key + ".z"), (float) yaml.getDouble(key + ".yaw"), (float) yaml.getDouble(key + ".pitch"));
    }
    record Record(Location destination, Location fallback, boolean instanced, UUID matchId) {}
}
