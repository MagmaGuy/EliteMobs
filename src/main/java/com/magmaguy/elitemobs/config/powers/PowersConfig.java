package com.magmaguy.elitemobs.config.powers;

import com.magmaguy.elitemobs.config.CustomConfig;
import com.magmaguy.elitemobs.powers.meta.ElitePower;

import java.util.HashMap;

public class PowersConfig extends CustomConfig {

    private static HashMap<String, PowersConfigFields> powers = new HashMap<>();

    public PowersConfig() {
        super("powers", "com.magmaguy.elitemobs.config.powers.premade", PowersConfigFields.class);
        powers = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            powers.put(key, (PowersConfigFields) super.getCustomConfigFieldsHashMap().get(key));
        ElitePower.initializePowers();
    }

    public static HashMap<String, PowersConfigFields> getPowers() {
        return powers;
    }

    public static PowersConfigFields getPower(String filename) {
        return powers.get(filename);
    }

}
