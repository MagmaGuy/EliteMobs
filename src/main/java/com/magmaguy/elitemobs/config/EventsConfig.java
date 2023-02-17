package com.magmaguy.elitemobs.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

public class EventsConfig {
    @Getter
    private
    static boolean announcementBroadcastWorldOnly;
    @Getter
    private static int actionEventMinimumCooldown;
    @Getter
    private static boolean actionEventsEnabled;
    @Getter
    private static boolean timedEventsEnabled;

    private EventsConfig() {
    }

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("events.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        announcementBroadcastWorldOnly = ConfigurationEngine.setBoolean(
                List.of("Sets if events will only broadcasted in the world the events happens in."),
                fileConfiguration, "Only broadcast event message in event worlds", false);
        actionEventMinimumCooldown = ConfigurationEngine.setInt(
                List.of("Sets the minimum cooldown, in minutes, between action events"),
                fileConfiguration, "actionEventMinimumCooldownMinutes", 60 * 4);
        actionEventsEnabled = ConfigurationEngine.setBoolean(
                List.of("Sets if action events will happen.",
                        "https://github.com/MagmaGuy/EliteMobs/wiki/Creating-Custom-Events#action-events"),
                fileConfiguration, "actionEventsEnabled", true);
        timedEventsEnabled = ConfigurationEngine.setBoolean(
                List.of("Sets if timed events will happen.",
                        "https://github.com/MagmaGuy/EliteMobs/wiki/Creating-Custom-Events#timed-events"),
                fileConfiguration, "timedEventsEnabled", true);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
