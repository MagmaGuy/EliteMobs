package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

public class EventsConfig {

    public static final String CONFIG_NAME = "events.yml";
    public static final String ENABLE_EVENTS = "Enable events";
    public static final String MINIMUM_ONLINE_PLAYERS = "Minimum amount of online players for event to trigger";
    public static final String MAXIMUM_ONLINE_PLAYERS = "Maximum amount of online players after which the event frequency won't increase";
    public static final String MINIMUM_EVENT_FREQUENCY = "Minimum event frequency (minutes)";
    public static final String MAXIMUM_EVENT_FREQUENCY = "Maximum event frequency (minutes)";
    private static final String ENABLED_EVENTS = "Enabled events.";
    public static final String TREASURE_GOBLIN_SMALL_ENABLED = ENABLED_EVENTS + "Small treasure goblin";
    public static final String DEAD_MOON_ENABLED = ENABLED_EVENTS + "Dead moon";
    public static final String KRAKEN_ENABLED = ENABLED_EVENTS + "Kraken";
    public static final String BALROG_ENABLED = ENABLED_EVENTS + "Balrog";
    public static final String FAE_ENABLED = ENABLED_EVENTS + "Fae";
    private static final String EVENT_WEIGHT = "Event weight.";
    public static final String SMALL_TREASURE_GOBLIN_EVENT_WEIGHT = EVENT_WEIGHT + "Small treasure goblin";
    public static final String DEAD_MOON_EVENT_WEIGHT = EVENT_WEIGHT + "Blood moon";
    public static final String KRAKEN_CHANCE_ON_FISH = "Kraken on fish chance";
    public static final String KRAKEN_NAME = "Name of mob in Kraken event";
    public static final String BALROG_CHANCE_ON_MINE = "Balrog chance on mine";
    public static final String FAE_CHANCE_ON_CHOP = "Fae chance on chop";
    public static final String ANNOUNCEMENT_BROADCAST_WORLD_ONLY = "Only broadcast event message in event world";
    public static final String DEADMOON_ANNOUNCEMENT_MESSAGE = "Deadmoon announcement message";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(ENABLE_EVENTS, true);
        configuration.addDefault(MINIMUM_ONLINE_PLAYERS, 2);
        configuration.addDefault(MAXIMUM_ONLINE_PLAYERS, 100);
        configuration.addDefault(MINIMUM_EVENT_FREQUENCY, 45);
        configuration.addDefault(MAXIMUM_EVENT_FREQUENCY, 10);
        configuration.addDefault(TREASURE_GOBLIN_SMALL_ENABLED, true);
        configuration.addDefault(DEAD_MOON_ENABLED, true);
        configuration.addDefault(SMALL_TREASURE_GOBLIN_EVENT_WEIGHT, 40);
        configuration.addDefault(DEAD_MOON_EVENT_WEIGHT, 20);
        configuration.addDefault(KRAKEN_ENABLED, true);
        configuration.addDefault(BALROG_ENABLED, true);
        configuration.addDefault(KRAKEN_CHANCE_ON_FISH, 0.005);
        configuration.addDefault(KRAKEN_NAME, "&1Kraken");
        configuration.addDefault(BALROG_CHANCE_ON_MINE, 0.0005);
        configuration.addDefault(FAE_ENABLED, true);
        configuration.addDefault(FAE_CHANCE_ON_CHOP, 0.001);
        configuration.addDefault(ANNOUNCEMENT_BROADCAST_WORLD_ONLY, false);
        configuration.addDefault(DEADMOON_ANNOUNCEMENT_MESSAGE, "A dead moon rises, and the undead with it...");

        configuration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
