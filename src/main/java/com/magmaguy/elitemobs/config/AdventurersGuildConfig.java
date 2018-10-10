package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

public class AdventurersGuildConfig {

    public static final String CONFIG_NAME = "AdventurersGuild.yml";

    public static final String ENABLE_ADVENTURERS_GUILD = "Enable adventurer's guild";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig(){

        configuration.addDefault(ENABLE_ADVENTURERS_GUILD, true);

        configuration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
