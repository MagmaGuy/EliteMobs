package com.magmaguy.elitemobs.config.dungeonpackager;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.dungeonpackager.premade.*;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DungeonPackagerConfig {

    public static final HashMap<String, DungeonPackagerConfigFields> dungeonPackages = new HashMap<>();

    private static final ArrayList<DungeonPackagerConfigFields> dungeonPackagerConfigFields = new ArrayList(Arrays.asList(
            new DarkCathedralLair(),
            new ColosseumLair(),
            new SewersMinidungeon(),
            new DarkSpireMinidungeon(),
            new HallosseumLair()
    ));

    public static void initializeConfigs() {
        for (DungeonPackagerConfigFields dungeonPackagerConfigFields : dungeonPackagerConfigFields)
            initializeConfiguration(dungeonPackagerConfigFields);

    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     */
    private static FileConfiguration initializeConfiguration(DungeonPackagerConfigFields dungeonPackagerConfigFields) {

        File file = ConfigurationEngine.fileCreator("dungeonpackages", dungeonPackagerConfigFields.getFileName());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        dungeonPackagerConfigFields.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
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
}
