package com.magmaguy.elitemobs.config.customspawns;

import com.magmaguy.elitemobs.config.CustomConfig;

import java.util.HashMap;

public class CustomSpawnConfig extends CustomConfig {

    public HashMap<String, ? extends CustomSpawnConfigFields> getCustomSpawns() {
        return customSpawns;
    }

    public CustomSpawnConfigFields getCustomEvent(String fileName) {
        return customSpawns.get(fileName);
    }

    private HashMap<String, CustomSpawnConfigFields> customSpawns = new HashMap<>();

    public static CustomSpawnConfig customSpawnConfig;

    public CustomSpawnConfig() {
        super("customspawns", "com.magmaguy.elitemobs.config.customspawns.premade", CustomSpawnConfigFields.class);
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            customSpawns.put(key, (CustomSpawnConfigFields) super.getCustomConfigFieldsHashMap().get(key));
        customSpawnConfig = this;
    }

}
