package com.magmaguy.elitemobs.config.powers;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfig;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.lua.LuaPowerManager;
import com.magmaguy.magmacore.config.CustomConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

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

        PowersConfigFields directMatch = powers.get(filename);
        if (directMatch != null) {
            return directMatch;
        }

        String lowerCaseFilename = filename.toLowerCase(Locale.ROOT);
        if (lowerCaseFilename.endsWith(".yml")) {
            return powers.get(filename.substring(0, filename.length() - 4) + ".lua");
        }
        if (lowerCaseFilename.endsWith(".yaml")) {
            return powers.get(filename.substring(0, filename.length() - 5) + ".lua");
        }
        return null;
    }
}
