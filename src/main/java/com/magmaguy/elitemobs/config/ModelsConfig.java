package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;

import java.util.List;

public class ModelsConfig extends ConfigurationFile {
    public static String treasureChest;

    public ModelsConfig() {
        super("Models.yml");
    }

    @Override
    public void initializeValues() {
        treasureChest = ConfigurationEngine.setString(
                List.of("Name of the custom model for the treasure chest"),
                file, fileConfiguration, "treasureChest", "elitemobs_treasure_chest", false);
    }
}
