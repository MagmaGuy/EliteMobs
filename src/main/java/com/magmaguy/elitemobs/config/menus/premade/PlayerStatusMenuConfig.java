package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import org.bukkit.configuration.file.FileConfiguration;

public class PlayerStatusMenuConfig extends MenusConfigFields {
    public PlayerStatusMenuConfig() {
        super("player_status_screen");
    }

    public static boolean doIndexPage, doStatsPage, doGearPage, doTeleportsPage, doCommandsPage, doQuestTrackingPage, doBossTrackingPage;
    public static String[] indexTextLines = new String[13], indexHoverLines = new String[13], indexCommandLines = new String[13];
    public static String[] statsTextLines = new String[13], statsHoverLines = new String[13], statsCommandLines = new String[13];
    public static String[] gearTextLines = new String[13], gearHoverLines = new String[13], gearCommandLines = new String[13];
    public static String[] teleportTextLines = new String[13], teleportHoverLines = new String[13], teleportCommandLines = new String[13];
    public static String[] commandsTextLines = new String[13], commandsHoverLines = new String[13], commandsCommandLines = new String[13];
    public static String[] bossTrackerTextLines = new String[13], bossTrackerHoverLines = new String[13], bossTrackerCommandLines = new String[13];
    public static String[] questTrackerTextLines = new String[13], questTrackerHoverLines = new String[13], questTrackerCommandLines = new String[13];
    public static String onBossTrackHover, onQuestTrackHover, onTeleportHover;

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {

        doIndexPage = ConfigurationEngine.setBoolean(fileConfiguration, "doIndexPage", true);
        doStatsPage = ConfigurationEngine.setBoolean(fileConfiguration, "doStatsPage", true);
        doGearPage = ConfigurationEngine.setBoolean(fileConfiguration, "doGearPage", true);
        doTeleportsPage = ConfigurationEngine.setBoolean(fileConfiguration, "doTeleportsPage", true);
        doCommandsPage = ConfigurationEngine.setBoolean(fileConfiguration, "doCommandsPage", true);
        doQuestTrackingPage = ConfigurationEngine.setBoolean(fileConfiguration, "doQuestTrackingPage", true);
        doBossTrackingPage = ConfigurationEngine.setBoolean(fileConfiguration, "doBossTrackingPage", true);

        indexLineCreator(0, "&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration);
        indexLineCreator(1, "&5&l/ag &7- &6EliteMobs Hub",
                "CLICK TO USE\\n" +
                        "The place where you can find\\n" +
                        "NPCs that give quests, buy and\\n" +
                        "sell items, give advice and more!",
                "/ag", fileConfiguration);
        indexLineCreator(2, "&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration);
        indexLineCreator(3, "", "", "", fileConfiguration);
        indexLineCreator(4, "&6&lIndex", "", "", fileConfiguration);
        indexLineCreator(5, "", "", "", fileConfiguration);
        indexLineCreator(6, "&bp. $statsPage &8- &6Stats", "Click to go!", "$statsPage", fileConfiguration);
        indexLineCreator(7, "&bp. $gearPage &8- &6Gear", "Click to go!", "$gearPage", fileConfiguration);
        indexLineCreator(8, "&bp. $teleportsPage &8- &6Teleports", "Click to go!", "$teleportsPage", fileConfiguration);
        indexLineCreator(9, "&bp. $commandsPage &8- &6Commands", "Click to go!", "$commandsPage", fileConfiguration);
        indexLineCreator(10, "&bp. $questsPage &8- &6Quest Tracking", "Click to go!", "$questsPage", fileConfiguration);
        indexLineCreator(11, "&bp. $bossTrackingPage &8- &6Boss Tracking", "Click to go!", "$bossTrackingPage", fileConfiguration);
        indexLineCreator(12, "", "", "", fileConfiguration);


        statsLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration);
        statsLineCreator(1, "&5&lPlayer Stats:", "", "", fileConfiguration);
        statsLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration);
        statsLineCreator(3, "", "", "", fileConfiguration);
        statsLineCreator(4, "&2Money: &a$money", "Kill Elite Mobs to loot currency or\n" + "sell their drops in /em shop or\n" + "complete quests in /em quest!", "", fileConfiguration);
        statsLineCreator(5, "", "", "", fileConfiguration);
        statsLineCreator(6, "&6Guild Tier: &3$guildtier", "Prestige Tier and Guild Rank:\n" +
                "Guild Rank determines how good your loot can " +
                "be, sets your bonus from the Prestige Tier, among " +
                "other things. The Prestige Tier unlocks extremely " +
                "powerful rewards, like increased max health, chance " +
                "to dodge/crit, increased currency rewards and more! " +
                "You can unlock Guild Ranks and Prestige Tiers at /ag!\n" +
                "⚜ = prestige rank, ✧ = guild rank!", "", fileConfiguration);
        statsLineCreator(7, "&4Elite Kills: &c$kills", "Amount of Elite Mobs killed.", "", fileConfiguration);
        statsLineCreator(8, "&4Max Lvl Killed: &c$highestkill", "Level of the highest Elite Mob killed.\n" +
                "Elite Mob levels are based on the tier\n" +
                "of your gear! Higher tiers, higher\n" +
                "Elite Mob levels!\n" +
                "Note: only non-exploity kills get counted!", "", fileConfiguration);
        statsLineCreator(9, "&4Elite Deaths: &c$deaths", "Times killed by Elite Mobs.", "", fileConfiguration);
        statsLineCreator(10, "&5Quests Completed: &d$quests", "Amount of EliteMobs quests completed\n" +
                "You can take quests on at /em quest\n", "", fileConfiguration);
        statsLineCreator(11, "", "", "", fileConfiguration);
        statsLineCreator(12, "&bScore: &3$score", "Your EliteMobs score. It goes up\n" +
                "when you kill and elite mob,\n" +
                "and it goes down when you die\n" +
                "to an elite. Higher level\n" +
                "elites give more score.", "", fileConfiguration);

        gearLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration);
        gearLineCreator(1, "&7&lArmor & Weapons:", "", "", fileConfiguration);
        gearLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration);
        gearLineCreator(3, "&8&lGear Tiers:", "", "", fileConfiguration);
        gearLineCreator(4, "          ☠ - $helmettier", "$helmet", "", fileConfiguration);
        gearLineCreator(5, "          ▼ - $chestplatetier", "$chestplate", "", fileConfiguration);
        gearLineCreator(6, "          Π - $leggingstier", "$leggings", "", fileConfiguration);
        gearLineCreator(7, "          ╯╰ - $bootstier", "$boots", "", fileConfiguration);
        gearLineCreator(8, "{⚔ - $mainhandtier}    {⛨ - $offhandtier}", "{$mainhand}{$offhand}", "", fileConfiguration);
        gearLineCreator(9, "", "", "", fileConfiguration);
        gearLineCreator(10, "{dmg : $damage}    {armr: $armor}", "{Base damage dealt to Elite Mobs}{Damage reduction from Elite Mobs}", "", fileConfiguration);
        gearLineCreator(11, "Threat level: $threat",
                "This determines the level of the\n" +
                        "Elite Mobs that spawns near you.\n" +
                        "Takes armor, weapon in hand, guild\n" +
                        "tier into account.\n",
                "", fileConfiguration);
        gearLineCreator(12, "", "", "", fileConfiguration);

        teleportLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration);
        teleportLineCreator(1, "&2&lTeleports", "", "", fileConfiguration);
        teleportLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration);
        teleportLineCreator(3, "Spawn", "Teleport to spawn!", "/em spawntp", fileConfiguration);
        teleportLineCreator(4, PlayerStatusScreen.convertLightColorsToBlack(AdventurersGuildConfig.adventurersGuildMenuName), "Teleport to the Adventurer's Guild Hub!", "/ag", fileConfiguration);

        onTeleportHover = ConfigurationEngine.setString(fileConfiguration, "onTeleportsHover", "Click to teleport!");

        commandsLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration);
        commandsLineCreator(1, "&3&lCommands:", "", "", fileConfiguration);
        commandsLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration);
        commandsLineCreator(3, "", "", "", fileConfiguration);
        commandsLineCreator(4, "&5/ag", "CLICK TO USE\n" +
                "The place where you can find\n" +
                "NPCs that give quests, buy and\n" +
                "sell items, give advice and more!", "/ag", fileConfiguration);
        commandsLineCreator(5, "", "", "", fileConfiguration);
        commandsLineCreator(6, "&5/shareitem", "CLICK TO USE\n" +
                "Shares the item you're holding\n" +
                "on chat!", "/shareitem", fileConfiguration);
        commandsLineCreator(7, "", "", "", fileConfiguration);
        commandsLineCreator(8, "", "", "", fileConfiguration);
        commandsLineCreator(9, "", "", "", fileConfiguration);
        commandsLineCreator(10, "", "", "", fileConfiguration);
        commandsLineCreator(11, "", "", "", fileConfiguration);
        commandsLineCreator(12, "", "", "", fileConfiguration);

        questTrackerLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration);
        questTrackerLineCreator(1, "&6&lQuests:", "Do /em quest to accept quests!", "", fileConfiguration);
        questTrackerLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration);

        onQuestTrackHover = ConfigurationEngine.setString(fileConfiguration, "onQuestTrackHover", "Click to abandon!");

        bossTrackerLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration);
        bossTrackerLineCreator(1, "&4&lBoss Tracker:", "Big bosses get displayed here!", "", fileConfiguration);
        bossTrackerLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration);

        onBossTrackHover = ConfigurationEngine.setString(fileConfiguration, "onBossTrackHover", "Click to track/untrack!");

    }

    private static void indexLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration) {
        indexTextLines[line] = ConfigurationEngine.setString(fileConfiguration, "indexTexts" + line, text);
        indexHoverLines[line] = ConfigurationEngine.setString(fileConfiguration, "indexHovers" + line, hover);
        indexCommandLines[line] = ConfigurationEngine.setString(fileConfiguration, "indexCommands" + line, command);
    }

    private static void statsLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration) {
        statsTextLines[line] = ConfigurationEngine.setString(fileConfiguration, "statsText" + line, text);
        statsHoverLines[line] = ConfigurationEngine.setString(fileConfiguration, "statsHover" + line, hover);
        statsCommandLines[line] = ConfigurationEngine.setString(fileConfiguration, "statsCommand" + line, command);
    }

    private static void gearLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration) {
        gearTextLines[line] = ConfigurationEngine.setString(fileConfiguration, "gearText" + line, text);
        gearHoverLines[line] = ConfigurationEngine.setString(fileConfiguration, "gearHover" + line, hover);
        gearCommandLines[line] = ConfigurationEngine.setString(fileConfiguration, "gearCommand" + line, command);
    }

    private static void teleportLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration) {
        teleportTextLines[line] = ConfigurationEngine.setString(fileConfiguration, "teleportText" + line, text);
        teleportHoverLines[line] = ConfigurationEngine.setString(fileConfiguration, "teleportHover" + line, hover);
        teleportCommandLines[line] = ConfigurationEngine.setString(fileConfiguration, "teleportCommand" + line, command);
    }

    private static void commandsLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration) {
        commandsTextLines[line] = ConfigurationEngine.setString(fileConfiguration, "commandsText" + line, text);
        commandsHoverLines[line] = ConfigurationEngine.setString(fileConfiguration, "commandsHover" + line, hover);
        commandsCommandLines[line] = ConfigurationEngine.setString(fileConfiguration, "commandsCommand" + line, command);
    }

    private static void questTrackerLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration) {
        questTrackerTextLines[line] = ConfigurationEngine.setString(fileConfiguration, "questTrackerText" + line, text);
        questTrackerHoverLines[line] = ConfigurationEngine.setString(fileConfiguration, "questTrackerHover" + line, hover);
        questTrackerCommandLines[line] = ConfigurationEngine.setString(fileConfiguration, "questTrackerCommand" + line, command);
    }

    private static void bossTrackerLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration) {
        bossTrackerTextLines[line] = ConfigurationEngine.setString(fileConfiguration, "bossTrackerText" + line, text);
        bossTrackerHoverLines[line] = ConfigurationEngine.setString(fileConfiguration, "bossTrackerHover" + line, hover);
        bossTrackerCommandLines[line] = ConfigurationEngine.setString(fileConfiguration, "bossTrackerCommand" + line, command);
    }

}
