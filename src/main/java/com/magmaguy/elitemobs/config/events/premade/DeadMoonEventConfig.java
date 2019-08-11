package com.magmaguy.elitemobs.config.events.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.events.EventsFieldConfig;
import org.bukkit.configuration.file.FileConfiguration;

public class DeadMoonEventConfig extends EventsFieldConfig {
    public DeadMoonEventConfig() {
        super("dead_moon",
                true);
    }

    public static boolean isEnabled;
    public static double weight;
    public static String eventAnnouncementMessage;

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        isEnabled = ConfigurationEngine.setBoolean(fileConfiguration, "isEnabled", true);
        weight = ConfigurationEngine.setDouble(fileConfiguration, "eventWeight", 20);
        eventAnnouncementMessage = ConfigurationEngine.setString(fileConfiguration, "eventAnnouncementMessage", "&7[EM] &5A dead moon rises, and the undead with it...");
    }

}
