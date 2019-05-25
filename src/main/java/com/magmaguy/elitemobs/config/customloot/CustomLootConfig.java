package com.magmaguy.elitemobs.config.customloot;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.UnusedNodeHandler;
import com.magmaguy.elitemobs.config.customloot.premade.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CustomLootConfig {

    private static HashMap<String, FileConfiguration> customItemsConfigList = new HashMap<>();

    public static HashMap<String, FileConfiguration> getCustomItemsConfigList() {
        return customItemsConfigList;
    }

    public static FileConfiguration getCustomLootConfig(String fileName) {
        return customItemsConfigList.get(fileName);
    }

    private static ArrayList<CustomLootConfigFields> initializedCustomLootConfigFieldsList = new ArrayList<>();

    public static ArrayList<CustomLootConfigFields> getInitializedCustomLootConfigFieldsList() {
        return initializedCustomLootConfigFieldsList;
    }

    private static ArrayList<CustomLootConfigFields> customLootConfigFieldsList = new ArrayList<>(Arrays.asList(
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
            new ZombieKingsAxeConfig()
    ));

    public static void initializeConfigurations() {
        //Check if the directory doesn't exist
        if (!Files.isDirectory(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/customitems"))) {
            generateFreshConfigurations();
            return;
        }
        //Runs if the directory exists

        //Check if all the defaults exist
        for (File file : (new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/customitems")).listFiles()) {
            Bukkit.getLogger().warning("File: " + file.getName());
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
            customItemsConfigList.put(customLootConfigFields.getFileName(), initializeConfiguration(customLootConfigFields));
    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     *
     * @param customLootConfigFields
     * @return
     */
    private static FileConfiguration initializeConfiguration(CustomLootConfigFields customLootConfigFields) {

        File file = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/customitems", customLootConfigFields.getFileName());

        if (!file.exists())
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException ex) {
                Bukkit.getLogger().warning("[EliteMobs] Error generating the plugin file: " + file.getName());
            }

        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        fileConfiguration.addDefaults(customLootConfigFields.generateConfigDefaults());
        fileConfiguration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(fileConfiguration);

        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        customItemsConfigList.put(file.getName(), fileConfiguration);

        return fileConfiguration;

    }

    /**
     * Called when a user-made loot is detected.
     *
     * @return
     */
    private static FileConfiguration initializeConfiguration(File file) {
        //TODO: add actual checks of what people are putting in here
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        return configuration;
    }


}
