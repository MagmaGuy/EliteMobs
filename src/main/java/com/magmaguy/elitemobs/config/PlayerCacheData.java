package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;

import java.io.File;

/**
 * Created by MagmaGuy on 02/07/2017.
 */
public class PlayerCacheData {

    public static final String CONFIG_NAME = "playerCache.yml";
    public CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    public Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME, true);

    public void initializeConfig() {

        File file = new File(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getDataFolder().getAbsolutePath() + "/data/" + CONFIG_NAME);
        if (!file.exists())
            new CustomConfigConstructor(CONFIG_NAME, CONFIG_NAME);
    }

}
