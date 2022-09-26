package com.magmaguy.elitemobs.config.powers;

import com.magmaguy.elitemobs.config.CustomConfig;
import com.magmaguy.elitemobs.powers.meta.ElitePower;

import java.util.HashMap;
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

        ElitePower.initializePowers();
    }

    public static Map<String, PowersConfigFields> getPowers() {
        return powers;
    }

    public static PowersConfigFields getPower(String filename) {
        return powers.get(filename);
    }

}
