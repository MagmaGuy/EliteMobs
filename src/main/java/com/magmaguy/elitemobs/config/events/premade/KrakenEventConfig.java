package com.magmaguy.elitemobs.config.events.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.events.EventsFieldConfig;
import org.bukkit.configuration.file.FileConfiguration;

public class KrakenEventConfig extends EventsFieldConfig {
    public KrakenEventConfig() {
        super("kraken",
                true);
    }

    public static boolean isEnabled;
    public static double chanceOnFish;
    public static String krakenName;

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        isEnabled = ConfigurationEngine.setBoolean(fileConfiguration, "isEnabled", true);
        chanceOnFish = ConfigurationEngine.setDouble(fileConfiguration, "chanceOnFish", 0.005);
        krakenName = ConfigurationEngine.setString(fileConfiguration, "krakenName", "&4Kraken");
    }

}
