package com.magmaguy.elitemobs.config.events.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.events.EventsFieldConfig;
import org.bukkit.configuration.file.FileConfiguration;

public class FaeEventConfig extends EventsFieldConfig {
    public FaeEventConfig() {
        super("fae",
                true);
    }

    public static boolean isEnabled;
    public static double chanceOnChop;

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        isEnabled = ConfigurationEngine.setBoolean(fileConfiguration, "isEnabled", true);
        chanceOnChop = ConfigurationEngine.setDouble(fileConfiguration, "chanceOnChop", 0.001);
    }
}
