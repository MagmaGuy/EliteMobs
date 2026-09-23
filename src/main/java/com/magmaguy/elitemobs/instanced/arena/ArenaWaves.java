package com.magmaguy.elitemobs.instanced.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Parsed once before registering an arena run or its scheduled work. */
public class ArenaWaves {
    private final Map<Integer, List<ArenaEntity>> arenaEntities;

    public ArenaWaves(List<String> rawBosses) {
        Map<Integer, List<ArenaEntity>> waves = new HashMap<>();
        if (rawBosses == null) throw new IllegalArgumentException("Arena bossList is missing");
        for (String entry : rawBosses) {
            if (entry == null) throw new IllegalArgumentException("Arena bossList contains a null entry");
            Map<String, String> fields = new HashMap<>();
            for (String field : entry.split(":", -1)) {
                String[] pair = field.split("=", 2);
                if (pair.length != 2 || pair[1].isBlank()) throw new IllegalArgumentException("Invalid arena wave: " + entry);
                String key = pair[0].trim().toLowerCase(Locale.ROOT);
                if (!List.of("wave", "spawnpoint", "boss", "mythicmob", "level").contains(key)
                        || fields.putIfAbsent(key, pair[1].trim()) != null)
                    throw new IllegalArgumentException("Unknown or duplicate arena wave field: " + entry);
            }
            int wave = integer(fields.get("wave"), entry);
            int level = fields.containsKey("level") ? integer(fields.get("level"), entry) : -1;
            String spawn = fields.get("spawnpoint"), boss = fields.get("boss");
            String mythic = fields.getOrDefault("mythicmob", "false");
            if (wave < 1 || (level != -1 && level < 1) || spawn == null || boss == null
                    || !(mythic.equalsIgnoreCase("true") || mythic.equalsIgnoreCase("false")))
                throw new IllegalArgumentException("Invalid arena wave: " + entry);
            ArenaEntity entity = new ArenaEntity(spawn, wave, boss);
            entity.setMythicMob(Boolean.parseBoolean(mythic));
            entity.setLevel(level);
            waves.computeIfAbsent(wave, ignored -> new ArrayList<>()).add(entity);
        }
        waves.replaceAll((wave, entries) -> List.copyOf(entries));
        arenaEntities = Map.copyOf(waves);
    }

    private static int integer(String value, String entry) {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException failure) { throw new IllegalArgumentException("Invalid arena wave number: " + entry, failure); }
    }

    void validate(int waveCount, ArenaContainer container) {
        if (waveCount < 1 || arenaEntities.size() != waveCount)
            throw new IllegalArgumentException("Arena requires at least one boss in every configured wave");
        for (var wave : arenaEntities.entrySet()) {
            if (wave.getKey() > waveCount) throw new IllegalArgumentException("Arena wave exceeds waveCount: " + wave.getKey());
            for (ArenaEntity entity : wave.getValue())
                if (container.spawnPoint(entity.getSpawnPointName()) == null)
                    throw new IllegalArgumentException("Unknown arena spawn point: " + entity.getSpawnPointName());
        }
    }

    public List<ArenaEntity> getWaveEntities(int wave) { return arenaEntities.get(wave); }
}
