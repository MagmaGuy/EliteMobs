package com.magmaguy.elitemobs.config.events.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.events.EventsFieldConfig;
import org.bukkit.configuration.file.FileConfiguration;

public class MeteorEventConfig extends EventsFieldConfig {
    public MeteorEventConfig() {
        super("meteor",
                true);
    }

    public static boolean isEnabled;
    public static double weight;
    public static boolean dropsBlocks;
    public static String eventAnnouncementMessage;

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        isEnabled = ConfigurationEngine.setBoolean(fileConfiguration, "isEnabled", true);
        weight = ConfigurationEngine.setDouble(fileConfiguration, "weight", 20);
        dropsBlocks = ConfigurationEngine.setBoolean(fileConfiguration, "dropsBlocks", true);
        eventAnnouncementMessage = ConfigurationEngine.setString(fileConfiguration, "eventAnnouncementMessage", "&7[EM] &6A weird meteorite has been sighted at $location!");
    }

}
