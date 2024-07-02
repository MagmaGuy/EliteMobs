package com.magmaguy.elitemobs.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

public class DungeonsConfig {

    @Getter
    private static String instancedDungeonTitle;
    @Getter
    private static List<String> instancedDungeonDescription;
    @Getter
    private static String dungeonRezInstructions;
    @Getter
    private static String dungeonLivesLeftText;
    @Getter
    private static String dungeonJoinAsPlayerText;
    @Getter
    private static String dungeonJoinAsSpectatorText;
    @Getter
    private static boolean allowSpectatorsInInstancedContent;
    @Getter
    private static String instancedDungeonCompleteMessage;
    @Getter
    private static String instancedDungeonClosingInstanceMessage;
    @Getter
    private static boolean friendlyFireInDungeons;
    @Getter
    private static double fireDamageMultiplier;
    @Getter
    private static double poisonDamageMultiplier;
    @Getter
    private static double witherDamageMultiplier;

    private DungeonsConfig() {
    }

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("dungeons.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        dungeonRezInstructions = ConfigurationEngine.setString(
                List.of("Sets the text that appears over resurrection banners in dungeons"),
                file, fileConfiguration, "dungeonRezInstructions", "&aPunch to rez!", true);
        dungeonLivesLeftText = ConfigurationEngine.setString(
                List.of("Sets the text that shows how many lives players have left in a dungeon! Placeholders:", "$amount - the amount of lives left"),
                file, fileConfiguration, "dungeonLivesLeftText", "&c$amount lives left!", true);
        dungeonJoinAsPlayerText = ConfigurationEngine.setString(
                List.of("Sets the text for joining a dungeon as a player! Placeholders:", "$dungeonName - the name of the dungeon"),
                file, fileConfiguration, "joinDungeonAsPlayerText", "&fJoin $dungeonName as a player!", true);
        dungeonJoinAsSpectatorText = ConfigurationEngine.setString(
                List.of("Sets the text for joining a dungeon as a spectator! Placeholders:", "$dungeonName - the name of the dungeon"),
                file, fileConfiguration, "joinDungeonAsSpectatorText", "&fJoin $dungeonName as a spectator!", true);
        instancedDungeonTitle = ConfigurationEngine.setString(
                List.of(
                        "Sets the title that will show up in the item description of instanced dungeon menus",
                        "$difficulty is the placeholder for the difficulty name in the configuration file of the dungeon"),
                file, fileConfiguration, "instancedDungeonTitle", "Start $difficulty difficulty dungeon!",
                true);
        instancedDungeonDescription = ConfigurationEngine.setList(
                List.of("Sets the description that will show up in the item description of instanced dungeon menus",
                        "$dungeonName is the placeholder for the dungeon name in the configuration file of the dungeon"),
                file,
                fileConfiguration,
                "instancedDungeonDescription",
                List.of("&fCreate a new instance of the dungeon", "$dungeonName &ffor yourself and maybe", "&fsome friends!"),
                true);
        allowSpectatorsInInstancedContent = ConfigurationEngine.setBoolean(
                List.of("Sets is spectating instanced content will be available."),
                fileConfiguration, "allowSpectatorsInInstancedContent", true);
        instancedDungeonCompleteMessage = ConfigurationEngine.setString(
                List.of("Sets the message that appears when an instanced dungeon is completed"),
                file, fileConfiguration, "instancedDungeonCompleteMessage", "[EliteMobs] Dungeon completed! It will self-destruct in 2 minutes!",
                true);
        instancedDungeonClosingInstanceMessage = ConfigurationEngine.setString(
                List.of("Sets the message that appears when an instanced dungeon closing"),
                file, fileConfiguration, "instancedDungeonClosingInstanceMessage", "[EliteMobs] Closing instance!",
                true);
        friendlyFireInDungeons = ConfigurationEngine.setBoolean(
                List.of("Sets if PvP will be allowed in dungeons"),
                fileConfiguration, "friendlyFireInDungeons", false);
        fireDamageMultiplier = ConfigurationEngine.setDouble(
                List.of("Sets the damage multiplier for fire damage in dungeons",
                        "This is important for balance as by default the damage is a bit too high for the dungeons as we design them"),
                fileConfiguration, "fireDamageMultiplier", 0.5);
        witherDamageMultiplier = ConfigurationEngine.setDouble(
                List.of("Sets the damage multiplier for wither damage in dungeons",
                        "This is important for balance as by default the damage is a bit too high for the dungeons as we design them"),
                fileConfiguration, "witherDamageMultiplier", 0.5);
        poisonDamageMultiplier = ConfigurationEngine.setDouble(
                List.of("Sets the damage multiplier for fire damage in dungeons",
                        "This is important for balance as by default the damage is a bit too high for the dungeons as we design them"),
                fileConfiguration, "poisonDamageMultiplier", 0.5);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
