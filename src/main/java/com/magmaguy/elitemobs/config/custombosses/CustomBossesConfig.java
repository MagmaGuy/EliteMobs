package com.magmaguy.elitemobs.config.custombosses;

import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.magmacore.config.CustomConfig;

import java.util.HashMap;
import java.util.List;

public class CustomBossesConfig extends CustomConfig {

    private static HashMap<String, CustomBossesConfigFields> customBosses = new HashMap<>();

    public CustomBossesConfig() {
        super("custombosses", "com.magmaguy.elitemobs.config.custombosses.premade", CustomBossesConfigFields.class);
        customBosses = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            if (super.getCustomConfigFieldsHashMap().get(key).isEnabled()) {
                CustomBossesConfigFields customBossesConfigFields = (CustomBossesConfigFields) super.getCustomConfigFieldsHashMap().get(key);
                customBosses.put(key, customBossesConfigFields);
            }
    }

    public static void initializeBosses() {
        for (CustomBossesConfigFields customBossesConfigFields : customBosses.values()) {
            if (customBossesConfigFields.isRegionalBoss()) {
                //Instanced regional bosses don't actually get initialized alongside normal Regional Bosses
                if (customBossesConfigFields.isInstanced()) {
                    //Initialize the regional bosses in the world
                    List<String> locations = customBossesConfigFields.processStringList("spawnLocations", customBossesConfigFields.getSpawnLocations(), customBossesConfigFields.getSpawnLocations(), false);
                    for (String string : locations)
                        InstancedBossEntity.add(string, customBossesConfigFields);
                    continue;
                }

                CustomBossesConfigFields.getRegionalElites().put(customBossesConfigFields.getFilename(), customBossesConfigFields);
                //Reinforcement elites are only temporary and situational, don't initialize them
                if (!customBossesConfigFields.isReinforcement()) {
                    //Initialize the regional bosses in the world
                    List<String> locations = customBossesConfigFields.processStringList("spawnLocations", customBossesConfigFields.getSpawnLocations(), customBossesConfigFields.getSpawnLocations(), false);
                    for (String string : locations)
                        new RegionalBossEntity(customBossesConfigFields, string).initialize();
                }
            }
        }
    }

    public static HashMap<String, ? extends CustomBossesConfigFields> getCustomBosses() {
        return customBosses;
    }

    public static CustomBossesConfigFields getCustomBoss(String fileName) {
        return customBosses.get(fileName);
    }

}
