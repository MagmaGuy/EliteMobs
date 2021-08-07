package com.magmaguy.elitemobs.config.powers;

import com.magmaguy.elitemobs.config.CustomConfig;

import java.util.HashMap;

public class PowersConfig extends CustomConfig {

    private static HashMap<String, PowersConfigFields> customItems = new HashMap<>();

    public PowersConfig() {
        super("powers", "com.magmaguy.elitemobs.config.powers.premade", PowersConfigFields.class);
        customItems = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            customItems.put(key, (PowersConfigFields) super.getCustomConfigFieldsHashMap().get(key));
    }

    public static HashMap<String, PowersConfigFields> getPowers() {
        return customItems;
    }

    public static PowersConfigFields getPower(String filename) {
        return customItems.get(filename);
    }

}
