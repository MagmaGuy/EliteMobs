package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

public class EventsConfig {

    public static final String CONFIG_NAME = "events.yml";
    public static boolean announcementBroadcastWorldOnly;
    public static int actionEventMinimumCooldown;
    public static boolean actionEventsEnabled, timedEventsEnabled;

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    FileConfiguration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        announcementBroadcastWorldOnly = ConfigurationEngine.setBoolean(configuration, "Only broadcast event message in event worlds", false);
        actionEventMinimumCooldown = ConfigurationEngine.setInt(configuration, "actionEventMinimumCooldownMinutes", 60 * 4);
        actionEventsEnabled = ConfigurationEngine.setBoolean(configuration, "actionEventsEnabled", true);
        timedEventsEnabled = ConfigurationEngine.setBoolean(configuration, "timedEventsEnabled", true);
        configuration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
