package com.magmaguy.elitemobs.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

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

        announcementBroadcastWorldOnly = ConfigurationEngine.setBoolean(fileConfiguration, "Only broadcast event message in event worlds", false);
        actionEventMinimumCooldown = ConfigurationEngine.setInt(fileConfiguration, "actionEventMinimumCooldownMinutes", 60 * 4);
        actionEventsEnabled = ConfigurationEngine.setBoolean(fileConfiguration, "actionEventsEnabled", true);
        timedEventsEnabled = ConfigurationEngine.setBoolean(fileConfiguration, "timedEventsEnabled", true);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
