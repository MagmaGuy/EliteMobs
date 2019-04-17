package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;

import java.io.File;

public class PlayerMaxGuildRank {

    public static final String CONFIG_NAME = "playerMaxGuildRank.yml";
    public CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    public Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME, true);

    public void intializeConfig() {

        File file = new File(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getDataFolder().getAbsolutePath() + "/data/" + CONFIG_NAME);
        if (!file.exists())
            new CustomConfigConstructor(CONFIG_NAME, CONFIG_NAME);

    }

}
