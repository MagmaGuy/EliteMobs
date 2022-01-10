package com.magmaguy.elitemobs.config.dungeonpackager;

import com.magmaguy.elitemobs.config.CustomConfig;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class DungeonPackagerConfig extends CustomConfig {

    @Getter
    private static Map<String, DungeonPackagerConfigFields> dungeonPackages = new HashMap<>();

    public DungeonPackagerConfig() {
        super("dungeonpackages", "com.magmaguy.elitemobs.config.dungeonpackager.premade", DungeonPackagerConfigFields.class);
        dungeonPackages = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            dungeonPackages.put(key, (DungeonPackagerConfigFields) super.getCustomConfigFieldsHashMap().get(key));
        dungeonPackages.values().forEach(Minidungeon::new);
    }

}
