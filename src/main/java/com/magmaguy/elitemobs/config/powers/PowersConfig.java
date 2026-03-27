package com.magmaguy.elitemobs.config.powers;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfig;
import com.magmaguy.elitemobs.powers.lua.LuaPowerManager;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.magmacore.config.CustomConfig;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PowersConfig extends CustomConfig {

    private static Map<String, PowersConfigFields> powers = new HashMap<>();

    public PowersConfig() {
        super("powers", "com.magmaguy.elitemobs.config.powers.premade", PowersConfigFields.class);
        powers = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet()) {
            PowersConfigFields powersConfigFields = (PowersConfigFields) super.getCustomConfigFieldsHashMap().get(key);
            powers.put(key, powersConfigFields);
            powersConfigFields.initializeScripts();
        }

        Map<String, PowersConfigFields> premadeLuaPowers = LuaPowersConfig.getLuaPowers();
        powers.putAll(premadeLuaPowers);

        Map<String, PowersConfigFields> discoveredLuaPowers = LuaPowerManager.discoverLuaPowers(powers.values());
        powers.putAll(discoveredLuaPowers);
        powers.putAll(premadeLuaPowers);

        ElitePower.initializePowers();
        powers.values().forEach(ElitePower::registerConfiguredPower);
    }

    public static Map<String, PowersConfigFields> getPowers() {
        return powers;
    }

    public static PowersConfigFields getPower(String filename) {
        if (filename == null) {
            return null;
        }

        // Try Lua variant first for yml/yaml requests (Lua powers supersede legacy YAML powers)
        String lowerCaseFilename = filename.toLowerCase(Locale.ROOT);
        if (lowerCaseFilename.endsWith(".yml")) {
            PowersConfigFields luaMatch = powers.get(filename.substring(0, filename.length() - 4) + ".lua");
            if (luaMatch != null) return luaMatch;
        } else if (lowerCaseFilename.endsWith(".yaml")) {
            PowersConfigFields luaMatch = powers.get(filename.substring(0, filename.length() - 5) + ".lua");
            if (luaMatch != null) return luaMatch;
        }

        // Fall back to direct match (covers .lua requests and YAML-only powers)
        return powers.get(filename);
    }
}
