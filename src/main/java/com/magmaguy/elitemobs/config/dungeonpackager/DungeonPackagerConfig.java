package com.magmaguy.elitemobs.config.dungeonpackager;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.magmacore.config.CustomConfig;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DungeonPackagerConfig extends CustomConfig {

    @Getter
    private static Map<String, DungeonPackagerConfigFields> dungeonPackages = new HashMap<>();
    @Getter
    private static Map<String, DungeonPackagerConfigFields> enchantedChallengeDungeonPackages = new HashMap<>();


    public DungeonPackagerConfig() {
        super("dungeonpackages", "com.magmaguy.elitemobs.config.dungeonpackager.premade", DungeonPackagerConfigFields.class);
        dungeonPackages = new HashMap<>();
        enchantedChallengeDungeonPackages = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet()) {
            if (!((DungeonPackagerConfigFields) super.getCustomConfigFieldsHashMap().get(key)).isEnchantmentChallenge())
                dungeonPackages.put(key, (DungeonPackagerConfigFields) super.getCustomConfigFieldsHashMap().get(key));
            else
                enchantedChallengeDungeonPackages.put(key, (DungeonPackagerConfigFields) super.getCustomConfigFieldsHashMap().get(key));
            EMPackage.initialize((DungeonPackagerConfigFields) super.getCustomConfigFieldsHashMap().get(key));
        }

        //Initialize blueprints folder
        File worldsBluePrint = new File(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() + File.separatorChar + "world_blueprints");
        if (!worldsBluePrint.exists()) worldsBluePrint.mkdir();
    }

}
