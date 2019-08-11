package com.magmaguy.elitemobs.config.events.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.events.EventsFieldConfig;
import org.bukkit.configuration.file.FileConfiguration;

public class BalrogEventConfig extends EventsFieldConfig {
    public BalrogEventConfig() {
        super("balrog",
                true);
    }

    public static boolean isEnabled;
    public static double chanceOnMine;

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        isEnabled = ConfigurationEngine.setBoolean(fileConfiguration, "isEnabled", true);
        chanceOnMine = ConfigurationEngine.setDouble(fileConfiguration, "chanceOnMine", 0.0005);
    }
}
