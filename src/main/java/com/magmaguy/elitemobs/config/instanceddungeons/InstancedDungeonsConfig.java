package com.magmaguy.elitemobs.config.instanceddungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CustomConfig;

import java.io.File;
import java.util.HashMap;

public class InstancedDungeonsConfig extends CustomConfig {

    private static HashMap<String, InstancedDungeonsConfigFields> instancedDungeons;

    public InstancedDungeonsConfig() {
        super("instanced_dungeons", "com.magmaguy.elitemobs.config.instanceddungeons.premade", InstancedDungeonsConfigFields.class);
        instancedDungeons = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            if (super.getCustomConfigFieldsHashMap().get(key).isEnabled())
                instancedDungeons.put(key, (InstancedDungeonsConfigFields) super.getCustomConfigFieldsHashMap().get(key));
        File worldsBluePrint = new File(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() + File.separatorChar + "world_blueprints");
        if (!worldsBluePrint.exists()) worldsBluePrint.mkdir();
    }

    public static InstancedDungeonsConfigFields getInstancedDungeonConfigFields(String filename) {
        return instancedDungeons.get(filename);
    }

}
