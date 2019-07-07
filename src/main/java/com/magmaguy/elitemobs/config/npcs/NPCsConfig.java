package com.magmaguy.elitemobs.config.npcs;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.UnusedNodeHandler;
import com.magmaguy.elitemobs.config.npcs.premade.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class NPCsConfig {

    private static ArrayList<NPCsConfigFields> NPCsList = new ArrayList<>();

    public static ArrayList<NPCsConfigFields> getNPCsList() {
        return NPCsList;
    }

    private static ArrayList<NPCsConfigFields> NPCsConfigFieldsList = new ArrayList<NPCsConfigFields>(Arrays.asList(
            new BarkeepConfig(),
            new BlacksmithConfig(),
            new GuildAttendantConfig(),
            new QuestGiverConfig(),
            new SpecialBlacksmithConfig()
    ));

    public static void initializeConfigs() {
        //Checks if the directory doesn't exist
        if (!Files.isDirectory(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/npcs"))) {
            generateFreshConfigurations();
            return;
        }

        //Check if all the defaults exist
        for (File file : (new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/npcs")).listFiles()) {
            boolean isPremade = false;
            for (NPCsConfigFields npCsConfigFields : NPCsConfigFieldsList) {
                if (file.getName().equalsIgnoreCase(npCsConfigFields.getFileName())) {
                    NPCsConfigFieldsList.remove(npCsConfigFields);
                    initializeConfiguration(npCsConfigFields);
                    isPremade = true;
                    break;
                }
            }
            if (!isPremade)
                initializeConfiguration(file);
        }

        if (!NPCsConfigFieldsList.isEmpty())
            generateFreshConfigurations();

    }

    /**
     * Called when the appropriate configurations directory does not exist
     *
     * @return
     */
    private static void generateFreshConfigurations() {
        for (NPCsConfigFields npCsConfigFields : NPCsConfigFieldsList)
            initializeConfiguration(npCsConfigFields);
    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     *
     * @param npCsConfigFields
     * @return
     */
    private static FileConfiguration initializeConfiguration(NPCsConfigFields npCsConfigFields) {

        File file = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/npcs", npCsConfigFields.getFileName());

        if (!file.exists())
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException ex) {
                Bukkit.getLogger().warning("[EliteMobs] Error generating the plugin file: " + file.getName());
            }

        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        fileConfiguration.addDefaults(npCsConfigFields.generateConfigDefaults(fileConfiguration));
        fileConfiguration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(fileConfiguration);

        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        NPCsList.add(new NPCsConfigFields(fileConfiguration));

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
        NPCsList.add(new NPCsConfigFields(fileConfiguration));
        return fileConfiguration;
    }

}
