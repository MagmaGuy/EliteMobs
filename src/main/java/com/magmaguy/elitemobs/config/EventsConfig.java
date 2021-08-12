package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

public class EventsConfig {

    public static final String CONFIG_NAME = "events.yml";
    public static boolean ANNOUNCEMENT_BROADCAST_WORLD_ONLY;

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault("Only broadcast event message in event worlds", false);
        configuration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
