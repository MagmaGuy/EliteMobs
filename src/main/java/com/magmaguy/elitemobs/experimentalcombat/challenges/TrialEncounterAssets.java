package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.powers.LuaPowerConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/** Indexes class admission metadata on ordinary, disk-loaded custom bosses. No bundled fallback. */
public final class TrialEncounterAssets {
    record Encounter(CustomBossesConfigFields boss, LuaPowerConfigFields power,
                     String opening, String halfway, String victory, String defeat) {}
    private static Map<String, Encounter> loaded = Map.of();
    private TrialEncounterAssets() {}

    public static synchronized void initialize() {
        Map<String, Encounter> pending = new LinkedHashMap<>();
        Set<String> claimed = new HashSet<>();
        for (var boss : CustomBossesConfig.getCustomBosses().values()) {
            ConfigurationSection config = boss.getFileConfiguration().getConfigurationSection("classTrial");
            if (config == null) continue;
            try {
                String id = required(config, "class");
                BuiltInClassContent.catalog().require(id);
                if (!claimed.add(id)) {
                    pending.remove(id);
                    throw new IllegalArgumentException("Multiple enabled bosses claim class " + id);
                }
                if (!(PowersConfig.getPower(required(config, "power")) instanceof LuaPowerConfigFields power))
                    throw new IllegalArgumentException("Missing Lua power " + config.getString("power"));
                if (power.getLuaPowerDefinition().getHooks().stream().noneMatch(hook -> hook.getKey().equals("on_game_tick")))
                    throw new IllegalArgumentException("Trial power needs an on_game_tick hook");
                var actors = config.getConfigurationSection("actors");
                if (actors != null) for (String key : actors.getKeys(false))
                    if (CustomBossesConfig.getCustomBoss(actors.getString(key)) == null)
                        throw new IllegalArgumentException("Missing trial actor " + actors.getString(key));
                pending.put(id, new Encounter(boss, power,
                        required(config, "voice.opening"), required(config, "voice.halfway"),
                        required(config, "voice.victory"), required(config, "voice.defeat")));
            } catch (RuntimeException failure) {
                Logger.warn("Invalid class trial in " + boss.getFile() + ": " + failure.getMessage());
            }
        }
        loaded = Map.copyOf(pending);
        Logger.info("Loaded " + loaded.size() + " class trials from custom boss YAML files.");
        List<String> missing = BuiltInClassContent.catalog().forms().stream().map(form -> form.id())
                .filter(id -> !loaded.containsKey(id)).sorted().toList();
        if (!missing.isEmpty()) Logger.warn("Missing class-trial content for " + String.join(", ", missing)
                + ". Install or check the Adventurer's Guild custombosses and powers. These trials are unavailable; no entry fee will be charged.");
    }

    public static synchronized void shutdown() { loaded = Map.of(); }

    static synchronized Encounter require(String id) {
        Encounter encounter = loaded.get(id);
        if (encounter == null) throw new IllegalArgumentException("No enabled custom boss YAML trial for " + id);
        return encounter;
    }

    private static String required(ConfigurationSection config, String path) {
        String value = config.getString(path);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing classTrial." + path);
        return value;
    }
}
