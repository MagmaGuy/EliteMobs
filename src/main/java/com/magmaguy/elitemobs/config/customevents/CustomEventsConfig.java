package com.magmaguy.elitemobs.config.customevents;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.customevents.premade.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CustomEventsConfig {

    private static final HashMap<String, CustomEventsConfigFields> customEvents = new HashMap<>();

    public static HashMap<String, CustomEventsConfigFields> getCustomEvents() {
        return customEvents;
    }

    public static CustomEventsConfigFields getCustomEvent(String fileName) {
        return customEvents.get(fileName);
    }

    private static final ArrayList<CustomEventsConfigFields> CustomEventsConfigFieldsList = new ArrayList(Arrays.asList(
            new BalrogEvent(),
            new FaeEvent(),
            new KrakenEvent(),
            new KillerRabbitOfCaerbannogEvent(),
            new TreasureGoblinEventConfig()
    ));

    /**
     * Initializes all configurations and stores them in a static list for later access
     */
    public static void initializeConfigs() {
        //Check if the directory doesn't exist
        if (!Files.isDirectory(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/customevents"))) {
            generateFreshConfigurations();
            return;
        }
        //Runs if the directory exists

        //Check if all the defaults exist
        for (File file : (new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/customevents")).listFiles()) {
            boolean isPremade = false;
            for (CustomEventsConfigFields customEventsConfigFields : CustomEventsConfigFieldsList) {
                if (file.getName().equalsIgnoreCase(customEventsConfigFields.getFilename())) {
                    CustomEventsConfigFieldsList.remove(customEventsConfigFields);
                    initialize(customEventsConfigFields);
                    isPremade = true;
                    break;
                }
            }
            if (!isPremade)
                initialize(file);
        }

        if (!CustomEventsConfigFieldsList.isEmpty())
            generateFreshConfigurations();

    }

    /**
     * Called when the appropriate configurations directory does not exist
     *
     * @return
     */
    private static void generateFreshConfigurations() {
        for (CustomEventsConfigFields CustomEventsConfigFields : CustomEventsConfigFieldsList)
            initialize(CustomEventsConfigFields);
    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     *
     * @param CustomEventsConfigFields
     * @return
     */
    private static void initialize(CustomEventsConfigFields CustomEventsConfigFields) {

        File file = ConfigurationEngine.fileCreator("customevents", CustomEventsConfigFields.getFilename());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        CustomEventsConfigFields.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);

        customEvents.put(file.getName(), new CustomEventsConfigFields(fileConfiguration, file));

    }

    /**
     * Called when a user-made mob is detected.
     *
     * @return
     */
    private static void initialize(File file) {
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        customEvents.put(file.getName(), new CustomEventsConfigFields(fileConfiguration, file));
    }
}
