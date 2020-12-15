package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
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
    public static boolean usePermissions;
    public static boolean setupDone;
    public static Location defaultSpawnLocation;

    private static File file = null;
    private static FileConfiguration fileConfiguration = null;

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
        usePermissions = ConfigurationEngine.setBoolean(fileConfiguration, "Use permissions", false);
        setupDone = ConfigurationEngine.setBoolean(fileConfiguration, "setupDone", false);
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

    public static void setUsePermissions(boolean bool, CommandSender commandSender) {
        usePermissions = bool;
        fileConfiguration.set("Use permissions", bool);
        setupDone = true;
        fileConfiguration.set("setupDone", true);
        try {
            fileConfiguration.save(file);
        } catch (Exception ex) {
            new WarningMessage("Failed to save config.yml!");
        }

        commandSender.sendMessage("----------------------------------------------------");
        commandSender.sendMessage(ChatColorConverter.convert("&2[EliteMobs] Preference registered! Use of permissions is " + usePermissions));
        if (usePermissions)
            commandSender.sendMessage(ChatColorConverter.convert("&cReminder: Recommended user permissions is elitemobs.user"));
        else
            commandSender.sendMessage(ChatColorConverter.convert("&aPlayers will be able to access all the recommended features. OPs will have global access."));
        commandSender.sendMessage(ChatColorConverter.convert("&cYou can change this preference at any point in config.yml under \"Use permissions\""));
        commandSender.sendMessage(ChatColorConverter.convert("&4This message will not be shown again."));
        commandSender.sendMessage("----------------------------------------------------");
        commandSender.sendMessage(ChatColor.GREEN + "A video on how to set EliteMobs up is available here: " + ChatColor.DARK_BLUE + "https://youtu.be/2u71JCyGj-E");
        commandSender.sendMessage(ChatColorConverter.convert("&aNeed help?  &9https://discord.gg/9f5QSka"));
    }

}
