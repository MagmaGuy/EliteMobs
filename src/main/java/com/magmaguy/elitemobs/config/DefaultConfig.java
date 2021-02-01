package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
    public static boolean otherCommandsLeadToEMStatusMenu;
    public static boolean setupDone;
    public static Location defaultSpawnLocation;

    private static File file = null;
    private static FileConfiguration fileConfiguration = null;

    public static void toggleSetupDone() {
        setupDone = !setupDone;
        fileConfiguration.set("setupDoneV3", setupDone);
        save();
    }

    public static void save() {
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }


    public static void initializeConfig() {

        file = ConfigurationEngine.fileCreator("config.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        alwaysShowNametags = ConfigurationEngine.setBoolean(fileConfiguration, "alwaysShowEliteMobNameTags", false);
        superMobStackAmount = ConfigurationEngine.setInt(fileConfiguration, "superMobStackAmount", 50);
        preventCreeperDamageToPassiveMobs = ConfigurationEngine.setBoolean(fileConfiguration, "preventEliteCreeperDamageToPassiveMobs", true);
        doPermissionTitles = ConfigurationEngine.setBoolean(fileConfiguration, "useTitlesForMissingPermissionMessages", true);
        preventEliteMobConversionOfNamedMobs = ConfigurationEngine.setBoolean(fileConfiguration, "preventEliteMobConversionOfNamedMobs", true);
        doStrictSpawningRules = ConfigurationEngine.setBoolean(fileConfiguration, "enableHighCompatibilityMode", false);
        nightmareWorldSpawnBonus = ConfigurationEngine.setDouble(fileConfiguration, "nightmareWorldSpawnBonus", 0.5);
        emLeadsToStatusMenu = ConfigurationEngine.setBoolean(fileConfiguration, "emLeadsToStatusMenu", true);
        otherCommandsLeadToEMStatusMenu = ConfigurationEngine.setBoolean(fileConfiguration, "otherCommandsLeadToEMStatusMenu", true);
        setupDone = ConfigurationEngine.setBoolean(fileConfiguration, "setupDoneV3", false);
        try {
            defaultSpawnLocation = ConfigurationLocation.deserialize(
                    ConfigurationEngine.setString(
                            fileConfiguration, "defaultSpawnLocation",
                            ConfigurationLocation.serialize(Bukkit.getWorlds().get(0).getSpawnLocation())));
        } catch (Exception ex) {
            new WarningMessage("There is an issue with your defaultSpawnLocation in the config.yml configuration file! Fix it!");
        }

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
