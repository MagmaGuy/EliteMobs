package com.magmaguy.elitemobs.config.custombosses;

import com.magmaguy.elitemobs.config.CustomConfig;

import java.util.HashMap;

public class CustomBossesConfig extends CustomConfig {

    public static HashMap<String, ? extends CustomBossesConfigFields> getCustomBosses() {
        return customBosses;
    }

    public static CustomBossesConfigFields getCustomBoss(String fileName) {
        return customBosses.get(fileName);
    }

    private static HashMap<String, CustomBossesConfigFields> customBosses = new HashMap<>();

    public CustomBossesConfig() {
        super("custombosses", "com.magmaguy.elitemobs.config.custombosses.premade", "CustomBossesConfigFields");
        customBosses = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            customBosses.put(key, (CustomBossesConfigFields) super.getCustomConfigFieldsHashMap().get(key));
    }

}
