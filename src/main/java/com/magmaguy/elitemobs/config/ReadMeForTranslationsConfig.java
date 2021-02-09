package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

public class ReadMeForTranslationsConfig {

    public static void initialize() {
        InputStream inputStream = MetadataHandler.PLUGIN.getResource("READ_ME_FOR_TRANSLATIONS.yml");
        try {
            FileUtils.copyInputStreamToFile(inputStream, new File(Paths.get(MetadataHandler.PLUGIN.getDataFolder() +
                    "/READ_ME_FOR_TRANSLATIONS.yml").normalize().toString()));
        } catch (Exception ex) {
            new WarningMessage("Failed to write read me for translations file!");
            ex.printStackTrace();
        }
    }

}
