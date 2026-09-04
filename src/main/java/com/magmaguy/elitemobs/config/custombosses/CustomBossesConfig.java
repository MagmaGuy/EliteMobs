package com.magmaguy.elitemobs.config.custombosses;

import com.magmaguy.elitemobs.config.EliteMobsConfigInheritance;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.magmacore.config.CustomConfig;

import java.util.HashMap;
import java.util.List;
import java.io.File;

public class CustomBossesConfig extends CustomConfig {

    private static HashMap<String, CustomBossesConfigFields> customBosses = new HashMap<>();
    private static CustomBossesConfig instance;

    public CustomBossesConfig() {
        super("custombosses", "com.magmaguy.elitemobs.config.custombosses.premade",
                CustomBossesConfigFields.class, EliteMobsConfigInheritance.POLICY);
        instance = this;
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

    public static CustomBossesConfigFields registerRuntimeFile(File file) {
        if (instance == null) throw new IllegalStateException("Custom boss configuration is not initialized");
        CustomBossesConfigFields fields = (CustomBossesConfigFields) instance.registerFile(file);
        if (fields != null && fields.isEnabled()) customBosses.put(fields.getFilename(), fields);
        return fields;
    }

}
