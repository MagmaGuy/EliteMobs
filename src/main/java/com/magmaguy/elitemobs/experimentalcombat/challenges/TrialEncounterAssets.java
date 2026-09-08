package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields.PowerType;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.powers.lua.LuaPowerManager;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EquipmentSlot;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Bundled authored content, validated as one catalog through the existing Lua power loader. */
final class TrialEncounterAssets {
    record Encounter(String id, String title, String skin, double healthMultiplier,
                     Map<EquipmentSlot, Material> equipment, String opening, String halfway,
                     String victory, String defeat, LuaPowerManager.Registration registration) {
        Encounter { equipment = Map.copyOf(equipment); }
    }
    private static Map<String, Encounter> loaded = Map.of();
    private TrialEncounterAssets() {}

    static synchronized Encounter require(String id) {
        if (loaded.isEmpty()) loadAll();
        Encounter encounter = loaded.get(id);
        if (encounter == null) throw new IllegalArgumentException("No authored trial for " + id);
        return encounter;
    }

    private static void loadAll() {
        TrialPoses.validate();
        Map<String, Encounter> pending = new LinkedHashMap<>();
        List<LuaPowerManager.Registration> registrations = new ArrayList<>();
        try {
            String support = text("shared.lua");
            for (var form : BuiltInClassContent.catalog().forms()) {
                String id = form.id();
                String root = BuiltInClassContent.catalog().rootOf(id).id();
                String metadataPath = "encounters/" + id + ".yml";
                YamlConfiguration metadata;
                try (var reader = new InputStreamReader(stream(metadataPath), StandardCharsets.UTF_8)) {
                    metadata = YamlConfiguration.loadConfiguration(reader);
                }
                if (!id.equals(metadata.getString("id"))) throw new IllegalArgumentException("Wrong form ID in " + metadataPath);
                String skin = required(metadata, "skin");
                com.magmaguy.elitemobs.config.npcs.ClassTrainerConfig.disguise(skin);
                Map<EquipmentSlot, Material> equipment = new EnumMap<>(EquipmentSlot.class);
                var slots = Objects.requireNonNull(metadata.getConfigurationSection("equipment"), metadataPath + " equipment");
                for (String slot : slots.getKeys(false)) {
                    Material material = Material.getMaterial(slots.getString(slot, ""));
                    if (material == null || !material.isItem()) throw new IllegalArgumentException("Invalid equipment " + metadataPath + ": " + slot);
                    equipment.put(EquipmentSlot.valueOf(slot), material);
                }
                if (!equipment.containsKey(EquipmentSlot.HAND)) throw new IllegalArgumentException("Missing weapon in " + metadataPath);
                double health = metadata.getDouble("healthMultiplier", 10 + form.band().depth());
                if (!Double.isFinite(health) || health < 1 || health > 15) throw new IllegalArgumentException("Invalid health in " + metadataPath);
                String source = support + "\n" + text("mobility/" + root + ".lua") + "\n" + text("encounters/" + id + ".lua");
                var registration = LuaPowerManager.registerLuaPower("class_trial_" + id + ".lua",
                        new File("bundled/class_trials/encounters/" + id + ".lua"), source, null, PowerType.UNIQUE);
                registrations.add(registration);
                if (!registration.hookKeys().contains("on_game_tick")) throw new IllegalArgumentException("No authored timeline in " + id);
                pending.put(id, new Encounter(id, required(metadata, "title"), skin, health, equipment,
                        required(metadata, "voice.opening"), required(metadata, "voice.halfway"),
                        required(metadata, "voice.victory"), required(metadata, "voice.defeat"), registration));
            }
            loaded = Map.copyOf(pending);
            Logger.info("Validated " + loaded.size() + " authored class-trial encounters.");
        } catch (Exception failure) {
            registrations.forEach(LuaPowerManager.Registration::close);
            throw new IllegalStateException("Authored trial content unavailable: " + failure.getMessage(), failure);
        }
    }

    private static String required(YamlConfiguration config, String path) {
        String value = config.getString(path);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing trial field " + path);
        return value;
    }

    private static java.io.InputStream stream(String path) {
        return Objects.requireNonNull(TrialEncounterAssets.class.getResourceAsStream("/class_trials/" + path),
                "Missing authored asset class_trials/" + path);
    }

    private static String text(String path) throws java.io.IOException {
        try (var input = stream(path)) { return new String(input.readAllBytes(), StandardCharsets.UTF_8); }
    }
}
