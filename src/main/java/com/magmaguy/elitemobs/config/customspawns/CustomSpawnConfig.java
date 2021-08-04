package com.magmaguy.elitemobs.config.customspawns;

import com.magmaguy.elitemobs.config.CustomConfig;

import java.util.HashMap;

public class CustomSpawnConfig extends CustomConfig {

    public static CustomSpawnConfig customSpawnConfig;
    private final HashMap<String, CustomSpawnConfigFields> customSpawns = new HashMap<>();

    public CustomSpawnConfig() {
        super("customspawns", "com.magmaguy.elitemobs.config.customspawns.premade", CustomSpawnConfigFields.class);
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            customSpawns.put(key, (CustomSpawnConfigFields) super.getCustomConfigFieldsHashMap().get(key));
        customSpawnConfig = this;
    }

    public HashMap<String, ? extends CustomSpawnConfigFields> getCustomSpawns() {
        return customSpawns;
    }

    public CustomSpawnConfigFields getCustomEvent(String fileName) {
        return customSpawns.get(fileName);
    }

}
