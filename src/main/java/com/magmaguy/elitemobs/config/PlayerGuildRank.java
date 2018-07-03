package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

public class PlayerGuildRank {

    public static final String CONFIG_NAME = "playerGuildRank.yml";
    public CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    public Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME, true);

    public void intializeConfig() {

        //no real defaults, just a data file
        customConfigLoader.getCustomConfig(CONFIG_NAME, true).options().copyDefaults(true);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME, true);
        customConfigLoader.saveCustomConfig(CONFIG_NAME, true);

    }

}
