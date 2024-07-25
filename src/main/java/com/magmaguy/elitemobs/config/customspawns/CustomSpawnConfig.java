package com.magmaguy.elitemobs.config.customspawns;

import com.magmaguy.magmacore.config.CustomConfig;

import java.util.HashMap;

public class CustomSpawnConfig extends CustomConfig {

    private static HashMap<String, CustomSpawnConfigFields> customSpawns = new HashMap<>();

    public CustomSpawnConfig() {
        super("customspawns", "com.magmaguy.elitemobs.config.customspawns.premade", CustomSpawnConfigFields.class);
        customSpawns = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            if (super.getCustomConfigFieldsHashMap().get(key).isEnabled())
                customSpawns.put(key, (CustomSpawnConfigFields) super.getCustomConfigFieldsHashMap().get(key));
    }

    public static HashMap<String, ? extends CustomSpawnConfigFields> getCustomSpawns() {
        return customSpawns;
    }

    public static CustomSpawnConfigFields getCustomEvent(String fileName) {
        return customSpawns.get(fileName);
    }

}
