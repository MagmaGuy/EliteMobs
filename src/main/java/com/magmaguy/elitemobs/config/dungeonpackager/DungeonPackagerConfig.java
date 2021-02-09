package com.magmaguy.elitemobs.config.dungeonpackager;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.dungeonpackager.premade.*;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DungeonPackagerConfig {

    public static final HashMap<String, DungeonPackagerConfigFields> dungeonPackages = new HashMap<>();

    private static final ArrayList<DungeonPackagerConfigFields> dungeonPackagerConfigFields = new ArrayList<>();

    public static void initializeConfigs() {

        dungeonPackagerConfigFields.addAll(Arrays.asList(
                new DarkCathedralLair(),
                new ColosseumLair(),
                new SewersMinidungeon(),
                new DarkSpireMinidungeon(),
                new HallosseumLair(),
                new CatacombsLair(),
                new PirateShipMinidungeon(),
                new NorthPoleMinidungeon(),
                new AirShipMinidungeon(),
                new OasisAdventure()));

        if (!Files.isDirectory(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/dungeonpackages"))) {
            generateFreshConfigurations();
            return;
        }

        //Check if all the defaults exist
        for (File file : (new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/dungeonpackages")).listFiles()) {
            boolean isPremade = false;
            for (DungeonPackagerConfigFields iteratedDungeonPackagerConfigFields : dungeonPackagerConfigFields) {
                if (file.getName().equalsIgnoreCase(iteratedDungeonPackagerConfigFields.getFileName())) {
                    dungeonPackagerConfigFields.remove(iteratedDungeonPackagerConfigFields);
                    initializeConfiguration(iteratedDungeonPackagerConfigFields);
                    isPremade = true;
                    break;
                }
            }
            if (!isPremade)
                initializeConfiguration(file);
        }

        if (!dungeonPackagerConfigFields.isEmpty())
            generateFreshConfigurations();

    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     */
    private static FileConfiguration initializeConfiguration(DungeonPackagerConfigFields dungeonPackagerConfigFields) {

        File file = ConfigurationEngine.fileCreator("dungeonpackages", dungeonPackagerConfigFields.getFileName());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        dungeonPackagerConfigFields.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
        DungeonPackagerConfigFields dungeonPackagerConfigFields1 = new DungeonPackagerConfigFields(fileConfiguration, file);
        dungeonPackages.put(file.getName(), dungeonPackagerConfigFields1);
        try {
            new Minidungeon(dungeonPackagerConfigFields1);
        } catch (Exception ex) {
            new WarningMessage("Error while parsing a minidungeon!");
            ex.printStackTrace();
        }
        return fileConfiguration;

    }

    private static FileConfiguration initializeConfiguration(File file) {
        //TODO: add actual checks of what people are putting in here
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
        dungeonPackagerConfigFields.add(new DungeonPackagerConfigFields(fileConfiguration, file));
        return fileConfiguration;
    }

    private static void generateFreshConfigurations() {
        for (DungeonPackagerConfigFields dungeonPackagerConfigFields : dungeonPackagerConfigFields)
            initializeConfiguration(dungeonPackagerConfigFields);
    }

}
