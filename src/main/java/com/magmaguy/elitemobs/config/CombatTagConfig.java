package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

public class CombatTagConfig {

    public static final String CONFIG_NAME = "CombatTag.yml";

    public static final String ENABLE_COMBAT_TAG = "Enable combat tag";
    public static final String COMBAT_TAG_MESSAGE = "Combat tag message";

    public static final String ENABLE_TELEPORT_TIMER = "Enable adventurers guild teleport timer";
    public static final String TELEPORT_TIMER_DURATION = "Teleport timer duration";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    private Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig(){

        configuration.addDefault(ENABLE_COMBAT_TAG, true);
        configuration.addDefault(COMBAT_TAG_MESSAGE, "&c[EliteMobs] Combat tag activated!");
        configuration.addDefault(ENABLE_TELEPORT_TIMER, true);
        configuration.addDefault(TELEPORT_TIMER_DURATION, 5);

        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
