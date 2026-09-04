package com.magmaguy.elitemobs.pathfinding.patrol;

import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Dedicated runtime persistence for patrol cursors. Route configuration files remain author-owned. */
final class PatrolStateStore {
    private static final int FORMAT_VERSION = 1;

    private final Path file;
    private final Map<String, StoredState> states = new HashMap<>();
    private boolean dirty;

    PatrolStateStore(File pluginDataFolder) {
        file = pluginDataFolder.toPath().resolve("patrol-state.yml");
        load();
    }

    Optional<StoredState> get(String canonicalIdentity) {
        return Optional.ofNullable(states.get(PatrolIdentity.storageKey(canonicalIdentity)));
    }

    void put(String canonicalIdentity, StoredState state) {
        String key = PatrolIdentity.storageKey(canonicalIdentity);
        StoredState normalized = state.withIdentity(canonicalIdentity);
        StoredState previous = states.put(key, normalized);
        if (!normalized.equals(previous)) dirty = true;
    }

    void remove(String canonicalIdentity) {
        if (states.remove(PatrolIdentity.storageKey(canonicalIdentity)) != null) dirty = true;
    }

    void saveIfDirty() {
        if (dirty) saveNow();
    }

    void saveNow() {
        try {
            Files.createDirectories(file.getParent());
            String serialized = serialize();
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temporary, serialized, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            dirty = false;
        } catch (IOException exception) {
            Logger.warn("Failed to save patrol state: " + exception.getMessage());
        }
    }

    private void load() {
        if (!Files.isRegularFile(file)) return;
        try {
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file.toFile());
            ConfigurationSection actors = configuration.getConfigurationSection("actors");
            if (actors == null) return;
            for (String key : actors.getKeys(false)) {
                ConfigurationSection section = actors.getConfigurationSection(key);
                if (section == null) continue;
                StoredState state = StoredState.read(section);
                if (state != null) states.put(key, state);
            }
        } catch (RuntimeException exception) {
            Logger.warn("Failed to load patrol-state.yml; configured patrols will start at their nearest node: "
                    + exception.getMessage());
        }
    }

    private String serialize() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("formatVersion", FORMAT_VERSION);
        for (Map.Entry<String, StoredState> entry : states.entrySet()) {
            String root = "actors." + entry.getKey();
            StoredState state = entry.getValue();
            configuration.set(root + ".identity", state.identity());
            configuration.set(root + ".currentNode", state.currentNode());
            configuration.set(root + ".targetNode", state.targetNode());
            configuration.set(root + ".direction", state.direction());
            configuration.set(root + ".fraction", state.fraction());
            configuration.set(root + ".virtualSpeed", state.virtualSpeed());
            if (state.safeLocation() != null) {
                configuration.set(root + ".safe.world", state.safeLocation().worldName());
                configuration.set(root + ".safe.x", state.safeLocation().x());
                configuration.set(root + ".safe.y", state.safeLocation().y());
                configuration.set(root + ".safe.z", state.safeLocation().z());
                configuration.set(root + ".safe.yaw", state.safeLocation().yaw());
                configuration.set(root + ".safe.pitch", state.safeLocation().pitch());
            }
        }
        return configuration.saveToString();
    }

    record StoredState(
            String identity,
            int currentNode,
            int targetNode,
            int direction,
            double fraction,
            double virtualSpeed,
            StoredLocation safeLocation) {

        StoredState withIdentity(String canonicalIdentity) {
            return new StoredState(canonicalIdentity, currentNode, targetNode, direction,
                    fraction, virtualSpeed, safeLocation);
        }

        static StoredState read(ConfigurationSection section) {
            String identity = section.getString("identity");
            if (identity == null || identity.isBlank()) return null;
            double fraction = section.getDouble("fraction", 0D);
            if (!Double.isFinite(fraction)) fraction = 0D;
            double virtualSpeed = section.getDouble("virtualSpeed", 0D);
            if (!Double.isFinite(virtualSpeed) || virtualSpeed < 0D) virtualSpeed = 0D;
            ConfigurationSection safe = section.getConfigurationSection("safe");
            StoredLocation safeLocation = safe == null ? null : StoredLocation.read(safe);
            return new StoredState(
                    identity,
                    section.getInt("currentNode", 0),
                    section.getInt("targetNode", 0),
                    section.getInt("direction", 1) < 0 ? -1 : 1,
                    Math.max(0D, Math.min(1D, fraction)),
                    virtualSpeed,
                    safeLocation);
        }
    }

    record StoredLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        static StoredLocation from(Location location, String fallbackWorldName) {
            if (location == null) return null;
            String worldName = location.getWorld() == null ? fallbackWorldName : location.getWorld().getName();
            if (worldName == null || worldName.isBlank()) return null;
            return new StoredLocation(worldName, location.getX(), location.getY(),
                    location.getZ(), location.getYaw(), location.getPitch());
        }

        static StoredLocation read(ConfigurationSection section) {
            String worldName = section.getString("world");
            if (worldName == null || worldName.isBlank()) return null;
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) return null;
            return new StoredLocation(worldName, x, y, z,
                    (float) section.getDouble("yaw"), (float) section.getDouble("pitch"));
        }

        Location resolve() {
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
            return world == null ? null : new Location(world, x, y, z, yaw, pitch);
        }
    }
}
