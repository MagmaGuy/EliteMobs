package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

public class CombatTagConfig {

    public static final String ENABLE_COMBAT_TAG = "Enable combat tag";
    public static final String COMBAT_TAG_MESSAGE = "Combat tag message";

    public static final String ENABLE_TELEPORT_TIMER = "Enable adventurers guild teleport timer";
    public static final String TELEPORT_TIMER_DURATION = "Teleport timer duration";

    private static FileConfiguration fileConfiguration;

    public static boolean getBoolean(String entry) {
        return fileConfiguration.getBoolean(entry);
    }

    public static String getString(String entry) {
        return fileConfiguration.getString(entry);
    }

    public static List<String> getStringList(String entry) {
        return fileConfiguration.getStringList(entry);
    }

    public static int getInt(String entry) {
        return fileConfiguration.getInt(entry);
    }

    public static void initializeConfig() {
        File file = EliteConfigGenerator.getFile("CombatTag.yml");
        fileConfiguration = EliteConfigGenerator.getFileConfiguration(file);

        fileConfiguration.addDefault(ENABLE_COMBAT_TAG, true);
        fileConfiguration.addDefault(COMBAT_TAG_MESSAGE, "&c[EliteMobs] Combat tag activated!");
        fileConfiguration.addDefault(ENABLE_TELEPORT_TIMER, true);
        fileConfiguration.addDefault(TELEPORT_TIMER_DURATION, 5);

        EliteConfigGenerator.saveDefaults(file, fileConfiguration);
    }

}
