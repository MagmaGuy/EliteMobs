package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;

import java.io.File;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class ItemsCustomLootListConfig {

    public static final String CONFIG_NAME = "ItemsCustomLootList.yml";

    public void intializeConfig() {

        File file = new File(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getDataFolder().getAbsolutePath() + "/data/" + CONFIG_NAME);
        if (!file.exists())
            new CustomConfigConstructor(CONFIG_NAME, CONFIG_NAME);

    }

}
