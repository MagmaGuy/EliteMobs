package com.magmaguy.elitemobs.config.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.custombosses.premade.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CustomBossesConfig {

    private static final HashMap<String, CustomBossConfigFields> customBosses = new HashMap<>();

    public static HashMap<String, CustomBossConfigFields> getCustomBosses() {
        return customBosses;
    }

    public static CustomBossConfigFields getCustomBoss(String fileName) {
        return customBosses.get(fileName);
    }

    private static final ArrayList<CustomBossConfigFields> customBossConfigFieldsList = new ArrayList<>(Arrays.asList(
            new TestCustomBossConfig(),
            new ZombieKingConfig(),
            new TreasureGoblinConfig(),
            new NecronomiconZombieConfig(),
            new NecronomiconSkeletonConfig(),
            new TheReturnedConfig(),
            new BalrogConfig(),
            new RaugConfig(),
            new ZombieFriendConfig(),
            new ZombieMomConfig(),
            new ZombieDadConfig(),
            new FireFaeConfig(),
            new IceFaeConfig(),
            new LightningFaeConfig(),
            new BlayyzeConfig(),
            new EmberConfig(),
            new SnoopyConfig(),
            new SummonableWolfConfig()
    ));

    /**
     * Initializes all configurations and stores them in a static list for later access
     */
    public static void initializeConfigs() {
        //Check if the directory doesn't exist
        if (!Files.isDirectory(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/custombosses"))) {
            generateFreshConfigurations();
            return;
        }
        //Runs if the directory exists

        //Check if all the defaults exist
        for (File file : (new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/custombosses")).listFiles()) {
            boolean isPremade = false;
            for (CustomBossConfigFields customBossConfigFields : customBossConfigFieldsList) {
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
        for (CustomBossConfigFields customBossConfigFields : customBossConfigFieldsList)
            initializeConfiguration(customBossConfigFields);
    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     *
     * @param customBossConfigFields
     * @return
     */
    private static FileConfiguration initializeConfiguration(CustomBossConfigFields customBossConfigFields) {

        File file = ConfigurationEngine.fileCreator("custombosses", customBossConfigFields.getFileName());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        customBossConfigFields.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);

        customBosses.put(file.getName(), new CustomBossConfigFields(fileConfiguration, file));

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
        customBosses.put(file.getName(), new CustomBossConfigFields(fileConfiguration, file));
        return fileConfiguration;
    }

}
