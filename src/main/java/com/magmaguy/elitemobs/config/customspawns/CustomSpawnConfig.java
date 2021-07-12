package com.magmaguy.elitemobs.config.customspawns;

import com.magmaguy.elitemobs.config.CustomConfig;

public class CustomSpawnConfig extends CustomConfig {

    public static CustomSpawnConfig customSpawnConfig;

    public static CustomSpawnConfigFields getCustomSpawnConfigField(String name) {
        return (CustomSpawnConfigFields) customSpawnConfig.getCustomConfigFieldsHashMap().get(name);
    }

    public CustomSpawnConfig() {
        super("customspawns", "com.magmaguy.elitemobs.config.customspawns.premade");
        customSpawnConfig = this;
    }

}
