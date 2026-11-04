package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Saved regional spawn points whose world is not loaded. Each point stays as its raw configuration string
 * ({@code world,x,y,z,pitch,yaw[:unixRespawnTime]}) until its world loads, instead of holding a full boss with
 * parsed powers, scripts and transitive blocks for a world that may never load on this server.
 * <p>
 * These points are still part of their boss file: anything that rewrites {@code spawnLocations} must write them
 * back next to the live holders, or the first save after startup deletes them.
 */
final class PendingRegionalSpawns {
    // Lower-case world name, as Bukkit matches world names, then boss file, then raw strings in configuration order.
    private final Map<String, Map<CustomBossesConfigFields, List<String>>> byWorld = new HashMap<>();

    /** The world part of a raw spawn string, parsed the same way as the regional boss constructor. */
    static String worldName(String rawString) {
        return rawString.split(":")[0].split(",")[0];
    }

    private static String key(String worldName) {
        return worldName.toLowerCase(Locale.ROOT);
    }

    synchronized void add(CustomBossesConfigFields fields, String rawString) {
        byWorld.computeIfAbsent(key(worldName(rawString)), ignored -> new LinkedHashMap<>())
                .computeIfAbsent(fields, ignored -> new ArrayList<>())
                .add(rawString);
    }

    /** Removes and returns every point waiting for this world, so a second drain of the same world finds nothing. */
    synchronized Map<CustomBossesConfigFields, List<String>> drain(String worldName) {
        Map<CustomBossesConfigFields, List<String>> drained = byWorld.remove(key(worldName));
        return drained == null ? Map.of() : drained;
    }

    synchronized List<String> of(CustomBossesConfigFields fields) {
        List<String> rawStrings = new ArrayList<>();
        for (Map<CustomBossesConfigFields, List<String>> world : byWorld.values()) {
            List<String> waiting = world.get(fields);
            if (waiting != null) rawStrings.addAll(waiting);
        }
        return rawStrings;
    }

    synchronized Set<CustomBossesConfigFields> owners() {
        Set<CustomBossesConfigFields> owners = new LinkedHashSet<>();
        for (Map<CustomBossesConfigFields, List<String>> world : byWorld.values()) owners.addAll(world.keySet());
        return owners;
    }

    synchronized int size() {
        int size = 0;
        for (Map<CustomBossesConfigFields, List<String>> world : byWorld.values())
            for (List<String> waiting : world.values()) size += waiting.size();
        return size;
    }

    /**
     * Drops the saved respawn time from every waiting point, so each boss spawns as soon as its world loads.
     *
     * @return the boss files whose points changed
     */
    synchronized Set<CustomBossesConfigFields> clearRespawnTimers() {
        Set<CustomBossesConfigFields> changed = new LinkedHashSet<>();
        for (Map<CustomBossesConfigFields, List<String>> world : byWorld.values())
            for (Map.Entry<CustomBossesConfigFields, List<String>> entry : world.entrySet())
                entry.getValue().replaceAll(rawString -> {
                    int timer = rawString.indexOf(':');
                    if (timer < 0) return rawString;
                    changed.add(entry.getKey());
                    return rawString.substring(0, timer);
                });
        return changed;
    }

    synchronized void clear() {
        byWorld.clear();
    }
}
