package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class CustomEnchantmentsConfig {

    public static String hunterName;
    public static int hunterSpawnBonus;
    public static String flamethrowerName;


    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("CustomEnchantments.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        hunterName = ConfigurationEngine.setString(fileConfiguration, "HunterEnchantment enchantment name", "HunterEnchantment");
        hunterSpawnBonus = ConfigurationEngine.setInt(fileConfiguration,
                "Percentual elite mob increase in spawn rate around the player per hunter enchantment level", 2);
        flamethrowerName = ConfigurationEngine.setString(fileConfiguration, "FlamethrowerEnchantment enchantment name", "FlamethrowerEnchantment");

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
