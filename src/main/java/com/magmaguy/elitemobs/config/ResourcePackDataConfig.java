package com.magmaguy.elitemobs.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ResourcePackDataConfig {

    @Getter
    public static boolean eliteMobsResourcePackEnabled;
    @Getter
    public static boolean displayCustomMenuUnicodes;
    private static File file;
    private static FileConfiguration fileConfiguration;
    private ResourcePackDataConfig() {
    }

    public static void initializeConfig() {
        file = ConfigurationEngine.fileCreator("data" + File.separatorChar + "resource_pack_status.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        eliteMobsResourcePackEnabled = ConfigurationEngine.setBoolean(fileConfiguration, "eliteMobsResourcePackEnabled", false);
        displayCustomMenuUnicodes = eliteMobsResourcePackEnabled;
        if (DefaultConfig.isMenuUnicodeFormatting()) displayCustomMenuUnicodes = true;
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

    public static void toggleEliteMobsResourcePackStatus(boolean status) {
        fileConfiguration.set("eliteMobsResourcePackEnabled", status);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
        eliteMobsResourcePackEnabled = status;
        displayCustomMenuUnicodes = eliteMobsResourcePackEnabled;
    }
}
