package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

/**
 * Created by MagmaGuy on 08/06/2017.
 */
public class DefaultConfig {

    public static boolean alwaysShowNametags;
    public static int superMobStackAmount;
    public static boolean preventCreeperDamageToPassiveMobs;
    public static boolean doPermissionTitles;
    public static boolean hideEnchantmentsAttribute;
    public static boolean preventEliteMobConversionOfNamedMobs;
    public static boolean doStrictSpawningRules;

    public static void initializeConfig() {

        File file = ConfigurationEngine.fileCreator("config.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        alwaysShowNametags = ConfigurationEngine.setBoolean(fileConfiguration, "alwaysShowEliteMobNameTags", false);
        superMobStackAmount = ConfigurationEngine.setInt(fileConfiguration, "superMobStackAmount", 50);
        preventCreeperDamageToPassiveMobs = ConfigurationEngine.setBoolean(fileConfiguration, "preventEliteCreeperDamageToPassiveMobs", true);
        doPermissionTitles = ConfigurationEngine.setBoolean(fileConfiguration, "useTitlesForMissingPermissionMessages", true);
        hideEnchantmentsAttribute = ConfigurationEngine.setBoolean(fileConfiguration, "hideEnchantmentAttributes", false);
        preventEliteMobConversionOfNamedMobs = ConfigurationEngine.setBoolean(fileConfiguration, "preventEliteMobConversionOfNamedMobs", true);
        doStrictSpawningRules = ConfigurationEngine.setBoolean(fileConfiguration, "enableHighCompatibilityMode", false);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);

    }

}
