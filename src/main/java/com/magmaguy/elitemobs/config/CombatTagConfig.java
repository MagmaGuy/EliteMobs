package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

public class CombatTagConfig {

    public static final String CONFIG_NAME = "CombatTag.yml";

    public static final String ENABLE_COMBAT_TAG = "Enable combat tag";
    public static final String COMBAT_TAG_MESSAGE = "Combat tag message";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    private Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig(){

        configuration.addDefault(ENABLE_COMBAT_TAG, true);
        configuration.addDefault(COMBAT_TAG_MESSAGE, "&c[EliteMobs] Combat tag activated!");

        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);


    }

}
