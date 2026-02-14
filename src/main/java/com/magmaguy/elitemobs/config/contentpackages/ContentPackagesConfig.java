package com.magmaguy.elitemobs.config.contentpackages;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.magmacore.config.CustomConfig;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ContentPackagesConfig extends CustomConfig {

    @Getter
    private static Map<String, ContentPackagesConfigFields> dungeonPackages = new HashMap<>();
    @Getter
    private static Map<String, ContentPackagesConfigFields> enchantedChallengeDungeonPackages = new HashMap<>();


    public ContentPackagesConfig() {
        super("content_packages", "com.magmaguy.elitemobs.config.contentpackages.premade", ContentPackagesConfigFields.class);
        dungeonPackages = new HashMap<>();
        enchantedChallengeDungeonPackages = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet()) {
            if (!((ContentPackagesConfigFields) super.getCustomConfigFieldsHashMap().get(key)).isEnchantmentChallenge())
                dungeonPackages.put(key, (ContentPackagesConfigFields) super.getCustomConfigFieldsHashMap().get(key));
            else
                enchantedChallengeDungeonPackages.put(key, (ContentPackagesConfigFields) super.getCustomConfigFieldsHashMap().get(key));
        }

        //Initialize blueprints folder
        File worldsBluePrint = new File(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() + File.separatorChar + "world_blueprints");
        if (!worldsBluePrint.exists()) worldsBluePrint.mkdir();
    }

    public static void initializePackages() {
        for (ContentPackagesConfigFields fields : dungeonPackages.values())
            EMPackage.initialize(fields);
        for (ContentPackagesConfigFields fields : enchantedChallengeDungeonPackages.values())
            EMPackage.initialize(fields);
    }

}
