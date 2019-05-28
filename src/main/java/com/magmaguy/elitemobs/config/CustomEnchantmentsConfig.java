package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

public class CustomEnchantmentsConfig {

    public static String HUNTER_NAME = "HunterEnchantment enchantment name";
    public static String HUNTER_SPAWN_BONUS = "Percentual elite mob increase in spawn rate around the player per hunter enchantment level";
    public static String FLAMETHROWER_NAME = "FlamethrowerEnchantment enchantment name";

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
        File file = EliteConfigGenerator.getFile("CustomEnchantments.yml");
        fileConfiguration = EliteConfigGenerator.getFileConfiguration(file);

        fileConfiguration.addDefault(HUNTER_NAME, "HunterEnchantment");
        fileConfiguration.addDefault(HUNTER_SPAWN_BONUS, 2);
        fileConfiguration.addDefault(FLAMETHROWER_NAME, "FlamethrowerEnchantment");

        EliteConfigGenerator.saveDefaults(file, fileConfiguration);
    }

}
