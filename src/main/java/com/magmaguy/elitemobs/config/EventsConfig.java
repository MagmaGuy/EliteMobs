package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.List;

public class EventsConfig extends ConfigurationFile {
    @Getter
    private static boolean announcementBroadcastWorldOnly;
    @Getter
    private static int actionEventMinimumCooldown;
    @Getter
    private static int timedEventMinimumCooldown;
    @Getter
    private static boolean actionEventsEnabled;
    @Getter
    private static boolean timedEventsEnabled;

    public EventsConfig() {
        super("events.yml");
    }

    private static EventsConfig instance;

    public static void setEventsEnabled(boolean enabled){
        ConfigurationEngine.writeValue(enabled, instance.file,instance.fileConfiguration, "actionEventsEnabled");
        ConfigurationEngine.writeValue(enabled, instance.file,instance.fileConfiguration, "timedEventsEnabled");
        timedEventsEnabled = enabled;
        actionEventsEnabled = enabled;
    }

    @Override
    public void initializeValues() {
        instance = this;
        announcementBroadcastWorldOnly = ConfigurationEngine.setBoolean(
                List.of("Sets if events will only broadcasted in the world the events happens in."),
                fileConfiguration, "Only broadcast event message in event worlds", false);
        actionEventMinimumCooldown = ConfigurationEngine.setInt(
                List.of("Sets the minimum cooldown, in minutes, between action events"),
                fileConfiguration, "actionEventMinimumCooldownMinutes", 4);
        timedEventMinimumCooldown = ConfigurationEngine.setInt(
                List.of("Sets the minimum cooldown, in minutes, between timed events"),
                fileConfiguration, "timedEventMinimumCooldownMinutes", 4);
        actionEventsEnabled = ConfigurationEngine.setBoolean(
                List.of("Sets if action events will happen.",
                        "https://github.com/MagmaGuy/EliteMobs/wiki/Creating-Custom-Events#action-events"),
                fileConfiguration, "actionEventsEnabled", true);
        timedEventsEnabled = ConfigurationEngine.setBoolean(
                List.of("Sets if timed events will happen.",
                        "https://github.com/MagmaGuy/EliteMobs/wiki/Creating-Custom-Events#timed-events"),
                fileConfiguration, "timedEventsEnabled", true);

    }
}
