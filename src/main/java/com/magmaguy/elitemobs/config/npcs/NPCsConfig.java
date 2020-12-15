package com.magmaguy.elitemobs.config.npcs;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.npcs.premade.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class NPCsConfig {

    private static final HashMap<String, NPCsConfigFields> NPCsList = new HashMap<>();

    public static HashMap<String, NPCsConfigFields> getNPCsList() {
        return NPCsList;
    }

    private static void addNPC(String fileName, NPCsConfigFields npCsConfigFields) {
        NPCsList.put(fileName, npCsConfigFields);
    }

    private static final ArrayList<NPCsConfigFields> NPCsConfigFieldsList = new ArrayList<NPCsConfigFields>(Arrays.asList(
            new BarkeepConfig(),
            new BlacksmithConfig(),
            new GuildAttendantConfig(),
            new QuestGiverConfig(),
            new SpecialBlacksmithConfig(),
            new CombatInstructorConfig(),
            new TravellingMerchantConfig(),
            new BackTeleporter()
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

        File file = ConfigurationEngine.fileCreator("npcs", npCsConfigFields.getFileName());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        npCsConfigFields.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
        addNPC(file.getName(), new NPCsConfigFields(fileConfiguration, file));
        return fileConfiguration;

    }

    /**
     * Called when a user-made mob is detected.
     *
     * @return
     */
    private static FileConfiguration initializeConfiguration(File file) {
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        addNPC(file.getName(), new NPCsConfigFields(fileConfiguration, file));
        return fileConfiguration;
    }

}
