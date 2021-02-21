package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

public class EventsConfig {

    public static final String CONFIG_NAME = "events.yml";
    public static final String ENABLE_EVENTS = "Enable events";
    public static final String MINIMUM_ONLINE_PLAYERS = "Minimum amount of online players for event to trigger";
    public static final String MAXIMUM_ONLINE_PLAYERS = "Maximum amount of online players after which the event frequency won't increase";
    public static final String MINIMUM_EVENT_FREQUENCY = "Minimum event frequency (minutes)";
    public static final String MAXIMUM_EVENT_FREQUENCY = "Maximum event frequency (minutes)";
    public static final String ANNOUNCEMENT_BROADCAST_WORLD_ONLY = "Only broadcast event message in event worlds";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(ENABLE_EVENTS, true);
        configuration.addDefault(MINIMUM_ONLINE_PLAYERS, 2);
        configuration.addDefault(MAXIMUM_ONLINE_PLAYERS, 100);
        configuration.addDefault(MINIMUM_EVENT_FREQUENCY, 45);
        configuration.addDefault(MAXIMUM_EVENT_FREQUENCY, 10);
        configuration.addDefault(ANNOUNCEMENT_BROADCAST_WORLD_ONLY, false);

        configuration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
