package com.magmaguy.elitemobs.config.customtreasurechests;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.customtreasurechests.premade.TestCustomTreasureChestConfig;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CustomTreasureChestsConfig {

    private static final HashMap<String, CustomTreasureChestConfigFields> customTreasureChestConfigFields = new HashMap<>();

    public static HashMap<String, CustomTreasureChestConfigFields> getCustomTreasureChestConfigFields() {
        return customTreasureChestConfigFields;
    }

    public static CustomTreasureChestConfigFields getCustomBoss(String fileName) {
        return customTreasureChestConfigFields.get(fileName);
    }

    private static final ArrayList<CustomTreasureChestConfigFields> customBossConfigFieldsList = new ArrayList(Arrays.asList(
            new TestCustomTreasureChestConfig()
    ));

    /**
     * Initializes all configurations and stores them in a static list for later access
     */
    public static void initializeConfigs() {
        //Check if the directory doesn't exist
        if (!Files.isDirectory(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/customtreasurechests"))) {
            generateFreshConfigurations();
            return;
        }
        //Runs if the directory exists

        //Check if all the defaults exist
        for (File file : (new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/customtreasurechests")).listFiles()) {
            boolean isPremade = false;
            for (CustomTreasureChestConfigFields customBossConfigFields : customBossConfigFieldsList) {
                if (file.getName().equalsIgnoreCase(customBossConfigFields.getFileName())) {
                    customBossConfigFieldsList.remove(customBossConfigFields);
                    initializeConfiguration(customBossConfigFields);
                    isPremade = true;
                    break;
                }
            }
            if (!isPremade)
                initializeConfiguration(file);
        }

        if (!customBossConfigFieldsList.isEmpty())
            generateFreshConfigurations();

    }

    /**
     * Called when the appropriate configurations directory does not exist
     *
     * @return
     */
    private static void generateFreshConfigurations() {
        for (CustomTreasureChestConfigFields customTreasureChestConfigFields : customBossConfigFieldsList)
            initializeConfiguration(customTreasureChestConfigFields);
    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     *
     * @param customTreasureChestConfigFields
     * @return
     */
    private static FileConfiguration initializeConfiguration(CustomTreasureChestConfigFields customTreasureChestConfigFields) {

        File file = ConfigurationEngine.fileCreator("customtreasurechests", customTreasureChestConfigFields.getFileName());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        customTreasureChestConfigFields.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);

        CustomTreasureChestsConfig.customTreasureChestConfigFields.put(file.getName(), new CustomTreasureChestConfigFields(fileConfiguration, file));

        return fileConfiguration;

    }

    /**
     * Called when a user-made mob is detected.
     *
     * @return
     */
    private static FileConfiguration initializeConfiguration(File file) {
        //TODO: add actual checks of what people are putting in here
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        customTreasureChestConfigFields.put(file.getName(), new CustomTreasureChestConfigFields(fileConfiguration, file));
        return fileConfiguration;
    }

}
