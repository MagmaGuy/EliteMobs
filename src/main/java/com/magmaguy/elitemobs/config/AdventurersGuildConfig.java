package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

public class AdventurersGuildConfig {

    public static final String CONFIG_NAME = "AdventurersGuild.yml";

    public static final String ENABLE_ADVENTURERS_GUILD = "Enable adventurer's guild";
    public static final String ADD_MAX_HEALTH = "Add max health when unlocking higher guild ranks";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig(){

        configuration.addDefault(ENABLE_ADVENTURERS_GUILD, true);
        configuration.addDefault(ADD_MAX_HEALTH, true);

        configuration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
