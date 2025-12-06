package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.List;

public class DungeonsConfig extends ConfigurationFile {

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
    @Getter
    private static String dynamicDungeonLevelSelectionMenuTitle;
    @Getter
    private static String dynamicDungeonLevelSelectionItemTitle;
    @Getter
    private static String dynamicDungeonLevelSelectionClickToSelect;
    @Getter
    private static String dynamicDungeonLevelSelectionMobsWillBeLevel;
    @Getter
    private static String dynamicDungeonLevelSelectionRecommended;
    @Getter
    private static String dynamicDungeonLevelSelectionEasier;
    @Getter
    private static String dynamicDungeonLevelSelectionHarder;
    @Getter
    private static String dynamicDungeonDifficultySelectionMenuTitle;
    @Getter
    private static String dynamicDungeonDifficultySelectionSelectedLevel;
    @Getter
    private static String dungeonLockoutSubtitle;
    @Getter
    private static String dungeonLockoutChatMessage;

    public DungeonsConfig() {
        super("dungeons.yml");
    }

    @Override
    public void initializeValues() {

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
        dynamicDungeonLevelSelectionMenuTitle = ConfigurationEngine.setString(
                List.of("Sets the title for the dynamic dungeon level selection menu"),
                file, fileConfiguration, "dynamicDungeonLevelSelectionMenuTitle", "&8Select Dungeon Level", true);
        dynamicDungeonLevelSelectionItemTitle = ConfigurationEngine.setString(
                List.of("Sets the title for level selection items in the dynamic dungeon menu",
                        "$level is the placeholder for the level number"),
                file, fileConfiguration, "dynamicDungeonLevelSelectionItemTitle", "&fLevel $level", true);
        dynamicDungeonLevelSelectionClickToSelect = ConfigurationEngine.setString(
                List.of("Sets the 'click to select' text in the dynamic dungeon level selection menu",
                        "$level is the placeholder for the level number"),
                file, fileConfiguration, "dynamicDungeonLevelSelectionClickToSelect", "&7Click to select level &e$level", true);
        dynamicDungeonLevelSelectionMobsWillBeLevel = ConfigurationEngine.setString(
                List.of("Sets the 'mobs will be level' text in the dynamic dungeon level selection menu",
                        "$level is the placeholder for the level number"),
                file, fileConfiguration, "dynamicDungeonLevelSelectionMobsWillBeLevel", "&7All mobs will be level &e$level", true);
        dynamicDungeonLevelSelectionRecommended = ConfigurationEngine.setString(
                List.of("Sets the 'recommended level' text in the dynamic dungeon level selection menu"),
                file, fileConfiguration, "dynamicDungeonLevelSelectionRecommended", "&a&lRecommended level!", true);
        dynamicDungeonLevelSelectionEasier = ConfigurationEngine.setString(
                List.of("Sets the 'easier than recommended' text in the dynamic dungeon level selection menu"),
                file, fileConfiguration, "dynamicDungeonLevelSelectionEasier", "&eEasier than recommended", true);
        dynamicDungeonLevelSelectionHarder = ConfigurationEngine.setString(
                List.of("Sets the 'harder than recommended' text in the dynamic dungeon level selection menu"),
                file, fileConfiguration, "dynamicDungeonLevelSelectionHarder", "&6Harder than recommended", true);
        dynamicDungeonDifficultySelectionMenuTitle = ConfigurationEngine.setString(
                List.of("Sets the title for the dynamic dungeon difficulty selection menu"),
                file, fileConfiguration, "dynamicDungeonDifficultySelectionMenuTitle", "&8Select Difficulty", true);
        dynamicDungeonDifficultySelectionSelectedLevel = ConfigurationEngine.setString(
                List.of("Sets the 'selected level' text in the dynamic dungeon difficulty selection menu",
                        "$level is the placeholder for the selected level number"),
                file, fileConfiguration, "dynamicDungeonDifficultySelectionSelectedLevel", "&7Selected Level: &e$level", true);
        dungeonLockoutSubtitle = ConfigurationEngine.setString(
                List.of("Sets the subtitle shown when a player kills a boss they are locked out from"),
                file, fileConfiguration, "dungeonLockoutSubtitle", "&cLockout!", true);
        dungeonLockoutChatMessage = ConfigurationEngine.setString(
                List.of("Sets the chat message shown when a player kills a boss they are locked out from",
                        "$bossName is the placeholder for the boss name",
                        "$remainingTime is the placeholder for the remaining lockout time"),
                file, fileConfiguration, "dungeonLockoutChatMessage",
                "&c[EliteMobs] &7You killed &c$bossName &7in the last lockout period and must wait another &e$remainingTime &7before it will drop loot for you again.", true);
    }
}
