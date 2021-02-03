package com.magmaguy.elitemobs.config.customloot;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.customloot.premade.*;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class CustomLootConfig {

    private static final ArrayList<CustomLootConfigFields> customLootConfigFieldsList = new ArrayList<>(Arrays.asList(
            new BerserkerCharmConfig(),
            new ChameleonCharmConfig(),
            new CheetahCharmConfig(),
            new DepthsSeekerConfig(),
            new DwarvenGreedConfig(),
            new LuckyCharmsConfig(),
            new ElephantCharmConfig(),
            new FireflyCharmConfig(),
            new FishyCharmConfig(),
            new MagmaguysToothpickConfig(),
            new OwlCharmConfig(),
            new RabbitCharmConfig(),
            new SalamanderCharmConfig(),
            new ScorpionCharm(),
            new ShulkerCharmConfig(),
            new SlowpokeCharmConfig(),
            new TheFellerConfig(),
            new VampiricCharmConfig(),
            new ZombieKingsAxeConfig(),
            new MeteorShowerScrollConfig(),
            new SummonMerchantScrollConfig(),
            new SummonWolfScrollConfig()
    ));

    public static void initializeConfigs() {
        //Check if the directory doesn't exist
        if (!Files.isDirectory(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/customitems"))) {
            generateFreshConfigurations();
            return;
        }
        //Runs if the directory exists

        //Check if all the defaults exist
        for (File file : (new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/customitems")).listFiles()) {
            boolean isPremade = false;
            for (CustomLootConfigFields customLootConfigFields : customLootConfigFieldsList) {
                if (file.getName().equalsIgnoreCase(customLootConfigFields.getFileName())) {
                    customLootConfigFieldsList.remove(customLootConfigFields);
                    initializeConfiguration(customLootConfigFields);
                    isPremade = true;
                    break;
                }
            }
            if (!isPremade)
                initializeConfiguration(file);
        }

        if (!customLootConfigFieldsList.isEmpty())
            generateFreshConfigurations();


    }

    /**
     * Called when the appropriate configurations directory does not exist
     *
     * @return
     */
    private static void generateFreshConfigurations() {
        for (CustomLootConfigFields customLootConfigFields : customLootConfigFieldsList)
            initializeConfiguration(customLootConfigFields);
    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     *
     * @param customLootConfigFields
     * @return
     */
    private static FileConfiguration initializeConfiguration(CustomLootConfigFields customLootConfigFields) {

        File file = ConfigurationEngine.fileCreator("customitems", customLootConfigFields.getFileName());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        customLootConfigFields.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);

        CustomLootConfigFields.addCustomLootConfigField(new CustomLootConfigFields(file.getName(), fileConfiguration));

        return fileConfiguration;

    }

    /**
     * Called when a user-made loot is detected.
     *
     * @return
     */
    private static FileConfiguration initializeConfiguration(File file) {
        try {
            FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
            CustomLootConfigFields.addCustomLootConfigField(new CustomLootConfigFields(file.getName(), fileConfiguration));
            return fileConfiguration;
        } catch (Exception e) {
            new WarningMessage("Warning: Custom Item " + file.getName() + " has an invalid configuration file and has therefore failed to load!");
            return null;
        }
    }


}
