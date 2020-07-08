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
    public static boolean preventEliteMobConversionOfNamedMobs;
    public static boolean doStrictSpawningRules;
    public static double nightmareWorldSpawnBonus;
    public static boolean emLeadsToStatusMenu;

    public static void initializeConfig() {

        File file = ConfigurationEngine.fileCreator("config.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        alwaysShowNametags = ConfigurationEngine.setBoolean(fileConfiguration, "alwaysShowEliteMobNameTags", false);
        superMobStackAmount = ConfigurationEngine.setInt(fileConfiguration, "superMobStackAmount", 50);
        preventCreeperDamageToPassiveMobs = ConfigurationEngine.setBoolean(fileConfiguration, "preventEliteCreeperDamageToPassiveMobs", true);
        doPermissionTitles = ConfigurationEngine.setBoolean(fileConfiguration, "useTitlesForMissingPermissionMessages", true);
        preventEliteMobConversionOfNamedMobs = ConfigurationEngine.setBoolean(fileConfiguration, "preventEliteMobConversionOfNamedMobs", true);
        doStrictSpawningRules = ConfigurationEngine.setBoolean(fileConfiguration, "enableHighCompatibilityMode", false);
        nightmareWorldSpawnBonus = ConfigurationEngine.setDouble(fileConfiguration, "nightmareWorldSpawnBonus", 0.5);
        emLeadsToStatusMenu = ConfigurationEngine.setBoolean(fileConfiguration, "emLeadsToStatusMenu", true);


        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);

    }

}
