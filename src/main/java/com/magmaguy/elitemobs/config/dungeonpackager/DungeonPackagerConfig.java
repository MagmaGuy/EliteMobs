package com.magmaguy.elitemobs.config.dungeonpackager;

import com.magmaguy.elitemobs.config.CustomConfig;
import com.magmaguy.elitemobs.dungeons.Minidungeon;

import java.util.HashMap;

public class DungeonPackagerConfig extends CustomConfig {

    public static HashMap<String, DungeonPackagerConfigFields> dungeonPackages = new HashMap<>();

    public DungeonPackagerConfig() {
        super("dungeonpackages", "com.magmaguy.elitemobs.config.dungeonpackager.premade", DungeonPackagerConfigFields.class);
        dungeonPackages = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            dungeonPackages.put(key, (DungeonPackagerConfigFields) super.getCustomConfigFieldsHashMap().get(key));
        dungeonPackages.values().forEach(Minidungeon::new);
    }

}
