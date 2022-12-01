package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.VersionChecker;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class PlayerStatusMenuConfig extends MenusConfigFields {
    @Getter
    private static final String[] indexTextLines = new String[13];
    @Getter
    private static final String[] indexHoverLines = new String[13];
    @Getter
    private static final String[] indexCommandLines = new String[13];
    @Getter
    private static final String[] statsTextLines = new String[13];
    @Getter
    private static final String[] statsHoverLines = new String[13];
    @Getter
    private static final String[] statsCommandLines = new String[13];
    @Getter
    private static final String[] gearTextLines = new String[13];
    @Getter
    private static final String[] gearHoverLines = new String[13];
    @Getter
    private static final String[] gearCommandLines = new String[13];
    @Getter
    private static final String[] teleportTextLines = new String[13];
    @Getter
    private static final String[] teleportHoverLines = new String[13];
    @Getter
    private static final String[] teleportCommandLines = new String[13];
    @Getter
    private static final String[] commandsTextLines = new String[13];
    @Getter
    private static final String[] commandsHoverLines = new String[13];
    @Getter
    private static final String[] commandsCommandLines = new String[13];
    @Getter
    private static final String[] bossTrackerTextLines = new String[13];
    @Getter
    private static final String[] bossTrackerHoverLines = new String[13];
    @Getter
    private static final String[] bossTrackerCommandLines = new String[13];
    @Getter
    private static final String[] questTrackerTextLines = new String[13];
    @Getter
    private static final String[] questTrackerHoverLines = new String[13];
    @Getter
    private static final String[] questTrackerCommandLines = new String[13];
    @Getter
    private static boolean doIndexPage;
    @Getter
    private static boolean doStatsPage;
    @Getter
    private static boolean doGearPage;
    @Getter
    private static boolean doTeleportsPage;
    @Getter
    private static boolean doCommandsPage;
    @Getter
    private static boolean doQuestTrackingPage;
    @Getter
    private static boolean doBossTrackingPage;
    @Getter
    private static String teleportChestMenuName;
    @Getter
    private static String bossTrackerChestMenuName;
    @Getter
    private static String onBossTrackHover;
    @Getter
    private static String onQuestTrackHover;
    @Getter
    private static String onTeleportHover;

    @Getter
    private static String indexChestMenuName;
    @Getter
    private static ItemStack backItem;
    @Getter
    private static ItemStack indexHeaderItem;
    @Getter
    private static int indexHeaderSlot;
    @Getter
    private static ItemStack indexStatsItem;
    @Getter
    private static int indexStatsSlot;
    @Getter
    private static ItemStack indexGearItem;
    @Getter
    private static int indexGearSlot;
    @Getter
    private static ItemStack indexTeleportsItem;
    @Getter
    private static int indexTeleportsSlot;
    @Getter
    private static ItemStack indexCommandsItem;
    @Getter
    private static int indexCommandsSlot;
    @Getter
    private static ItemStack indexQuestTrackingItem;
    @Getter
    private static int indexQuestTrackingSlot;
    @Getter
    private static ItemStack indexBossTrackingItem;
    @Getter
    private static int indexBossTrackingSlot;
    @Getter
    private static String gearChestMenuName;
    @Getter
    private static ItemStack gearDamageItem;
    @Getter
    private static int gearDamageSlot;
    @Getter
    private static ItemStack gearArmorItem;
    @Getter
    private static int gearArmorSlot;
    @Getter
    private static ItemStack gearThreatItem;
    @Getter
    private static int gearThreatSlot;
    @Getter
    private static String statsChestMenuName;
    @Getter
    private static ItemStack statsMoneyItem;
    @Getter
    private static int statsMoneySlot;
    @Getter
    private static ItemStack statsGuildTierItem;
    @Getter
    private static int statsGuildTierSlot;
    @Getter
    private static ItemStack statsEliteKillsItem;
    @Getter
    private static int statsEliteKillsSlot;
    @Getter
    private static ItemStack statsMaxEliteLevelKilledItem;
    @Getter
    private static int statsMaxEliteLevelKilledSlot;
    @Getter
    private static ItemStack statsEliteDeathsItem;
    @Getter
    private static int statsEliteDeathsSlot;
    @Getter
    private static ItemStack statsQuestsCompletedItem;
    @Getter
    private static int statsQuestsCompletedSlot;
    @Getter
    private static ItemStack statsScoreItem;
    @Getter
    private static int statsScoreSlot;
    @Getter
    private static String commandsChestMenuName;
    @Getter
    private static ItemStack commandsAGItem;
    @Getter
    private static int commandsAGSlot;
    @Getter
    private static ItemStack commandsShareItemItem;
    @Getter
    private static int commandsShareItemSlot;


    public PlayerStatusMenuConfig() {
        super("player_status_screen", true);
    }

    private static void indexLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
        indexTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "indexTexts" + line, text, true);
        indexHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "indexHovers" + line, hover, true);
        indexCommandLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "indexCommands" + line, command, false);
    }

    private static void statsLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
        statsTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "statsText" + line, text, true);
        statsHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "statsHover" + line, hover, true);
        statsCommandLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "statsCommand" + line, command, false);
    }

    private static void gearLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
        gearTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "gearText" + line, text, true);
        gearHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "gearHover" + line, hover, true);
        gearCommandLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "gearCommand" + line, command, false);
    }

    private static void teleportLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
        teleportTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "teleportText" + line, text, true);
        teleportHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "teleportHover" + line, hover, true);
        teleportCommandLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "teleportCommand" + line, command, false);
    }

    private static void commandsLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
        commandsTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "commandsText" + line, text, true);
        commandsHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "commandsHover" + line, hover, true);
        commandsCommandLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "commandsCommand" + line, command, false);
    }

    private static void questTrackerLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
        questTrackerTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "questTrackerText" + line, text, true);
        questTrackerHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "questTrackerHover" + line, hover, true);
        questTrackerCommandLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "questTrackerCommand" + line, command, false);
    }

    private static void bossTrackerLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
        bossTrackerTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "bossTrackerText" + line, text, true);
        bossTrackerHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "bossTrackerHover" + line, hover, true);
        bossTrackerCommandLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "bossTrackerCommand" + line, command, false);
    }

    @Override
    public void processAdditionalFields() {

        doIndexPage = ConfigurationEngine.setBoolean(fileConfiguration, "doIndexPage", true);
        doStatsPage = ConfigurationEngine.setBoolean(fileConfiguration, "doStatsPage", true);
        doGearPage = ConfigurationEngine.setBoolean(fileConfiguration, "doGearPage", true);
        doTeleportsPage = ConfigurationEngine.setBoolean(fileConfiguration, "doTeleportsPage", true);
        doCommandsPage = ConfigurationEngine.setBoolean(fileConfiguration, "doCommandsPage", true);
        doQuestTrackingPage = ConfigurationEngine.setBoolean(fileConfiguration, "doQuestTrackingPage", true);
        doBossTrackingPage = ConfigurationEngine.setBoolean(fileConfiguration, "doBossTrackingPage", true);

        indexLineCreator(0, "&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        indexLineCreator(1, "&5&l/ag &7- &6EliteMobs Hub",
                "CLICK TO USE\n" +
                        "The place where you can find\n" +
                        "NPCs that give quests, buy and\n" +
                        "sell items, give advice and more!",
                "/ag", fileConfiguration, file);
        indexLineCreator(2, "&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        indexLineCreator(3, "", "", "", fileConfiguration, file);
        indexLineCreator(4, "&6&lIndex", "", "", fileConfiguration, file);
        indexLineCreator(5, "", "", "", fileConfiguration, file);
        indexLineCreator(6, "&bp. $statsPage &8- &6Stats", "Click to go!", "$statsPage", fileConfiguration, file);
        indexLineCreator(7, "&bp. $gearPage &8- &6Gear", "Click to go!", "$gearPage", fileConfiguration, file);
        indexLineCreator(8, "&bp. $teleportsPage &8- &6Teleports", "Click to go!", "$teleportsPage", fileConfiguration, file);
        indexLineCreator(9, "&bp. $commandsPage &8- &6Commands", "Click to go!", "$commandsPage", fileConfiguration, file);
        indexLineCreator(10, "&bp. $questsPage &8- &6Quest Tracking", "Click to go!", "$questsPage", fileConfiguration, file);
        indexLineCreator(11, "&bp. $bossTrackingPage &8- &6Boss Tracking", "Click to go!", "$bossTrackingPage", fileConfiguration, file);
        indexLineCreator(12, "", "", "", fileConfiguration, file);


        statsLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        statsLineCreator(1, "&5&lPlayer Stats:", "", "", fileConfiguration, file);
        statsLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        statsLineCreator(3, "", "", "", fileConfiguration, file);
        statsLineCreator(4, "&2Money: &a$money", "Kill Elite Mobs to loot currency or\n" +
                "sell their drops in /em shop or\n" +
                "complete quests!", "", fileConfiguration, file);
        statsLineCreator(5, "", "", "", fileConfiguration, file);
        statsLineCreator(6, "&6Guild Tier: &3$guildtier", "Prestige Tier and Guild Rank:\n" +
                "Guild Rank determines how good your loot can " +
                "be, sets your bonus from the Prestige Tier, among " +
                "other things. The Prestige Tier unlocks extremely " +
                "powerful rewards, like increased max health, chance " +
                "to dodge/crit, increased currency rewards and more! " +
                "You can unlock Guild Ranks and Prestige Tiers at /ag!\n" +
                "⚜ = prestige rank, ✧ = guild rank!", "", fileConfiguration, file);
        statsLineCreator(7, "&4Elite Kills: &c$kills", "Amount of Elite Mobs killed.", "", fileConfiguration, file);
        statsLineCreator(8, "&4Max Lvl Killed: &c$highestkill", "Level of the highest Elite Mob killed.\n" +
                "Elite Mob levels are based on the tier\n" +
                "of your gear! Higher tiers, higher\n" +
                "Elite Mob levels!\n" +
                "Note: only non-exploity kills get counted!", "", fileConfiguration, file);
        statsLineCreator(9, "&4Elite Deaths: &c$deaths", "Times killed by Elite Mobs.", "", fileConfiguration, file);
        statsLineCreator(10, "&5Quests Completed: &d$quests", "Amount of EliteMobs quests completed.\n" +
                "You can accept quests by talking to NPCs!", "", fileConfiguration, file);
        statsLineCreator(11, "", "", "", fileConfiguration, file);
        statsLineCreator(12, "&bScore: &3$score", "Your EliteMobs score. It goes up\n" +
                "when you kill and elite mob,\n" +
                "and it goes down when you die\n" +
                "to an elite. Higher level\n" +
                "elites give more score.", "", fileConfiguration, file);

        gearLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        gearLineCreator(1, "&7&lArmor & Weapons:", "", "", fileConfiguration, file);
        gearLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        gearLineCreator(3, "&8&lGear Tiers:", "", "", fileConfiguration, file);
        gearLineCreator(4, "          ☠ - $helmettier", "$helmet", "", fileConfiguration, file);
        gearLineCreator(5, "          ▼ - $chestplatetier", "$chestplate", "", fileConfiguration, file);
        gearLineCreator(6, "          Π - $leggingstier", "$leggings", "", fileConfiguration, file);
        gearLineCreator(7, "          ╯╰ - $bootstier", "$boots", "", fileConfiguration, file);
        gearLineCreator(8, "{⚔ - $mainhandtier}    {⛨ - $offhandtier}", "{$mainhand}{$offhand}", "", fileConfiguration, file);
        gearLineCreator(9, "", "", "", fileConfiguration, file);
        gearLineCreator(10, "{dmg : $damage}    {armr: $armor}", "{Base damage dealt to Elite Mobs}{Damage reduction from Elite Mobs}",
                "", fileConfiguration, file);
        gearLineCreator(11, "Threat level: $threat",
                "This determines the level of the\n" +
                        "Elite Mobs that spawns near you.\n" +
                        "Takes armor, weapon in hand, guild\n" +
                        "tier into account.\n",
                "", fileConfiguration, file);
        gearLineCreator(12, "", "", "", fileConfiguration, file);

        teleportLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        teleportLineCreator(1, "&2&lTeleports", "", "", fileConfiguration, file);
        teleportLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        teleportLineCreator(3, "Spawn", "Teleport to spawn!", "/em spawntp", fileConfiguration, file);
        teleportLineCreator(4, PlayerStatusScreen.convertLightColorsToBlack(AdventurersGuildConfig.getAdventurersGuildMenuName()), "Teleport to the Adventurer's Guild Hub!", "/ag", fileConfiguration, file);

        onTeleportHover = ConfigurationEngine.setString(file, fileConfiguration, "onTeleportsHover", "Click to teleport!", true);

        commandsLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        commandsLineCreator(1, "&3&lCommands:", "", "", fileConfiguration, file);
        commandsLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        commandsLineCreator(3, "", "", "", fileConfiguration, file);
        commandsLineCreator(4, "&5/ag", "CLICK TO USE\n" +
                "The place where you can find\n" +
                "NPCs that give quests, buy and\n" +
                "sell items, give advice and more!", "/ag", fileConfiguration, file);
        commandsLineCreator(5, "", "", "", fileConfiguration, file);
        commandsLineCreator(6, "&5/shareitem", "CLICK TO USE\n" +
                "Shares the item you're holding\n" +
                "on chat!", "/shareitem", fileConfiguration, file);
        commandsLineCreator(7, "", "", "", fileConfiguration, file);
        commandsLineCreator(8, "", "", "", fileConfiguration, file);
        commandsLineCreator(9, "", "", "", fileConfiguration, file);
        commandsLineCreator(10, "", "", "", fileConfiguration, file);
        commandsLineCreator(11, "", "", "", fileConfiguration, file);
        commandsLineCreator(12, "", "", "", fileConfiguration, file);

        questTrackerLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        questTrackerLineCreator(1, "&6&lQuests:", "Talk to NPCs to accept quests!", "", fileConfiguration, file);
        questTrackerLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);

        onQuestTrackHover = ConfigurationEngine.setString(file, fileConfiguration, "onQuestTrackHover", "Click to abandon!", true);

        bossTrackerLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        bossTrackerLineCreator(1, "&4&lBoss Tracker:", "Big bosses get displayed here!", "", fileConfiguration, file);
        bossTrackerLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);

        onBossTrackHover = ConfigurationEngine.setString(file, fileConfiguration, "onBossTrackHover", "Click to track/untrack!", true);

        teleportChestMenuName = ConfigurationEngine.setString(file, fileConfiguration, "teleportChestMenuName", "&2EliteMobs Teleports", true);

        bossTrackerChestMenuName = ConfigurationEngine.setString(file, fileConfiguration, "bossTrackerChestMenuName", "&2EliteMobs Boss Tracking", true);

        //inventory-based menus for bedrock
        indexChestMenuName = ConfigurationEngine.setString(file, fileConfiguration, "indexChestMenuName", "&2EliteMobs Index", true);
        indexHeaderItem = ConfigurationEngine.setItemStack(fileConfiguration, "indexHeaderItem",
                ItemStackGenerator.generateItemStack(Material.PAPER,
                        "&5&l/ag &7- &6EliteMobs Hub",
                        Arrays.asList("CLICK TO USE",
                                "The place where you can find",
                                "NPCs that give quests, buy and",
                                "sell items, give advice and more!")));
        indexHeaderSlot = ConfigurationEngine.setInt(fileConfiguration, "indexHeaderSlot", 4);

        indexStatsItem = ConfigurationEngine.setItemStack(fileConfiguration, "indexStatsItem",
                ItemStackGenerator.generateItemStack(Material.MAP,
                        "&6Stats",
                        List.of("Click to go!")));
        indexStatsSlot = ConfigurationEngine.setInt(fileConfiguration, "indexStatsSlot", 10);

        indexGearItem = ConfigurationEngine.setItemStack(fileConfiguration, "indexGearItem",
                ItemStackGenerator.generateItemStack(Material.DIAMOND_SWORD,
                        "&6Gear",
                        List.of("Click to go!")));
        indexGearSlot = ConfigurationEngine.setInt(fileConfiguration, "indexGearSlot", 12);

        indexTeleportsItem = ConfigurationEngine.setItemStack(fileConfiguration, "indexTeleportsItem",
                ItemStackGenerator.generateItemStack(Material.END_PORTAL_FRAME,
                        "&6Teleports",
                        List.of("Click to go!")));
        indexTeleportsSlot = ConfigurationEngine.setInt(fileConfiguration, "indexTeleportsSlot", 14);

        indexCommandsItem = ConfigurationEngine.setItemStack(fileConfiguration, "indexCommandsItem",
                ItemStackGenerator.generateItemStack(Material.JUKEBOX,
                        "&6Commands",
                        List.of("Click to go!")));
        indexCommandsSlot = ConfigurationEngine.setInt(fileConfiguration, "indexCommandsSlot", 16);


        indexQuestTrackingItem = ConfigurationEngine.setItemStack(fileConfiguration, "indexQuestTrackingItem",
                ItemStackGenerator.generateItemStack(Material.WRITABLE_BOOK,
                        "&6Quest Tracking",
                        List.of("Click to go!")));
        indexQuestTrackingSlot = ConfigurationEngine.setInt(fileConfiguration, "indexQuestTrackingSlot", 20);

        if (!VersionChecker.serverVersionOlderThan(16, 0))
            indexBossTrackingItem = ConfigurationEngine.setItemStack(fileConfiguration, "indexBossTrackingItem",
                    ItemStackGenerator.generateItemStack(Material.TARGET,
                            "&6Boss Tracking",
                            List.of("Click to go!")));
        else
            indexBossTrackingItem = ConfigurationEngine.setItemStack(fileConfiguration, "indexBossTrackingItem",
                    ItemStackGenerator.generateItemStack(Material.DIAMOND,
                            "&6Boss Tracking",
                            List.of("Click to go!")));
        indexBossTrackingSlot = ConfigurationEngine.setInt(fileConfiguration, "indexBossTrackingSlot", 24);


        backItem = ConfigurationEngine.setItemStack(fileConfiguration, "backItem",
                ItemStackGenerator.generateItemStack(Material.BARRIER, "&cBack"));

        gearChestMenuName = ConfigurationEngine.setString(file, fileConfiguration, "gearChestMenuName", "&2EliteMobs Gear", true);
        gearDamageItem = ConfigurationEngine.setItemStack(fileConfiguration, "gearDamageItem",
                ItemStackGenerator.generateItemStack(Material.DIAMOND_SWORD,
                        "&4Damage: $damage",
                        Arrays.asList("&fBase damage dealt to Elites.",
                                "&fBased on the level of your weapon!")));
        gearDamageSlot = ConfigurationEngine.setInt(fileConfiguration, "gearDamageSlot", 23);
        gearArmorItem = ConfigurationEngine.setItemStack(fileConfiguration, "gearArmorItem",
                ItemStackGenerator.generateItemStack(Material.SHIELD,
                        "&2Defense: $defense",
                        Arrays.asList("&fBase damage reduction from Elites.",
                                "&fBased on the average level of your armor!")));
        gearArmorSlot = ConfigurationEngine.setInt(fileConfiguration, "gearArmorSlot", 24);
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            gearThreatItem = ConfigurationEngine.setItemStack(fileConfiguration, "gearThreatItem",
                    ItemStackGenerator.generateItemStack(Material.TARGET,
                            "&cThreat Level: $threat",
                            Arrays.asList("&fThis determines the level of the",
                                    "&fElite Mobs that spawns near you",
                                    "&fTakes armor, weapon in hand, guild",
                                    "&ftier into account.")));
        else
            gearThreatItem = ConfigurationEngine.setItemStack(fileConfiguration, "gearThreatItem",
                    ItemStackGenerator.generateItemStack(Material.DIAMOND,
                            "&cThreat Level: $threat",
                            Arrays.asList("&fThis determines the level of the",
                                    "&fElite Mobs that spawns near you",
                                    "&fTakes armor, weapon in hand, guild",
                                    "&ftier into account.")));
        gearThreatSlot = ConfigurationEngine.setInt(fileConfiguration, "gearThreatSlot", 25);

        statsChestMenuName = ConfigurationEngine.setString(file, fileConfiguration, "statsChestMenuName", "&2EliteMobs Stats", true);

        statsMoneyItem = ConfigurationEngine.setItemStack(fileConfiguration, "statsMoneyItem",
                ItemStackGenerator.generateItemStack(Material.GOLD_INGOT,
                        "&2Elite Coins: $money",
                        Arrays.asList("&fKill Elite Mobs to loot currency,",
                                "&fsell their drops in /em shop or",
                                "&fcomplete quests!")));
        statsMoneySlot = ConfigurationEngine.setInt(fileConfiguration, "statsMoneySlot", 10);

        if (!VersionChecker.serverVersionOlderThan(16, 0))
            statsGuildTierItem = ConfigurationEngine.setItemStack(fileConfiguration, "statsGuildTierItem",
                    ItemStackGenerator.generateItemStack(Material.TARGET,
                            "&6Guild Tier: $tier",
                            Arrays.asList("&fGuild Rank determines how good your loot can ",
                                    "&fbe, sets your bonus from the Prestige Tier, among ",
                                    "&fother things. The Prestige Tier unlocks extremely ",
                                    "&fpowerful rewards, like increased max health, chance ",
                                    "&fto dodge/crit, increased currency rewards and more! ",
                                    "&fYou can unlock Guild Ranks and Prestige Tiers at /ag!",
                                    "&f⚜ = prestige rank, ✧ = guild rank!")));
        else
            statsGuildTierItem = ConfigurationEngine.setItemStack(fileConfiguration, "statsGuildTierItem",
                    ItemStackGenerator.generateItemStack(Material.DIAMOND,
                            "&6Guild Tier: $tier",
                            Arrays.asList("&fGuild Rank determines how good your loot can ",
                                    "&fbe, sets your bonus from the Prestige Tier, among ",
                                    "&fother things. The Prestige Tier unlocks extremely ",
                                    "&fpowerful rewards, like increased max health, chance ",
                                    "&fto dodge/crit, increased currency rewards and more! ",
                                    "&fYou can unlock Guild Ranks and Prestige Tiers at /ag!",
                                    "&f⚜ = prestige rank, ✧ = guild rank!")));
        statsGuildTierSlot = ConfigurationEngine.setInt(fileConfiguration, "statsGuildTierSlot", 11);

        statsEliteKillsItem = ConfigurationEngine.setItemStack(fileConfiguration, "statsEliteKillsItem",
                ItemStackGenerator.generateItemStack(Material.DIAMOND_SWORD,
                        "&4Elite Kils: &c$kills",
                        List.of("&fAmount of EliteMobs killed.")));
        statsEliteKillsSlot = ConfigurationEngine.setInt(fileConfiguration, "statsEliteKillsSlot", 12);

        statsMaxEliteLevelKilledItem = ConfigurationEngine.setItemStack(fileConfiguration, "statsMaxEliteLevelKilledItem",
                ItemStackGenerator.generateItemStack(Material.GOLDEN_SWORD,
                        "&4Max Lvl Killed: &c$maxKill",
                        Arrays.asList("&fElite Mob levels are based on the tier",
                                "&fof your gear! Higher tiers, higher",
                                "&fElite Mob levels!\n",
                                "&eNote: only non-exploity kills get counted!")));
        statsMaxEliteLevelKilledSlot = ConfigurationEngine.setInt(fileConfiguration, "statsMaxEliteLevelKilledSlot", 13);

        statsEliteDeathsItem = ConfigurationEngine.setItemStack(fileConfiguration, "statsEliteDeathsItem",
                ItemStackGenerator.generateItemStack(Material.TOTEM_OF_UNDYING,
                        "&4Elite Deaths: $deaths",
                        List.of("&fTimes killed by Elite Mobs.")));
        statsEliteDeathsSlot = ConfigurationEngine.setInt(fileConfiguration, "statsEliteDeathsSlot", 14);

        statsQuestsCompletedItem = ConfigurationEngine.setItemStack(fileConfiguration, "statsQuestsCompletedItem",
                ItemStackGenerator.generateItemStack(Material.LECTERN,
                        "&5Quests Completed: &d$questsCompleted",
                        Arrays.asList("&fAmount of EliteMobs quests completed.",
                                "&fYou can accept quests by talking to NPCs!")));
        statsQuestsCompletedSlot = ConfigurationEngine.setInt(fileConfiguration, "statsQuestsCompletedSlot", 15);

        statsScoreItem = ConfigurationEngine.setItemStack(fileConfiguration, "statsScoreItem",
                ItemStackGenerator.generateItemStack(Material.ITEM_FRAME,
                        "&3Score: &b$score",
                        Arrays.asList("&fYour EliteMobs score. It goes up",
                                "&fwhen you kill and elite mob,",
                                "&fand it goes down when you die",
                                "&fto an elite. Higher level",
                                "&felites give more score.")));
        statsScoreSlot = ConfigurationEngine.setInt(fileConfiguration, "statsScoreSlot", 16);

        commandsChestMenuName = ConfigurationEngine.setString(file, fileConfiguration, "commandsChestMenuName", "&2EliteMobs Commands", true);

        commandsAGItem = ConfigurationEngine.setItemStack(fileConfiguration, "commandsAGItem",
                ItemStackGenerator.generateItemStack(Material.END_PORTAL_FRAME,
                        "&5/ag",
                        Arrays.asList("&fClick to use!",
                                "&fThe place where you can find",
                                "&fNPCs that give quests, buy and",
                                "&fsell items, give advice and more!")));
        commandsAGSlot = ConfigurationEngine.setInt(fileConfiguration, "commandsAGSlot", 11);

        commandsShareItemItem = ConfigurationEngine.setItemStack(fileConfiguration, "commandsShareItemItem",
                ItemStackGenerator.generateItemStack(Material.PAPER,
                        "&5/shareitem",
                        Arrays.asList("&fClick to use!",
                                "&fShares the Elite Item you're holding",
                                "&fon chat!")));
        commandsShareItemSlot = ConfigurationEngine.setInt(fileConfiguration, "commandsShareItemSlot", 15);
    }

}
