package com.magmaguy.elitemobs.config.events.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.events.EventsFieldConfig;
import org.bukkit.configuration.file.FileConfiguration;

public class SmallTreasureGoblinEventConfig extends EventsFieldConfig {
    public SmallTreasureGoblinEventConfig() {
        super("small_treasure_goblin",
                true);
    }

    public static boolean isEnabled;
    public static double weight;

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        isEnabled = ConfigurationEngine.setBoolean(fileConfiguration, "isEnabled", true);
        weight = ConfigurationEngine.setDouble(fileConfiguration, "eventWeight", 40);
    }

}
