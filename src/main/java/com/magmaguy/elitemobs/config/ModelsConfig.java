package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

public class ModelsConfig {
    private ModelsConfig() {
    }

    public static String treasureChest;

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("Models.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        treasureChest = ConfigurationEngine.setString(
                List.of("Name of the custom model for the treasure chest"),
                file, fileConfiguration, "treasureChest", "elitemobs_treasure_chest", false);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }
}
