package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.WarningMessage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SchematicsFolderConfig {
    private SchematicsFolderConfig() {
    }

    public static void initializeConfigs() {
        Path configurationsPath = Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath());
        if (!Files.isDirectory(Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "schematics"))) {
            try {
                Files.createDirectory(Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "schematics"));
            } catch (Exception exception) {
                new WarningMessage("Failed to create schematics directory! Tell the dev!");
                exception.printStackTrace();
            }
        }
    }
}
