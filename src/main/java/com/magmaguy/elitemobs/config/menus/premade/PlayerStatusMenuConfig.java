package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayerStatusMenuConfig extends MenusConfigFields {
    private static final String ROOT_TITLE = "<g:#8B0000:#CC4400:#DAA520>EliteMobs</g> &7Menu";
    private static final String STATS_TITLE = "<g:#B8860B:#F0C040>★ Adventure Record</g>";
    private static final String GEAR_TITLE = "<g:#A63D2F:#E97932>⚔ Combat Gear</g>";
    private static final String TELEPORTS_TITLE = "<g:#355CA8:#5FA9E8>↔ Teleports</g>";
    private static final String COMMANDS_TITLE = "<g:#267A78:#58B8A9>⚡ Quick Actions</g>";
    private static final String QUESTS_TITLE = "<g:#2E7D4F:#69C56F>✉ Quests</g>";
    private static final String BOSS_TITLE = "<g:#7A1F2B:#C2414A>☠ Boss Tracker</g>";
    private static final String SKILLS_TITLE = "<g:#6D3AA8:#A855F7>⚗ Skills</g>";
    private static final String PARTY_TITLE = "<g:#A04468:#E07A9A>♥ Party</g>";

    @Getter
    private static final String[] indexTextLines = new String[14];
    @Getter
    private static final String[] indexHoverLines = new String[14];
    @Getter
    private static final String[] indexCommandLines = new String[14];
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
    private static List<String> landingOverviewLines;
    @Getter
    private static List<String> dialogGearSummaryLines;
    @Getter
    private static String gearUnarmedLabel;
    @Getter
    private static String gearNoSelectedPerksLabel;
    @Getter
    private static String dialogGearHelmetLabel;
    @Getter
    private static String dialogGearChestplateLabel;
    @Getter
    private static String dialogGearLeggingsLabel;
    @Getter
    private static String dialogGearBootsLabel;
    @Getter
    private static String dialogGearMainHandLabel;
    @Getter
    private static String dialogGearOffHandLabel;
    @Getter
    private static String dialogQuestFallbackFormat;
    @Getter
    private static String teleportChestMenuName;
    @Getter
    private static ItemStack teleportSpawnItem;
    @Getter
    private static int teleportSpawnSlot;
    @Getter
    private static ItemStack teleportGuildItem;
    @Getter
    private static int teleportGuildSlot;
    @Getter
    private static String bossTrackerChestMenuName;
    @Getter
    private static String onBossTrackHover;
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
    private static ItemStack indexSkillsItem;
    @Getter
    private static int indexSkillsSlot;
    @Getter
    private static ItemStack indexPartyItem;
    @Getter
    private static int indexPartySlot;
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
    private static String statsRankUnavailableText;
    @Getter
    private static String statsRankTotalUnavailableText;
    @Getter
    private static ItemStack statsRankItem;
    @Getter
    private static int statsRankSlot;
    @Getter
    private static ItemStack statsDungeonsCompletedItem;
    @Getter
    private static int statsDungeonsCompletedSlot;
    @Getter
    private static ItemStack statsCombatLevelItem;
    @Getter
    private static int statsCombatLevelSlot;
    @Getter
    private static ItemStack statsEliteKillsItem;
    @Getter
    private static int statsEliteKillsSlot;
    @Getter
    private static ItemStack statsMaxEliteLevelKilledItem;
    @Getter
    private static int statsMaxEliteLevelKilledSlot;
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
    private static ItemStack commandsSpawnItem;
    @Getter
    private static int commandsSpawnSlot;
    @Getter
    private static ItemStack commandsShareItemItem;
    @Getter
    private static int commandsShareItemSlot;
    @Getter
    private static String skillsItemDisplayName;
    @Getter
    private static String skillsItemLore1;
    @Getter
    private static String skillsItemLore2;
    @Getter
    private static String skillsItemClickLore;
    @Getter
    private static String skillsPageHeader;
    @Getter
    private static String skillsPageLevelFormat;
    @Getter
    private static String skillsPageXpFormat;
    @Getter
    private static String skillItemDisplayNameFormat;
    @Getter
    private static String skillItemSelectLore1;
    @Getter
    private static String skillItemSelectLore2;

    // Dialog titles
    @Getter
    private static String dialogTitlePlayerStatus;
    @Getter
    private static String dialogTitleStats;
    @Getter
    private static String dialogTitleGear;
    @Getter
    private static String dialogTitleTeleports;
    @Getter
    private static String dialogTitleCommands;
    @Getter
    private static String dialogTitleQuests;
    @Getter
    private static String dialogTitleBossTracking;
    @Getter
    private static String dialogTitleSkills;
    @Getter
    private static String dialogTitleParty;
    @Getter
    private static String dialogPartyDescription;
    @Getter
    private static String dialogPartyCreateButton;
    @Getter
    private static String dialogPartyInviteButton;
    @Getter
    private static String dialogPartyLeaveButton;
    @Getter
    private static String dialogPartyInviteTitle;
    @Getter
    private static String dialogPartyInvitePlayerButton;
    @Getter
    private static String dialogPartyInvitePlayerTooltip;
    @Getter
    private static String dialogPartyInviteNoPlayers;
    @Getter
    private static String dialogNoActiveQuests;
    @Getter
    private static String dialogNoTrackableBosses;
    @Getter
    private static String dialogActiveQuestsFormat;
    @Getter
    private static String dialogBackButton;


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
    }

    private static void teleportLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
        teleportTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "teleportTextV2" + line, text, true);
        teleportHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "teleportHoverV2" + line, hover, true);
        teleportCommandLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "teleportCommandV2" + line, command, false);
    }

    private static void commandsLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
        commandsTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "commandsText" + line, text, true);
        commandsHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "commandsHover" + line, hover, true);
        commandsCommandLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "commandsCommand" + line, command, false);
    }

    private static void bossTrackerLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
        bossTrackerTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "bossTrackerText" + line, text, true);
        bossTrackerHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "bossTrackerHover" + line, hover, true);
        bossTrackerCommandLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "bossTrackerCommand" + line, command, false);
    }

    private void migrateLegacyQuickActionDefaults() {
        migrateLegacyStatsDefaults();
        migrateExactString("indexTexts9", "&bp. $commandsPage &8- &6Commands",
                "&bp. $commandsPage &8- &6Quick Actions");
        migrateExactString("indexHovers9", "Click to go!", "Open useful EliteMobs actions");
        migrateExactString("commandsText1", "&3&lCommands:", "&3&lQuick Actions:");
        migrateExactString("commandsText4", "&5/ag", "&5/ag &7- EliteMobs Hub");
        migrateLegacySpawnAction();
        migrateExactString("commandsText6", "&5/shareitem", "&5/em shareitem &7- Share Item");
        migrateExactString("commandsCommand6", "/shareitem", "/em shareitem");
        migrateExactString("commandsChestMenuName", "&2EliteMobs Commands", "&2EliteMobs Quick Actions");
        migrateExactColorString("commandsShareItemItem.name", "&5/shareitem", "&5/em shareitem");
        migrateExactString("dialog.titleCommands", "Commands", "&6Quick Actions");
        migrateExactString("gearHover11",
                "This determines the level of the\nElite Mobs that spawns near you.\nTakes armor, weapon in hand, guild\ntier into account.\n",
                "This is your combat level and determines the\nlevel of Elite Mobs that spawn near you.\n" +
                        "It averages your armor skill and your\ntwo highest weapon skills.\n");

        boolean missingLegacyCommandsLore = !fileConfiguration.isSet("indexCommandsItem.lore");
        if (isExactString("indexCommandsItem.material", Material.JUKEBOX.name()) &&
                isExactColorString("indexCommandsItem.name", "&6Commands") &&
                (missingLegacyCommandsLore ||
                        isExactColorList("indexCommandsItem.lore", List.of("Click to go!")))) {
            fileConfiguration.set("indexCommandsItem.name", "&6&lQuick Actions");
            fileConfiguration.set("indexCommandsItem.lore",
                    List.of("&7Open the Hub, teleport to spawn", "&7or share your held Elite item.", "", "&aClick to view!"));
        }

        if (isExactString("gearThreatItem.material", Material.TARGET.name()) &&
                isExactColorString("gearThreatItem.name", "&cThreat Level: $threat") &&
                isExactColorList("gearThreatItem.lore", List.of(
                        "&fThis determines the level of the",
                        "&fElite Mobs that spawns near you",
                        "&fTakes armor, weapon in hand, guild",
                        "&ftier into account.")))
            fileConfiguration.set("gearThreatItem.lore", List.of(
                    "&fThis is your combat level and determines",
                    "&fthe level of Elite Mobs spawning nearby.",
                    "&fIt averages your armor skill and your",
                    "&ftwo highest weapon skills."));

        if (isExactString("gearDamageItem.material", Material.DIAMOND_SWORD.name()) &&
                isExactColorString("gearDamageItem.name", "&4Damage: $damage") &&
                isExactColorList("gearDamageItem.lore", List.of(
                        "&fBase damage dealt to Elites.",
                        "&fBased on the level of your weapon!"))) {
            fileConfiguration.set("gearDamageItem.name", "&c&lWeapon Power: &fLv. $weaponLevel");
            fileConfiguration.set("gearDamageItem.lore", List.of(
                    "&7$weaponSkill skill: &fLv. $weaponSkillLevel",
                    "&7Equal-level offense: &f$offenseMatch%",
                    "",
                    "&8Equipment contribution only; active skills",
                    "&8and temporary effects apply during combat."));
        }
        if (isExactString("gearArmorItem.material", Material.SHIELD.name()) &&
                isExactColorString("gearArmorItem.name", "&2Defense: $defense") &&
                isExactColorList("gearArmorItem.lore", List.of(
                        "&fBase damage reduction from Elites.",
                        "&fBased on the average level of your armor!"))) {
            fileConfiguration.set("gearArmorItem.name", "&b&lArmor Rating: &f$armorLevel");
            fileConfiguration.set("gearArmorItem.lore", List.of(
                    "&7Armor skill: &fLv. $armorSkillLevel",
                    "&7Equal-level reduction: &f$defenseMatch%",
                    "",
                    "&8Includes equipped armor and relevant",
                    "&8protection enchantments."));
        }
        if (isExactString("gearThreatItem.material", Material.TARGET.name()) &&
                isExactColorString("gearThreatItem.name", "&cThreat Level: $threat") &&
                isExactColorList("gearThreatItem.lore", List.of(
                        "&fThis is your combat level and determines",
                        "&fthe level of Elite Mobs spawning nearby.",
                        "&fIt averages your armor skill and your",
                        "&ftwo highest weapon skills."))) {
            fileConfiguration.set("gearThreatItem.name", "&e&lCombat Level: &f$combatLevel");
            fileConfiguration.set("gearThreatItem.lore", List.of(
                    "&7Average of your armor skill and your",
                    "&7two strongest weapon skills.",
                    "",
                    "&8Nearby natural Elites scale from this level."));
        }
    }

    /**
     * Updates only defaults introduced during this unreleased menu redesign. Existing customized or translated
     * values deliberately do not match these exact comparisons and are therefore left alone.
     */
    private void migrateWipVisualDefaults() {
        migrateExactColorString("dialog.titlePlayerStatus", "Player Status Menu", ROOT_TITLE);
        migrateExactColorString("dialog.titleStats", "&6Adventure Record", STATS_TITLE);
        migrateExactColorString("dialog.titleGear", "Gear", GEAR_TITLE);
        migrateExactColorString("dialog.titleTeleports", "Teleports", TELEPORTS_TITLE);
        migrateExactColorString("dialog.titleCommands", "&6Quick Actions", COMMANDS_TITLE);
        migrateExactColorString("dialog.titleQuests", "Quests", QUESTS_TITLE);
        migrateExactColorString("dialog.titleBossTracking", "Boss Tracking", BOSS_TITLE);
        migrateExactColorString("dialog.titleSkills", "Skills", SKILLS_TITLE);
        migrateExactColorString("dialog.titleParty", "&6Party", PARTY_TITLE);
        migrateExactColorString("indexChestMenuName", "&2EliteMobs Index", ROOT_TITLE);
        migrateExactColorString("statsChestMenuName", "&6Adventure Record", STATS_TITLE);
        migrateExactColorString("gearChestMenuName", "&2EliteMobs Gear", GEAR_TITLE);
        migrateExactColorString("teleportChestMenuName", "&2EliteMobs Teleports", TELEPORTS_TITLE);
        migrateExactColorString("commandsChestMenuName", "&2EliteMobs Quick Actions", COMMANDS_TITLE);
        migrateExactColorString("bossTrackerChestMenuName", "&2EliteMobs Boss Tracking", BOSS_TITLE);
        migrateExactColorString("dialog.partyInviteTitle", "&6Invite a Player",
                "<g:#A04468:#E07A9A>✉ Invite a Player</g>");
        migrateExactColorString("dialog.partyCreateButton", "&aCreate a Party",
                "<g:#2E7D4F:#69C56F>+ Create a Party</g>");
        migrateExactColorString("dialog.partyInviteButton", "&aInvite a Player",
                "<g:#267A78:#58B8A9>✉ Invite a Player</g>");
        migrateExactColorString("dialog.partyLeaveButton", "&cLeave Party",
                "<g:#7A1F2B:#C2414A>✘ Leave Party</g>");
        migrateExactColorString("dialog.partyInvitePlayerButton", "&aInvite $player",
                "<g:#267A78:#58B8A9>✉ Invite</g> &f$player");

        migrateExactColorList("landingOverviewLines", List.of(
                        "&2Coins: &a$money &8| &cThreat: &f$threat",
                        "&dActive Quests: &f$activeQuests &8| &bScore: &f$score"),
                List.of(
                        "<g:#B8860B:#F0C040>Coins</g>: &f$money &8• <g:#A63D2F:#E97932>Combat Level</g>: &f$combatLevel",
                        "<g:#2E7D4F:#69C56F>Active Quests</g>: &f$activeQuests &8• <g:#355CA8:#5FA9E8>Score</g>: &f$score"));
        migrateExactColorList("dialog.gearSummaryLines", List.of(
                        "&6&lCombat Profile",
                        "&eCombat Level: &f$combatLevel",
                        "&cWeapon Power: &fLv. $weaponLevel &8| &c$weaponSkill: &fLv. $weaponSkillLevel",
                        "&cEqual-level offense: &f$offenseMatch%",
                        "&bArmor Rating: &f$armorLevel &8| &bArmor Skill: &fLv. $armorSkillLevel",
                        "&bEqual-level reduction: &f$defenseMatch%",
                        "&8Equipment matchup only; skills and temporary effects apply in combat."),
                List.of(
                        "<g:#A63D2F:#E97932>⚔ Equipment Matchup</g>",
                        "<g:#B8860B:#F0C040>Combat Level</g>: &f$combatLevel",
                        "<g:#A63D2F:#E97932>Held Weapon</g>: &fLv. $weaponLevel &8• &7$weaponSkill: &fLv. $weaponSkillLevel",
                        "<g:#A63D2F:#E97932>Weapon factor vs Lv.</g> &f$referenceLevel: &f×$weaponFactor",
                        "<g:#A04468:#E07A9A>Critical chance</g>: &f$critChance% &8• &7Equipment bonus: &f+$enchantmentBonus%",
                        "&7Selected weapon perks: &f$weaponPerks",
                        "<g:#355CA8:#5FA9E8>Melee armor</g>: &f$armorLevel &8• &7Armor Skill: &fLv. $armorSkillLevel",
                        "<g:#355CA8:#5FA9E8>Melee reduction vs Lv.</g> &f$referenceLevel: &f$defenseMatch%",
                        "<g:#2E7D4F:#69C56F>Health</g>: &f$health&7/&f$maxHealth",
                        "&7Selected armor perks: &f$armorPerks",
                        "<g:#B8860B:#F0C040>Threat generated</g>: &f×$threatMultiplier",
                        "&8Equipment matchup only; skills and temporary effects apply in combat."));

        migrateExactColorString("indexTexts6", "&bp. $statsPage &8- &6Stats",
                "&bp. $statsPage &8- " + STATS_TITLE);
        migrateExactColorString("indexTexts7", "&bp. $gearPage &8- &6Gear",
                "&bp. $gearPage &8- " + GEAR_TITLE);
        migrateExactColorString("indexTexts8", "&bp. $teleportsPage &8- &6Teleports",
                "&bp. $teleportsPage &8- " + TELEPORTS_TITLE);
        migrateExactColorString("indexTexts9", "&bp. $commandsPage &8- &6Quick Actions",
                "&bp. $commandsPage &8- " + COMMANDS_TITLE);
        migrateExactColorString("indexTexts10", "&bp. $questsPage &8- &6Quest Tracking",
                "&bp. $questsPage &8- " + QUESTS_TITLE);
        migrateExactColorString("indexTexts11", "&bp. $bossTrackingPage &8- &6Boss Tracking",
                "&bp. $bossTrackingPage &8- " + BOSS_TITLE);
        migrateExactColorString("indexTexts12", "&bp. $skillsPage &8- &6Skills",
                "&bp. $skillsPage &8- " + SKILLS_TITLE);
        migrateExactColorString("indexTexts13", "&6Party Controls", PARTY_TITLE);

        migrateExactColorString("indexStatsItem.name", "&6Stats", STATS_TITLE);
        migrateExactColorString("indexGearItem.name", "&6Gear", GEAR_TITLE);
        migrateExactColorString("indexTeleportsItem.name", "&6Teleports", TELEPORTS_TITLE);
        migrateExactColorString("indexCommandsItem.name", "&6&lQuick Actions", COMMANDS_TITLE);
        migrateExactColorString("indexQuestTrackingItem.name", "&6Quest Tracking", QUESTS_TITLE);
        migrateExactColorString("indexBossTrackingItem.name", "&6Boss Tracking", BOSS_TITLE);
        migrateExactColorString("indexSkillsItem.name", "&5&lSkills", SKILLS_TITLE);
        migrateExactColorString("indexPartyItem.name", "&6&lParty", PARTY_TITLE);

        migrateExactColorString("gearDamageItem.name", "&c&lWeapon Power: &fLv. $weaponLevel",
                "<g:#A63D2F:#E97932>⚔ Held Weapon</g>: &fLv. $weaponLevel");
        migrateExactColorString("gearArmorItem.name", "&b&lArmor Rating: &f$armorLevel",
                "<g:#355CA8:#5FA9E8>⛨ Armor Rating</g>: &f$armorLevel");
        migrateExactColorString("gearThreatItem.name", "&e&lCombat Level: &f$combatLevel",
                "<g:#B8860B:#F0C040>★ Combat Level</g>: &f$combatLevel");
        migrateExactColorList("gearDamageItem.lore", List.of(
                        "&7$weaponSkill skill: &fLv. $weaponSkillLevel",
                        "&7Equal-level offense: &f$offenseMatch%", "",
                        "&8Equipment contribution only; active skills",
                        "&8and temporary effects apply during combat."),
                List.of(
                        "&7$weaponSkill skill: &fLv. $weaponSkillLevel",
                        "&7Weapon factor vs Lv. $referenceLevel: &f×$weaponFactor",
                        "&7Critical chance: &f$critChance%",
                        "&7Equipment damage bonus: &f+$enchantmentBonus%", "",
                        "&7Selected weapon perks:", "&f$weaponPerks"));
        migrateExactColorList("gearArmorItem.lore", List.of(
                        "&7Armor skill: &fLv. $armorSkillLevel",
                        "&7Equal-level reduction: &f$defenseMatch%", "",
                        "&8Includes equipped armor and relevant",
                        "&8protection enchantments."),
                List.of(
                        "&7Armor skill: &fLv. $armorSkillLevel",
                        "&7Melee reduction vs Lv. $referenceLevel: &f$defenseMatch%",
                        "&7Health: &f$health&7/&f$maxHealth", "",
                        "&7Selected armor perks:", "&f$armorPerks"));
        migrateExactColorList("gearThreatItem.lore", List.of(
                        "&7Average of your armor skill and your",
                        "&7two strongest weapon skills.", "",
                        "&8Nearby natural Elites scale from this level."),
                List.of(
                        "&7Progression baseline: &fLv. $combatLevel",
                        "&7Threat generated: &f×$threatMultiplier", "",
                        "&8Natural scaling may also use distance,",
                        "&8nearby players, party size and randomness."));

        migrateExactColorString("gearText10", "{dmg : $damage}    {armr: $armor}",
                "Combat Lv. $combatLevel | Crit $critChance%");
        migrateExactString("gearHover10", "{Base damage dealt to Elite Mobs}{Damage reduction from Elite Mobs}",
                "Weapon: $weaponSkill Lv. $weaponSkillLevel\nGear Lv. $weaponLevel vs Elite Lv. $referenceLevel");
        migrateExactColorString("gearText11", "Threat level: $threat",
                "Weapon ×$weaponFactor | Melee armor $defenseMatch%");
        migrateExactString("gearHover11",
                "This is your combat level and determines the\nlevel of Elite Mobs that spawn near you.\n" +
                        "It averages your armor skill and your\ntwo highest weapon skills.\n",
                "Melee armor rating: $armorLevel\nArmor skill: Lv. $armorSkillLevel\nEquipment damage bonus: +$enchantmentBonus%");
        migrateExactColorString("gearText12", "", "Threat ×$threatMultiplier | HP $health/$maxHealth");
        migrateExactString("gearHover12", "",
                "Selected weapon perks: $weaponPerks\nSelected armor perks: $armorPerks");

        migrateExactColorString("statsText1", "&6&lAdventure Record", STATS_TITLE);
        migrateExactColorString("statsText4", "&6Server Rank: &f$rank &8/ &7$ranktotal",
                "<g:#B8860B:#F0C040>★ Server Rank</g>: &f$rank &8/ &7$ranktotal");
        migrateExactColorString("statsText5", "&9Adventure Score: &f$score",
                "<g:#355CA8:#5FA9E8>Adventure Score</g>: &f$score");
        migrateExactColorString("statsText6", "&dDungeons Cleared: &f$dungeons",
                "<g:#6D3AA8:#A855F7>Dungeons Cleared</g>: &f$dungeons");
        migrateExactColorString("statsText7", "&cCombat Level: &f$combat",
                "<g:#A63D2F:#E97932>Combat Level</g>: &f$combat");
        migrateExactColorString("statsText9", "&aElite Kills: &f$kills",
                "<g:#2E7D4F:#69C56F>Elite Kills</g>: &f$kills");
        migrateExactColorString("statsText10", "&bHighest Elite Level: &f$highestkill",
                "<g:#267A78:#58B8A9>Highest Elite Level</g>: &f$highestkill");
        migrateExactColorString("statsText11", "&eQuests Completed: &f$quests",
                "<g:#B8860B:#F0C040>Quests Completed</g>: &f$quests");

        migrateExactColorString("statsRankItem.name", "&6&lServer Rank &8• &f$rank &7/ $ranktotal",
                "<g:#B8860B:#F0C040>★ Server Rank</g> &8• &f$rank &7/ $ranktotal");
        migrateExactColorString("statsDungeonsCompletedItem.name", "&d&lDungeons Cleared &8• &f$dungeons",
                "<g:#6D3AA8:#A855F7>Dungeons Cleared</g> &8• &f$dungeons");
        migrateExactColorString("statsCombatLevelItem.name", "&c&lCombat Level &8• &f$combat",
                "<g:#A63D2F:#E97932>Combat Level</g> &8• &f$combat");
        migrateExactColorString("statsEliteKillsItem.name", "&a&lElite Kills &8• &f$kills",
                "<g:#2E7D4F:#69C56F>Elite Kills</g> &8• &f$kills");
        migrateExactColorString("statsMaxEliteLevelKilledItem.name", "&b&lPeak Elite Level &8• &f$maxKill",
                "<g:#267A78:#58B8A9>Peak Elite Level</g> &8• &f$maxKill");
        migrateExactColorString("statsQuestsCompletedItem.name", "&e&lQuests Completed &8• &f$questsCompleted",
                "<g:#B8860B:#F0C040>Quests Completed</g> &8• &f$questsCompleted");
        migrateExactColorString("statsScoreItem.name", "&9&lAdventure Score &8• &f$score",
                "<g:#355CA8:#5FA9E8>Adventure Score</g> &8• &f$score");

        migrateExactColorString("skillsItemDisplayName", "&5&lSkills", SKILLS_TITLE);
        migrateExactColorString("skillsPageHeader", "&5&lYour Skills",
                "<g:#6D3AA8:#A855F7>⚗ Your Skills</g>");
    }

    private void migrateLegacyStatsDefaults() {
        List<String> legacyText = List.of(
                "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                "&5&lPlayer Stats:",
                "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                "",
                "&2Money: &a$money",
                "",
                "&6Guild Tier: &3$guildtier",
                "&4Elite Kills: &c$kills",
                "&4Max Lvl Killed: &c$highestkill",
                "&4Elite Deaths: &c$deaths",
                "&5Quests Completed: &d$quests",
                "",
                "&bScore: &3$score");
        List<String> legacyHover = List.of(
                "", "", "", "",
                "Kill Elite Mobs to loot currency or\nsell their drops in /em shop or\ncomplete quests!",
                "",
                "Prestige Tier and Guild Rank:\nGuild Rank determines how good your loot can be, sets your bonus from the Prestige Tier, among other things. The Prestige Tier unlocks extremely powerful rewards, like increased max health, chance to dodge/crit, increased currency rewards and more! You can unlock Guild Ranks and Prestige Tiers at /ag!\n⚜ = prestige rank, ✧ = guild rank!",
                "Amount of Elite Mobs killed.",
                "Level of the highest Elite Mob killed.\nElite Mob levels are based on the tier\nof your gear! Higher tiers, higher\nElite Mob levels!\nNote: only non-exploity kills get counted!",
                "Times killed by Elite Mobs.",
                "Amount of EliteMobs quests completed.\nYou can accept quests by talking to NPCs!",
                "",
                "Your EliteMobs score. It goes up\nwhen you kill and elite mob,\nand it goes down when you die\nto an elite. Higher level\nelites give more score.");

        boolean exactLegacyText = true;
        for (int i = 0; i < legacyText.size(); i++) {
            if (!isExactColorString("statsText" + i, legacyText.get(i)) ||
                    !isExactString("statsHover" + i, legacyHover.get(i))) {
                exactLegacyText = false;
                break;
            }
        }
        if (exactLegacyText) {
            List<String> text = List.of(
                    "&6&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                    "&6&lAdventure Record",
                    "&6&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                    "",
                    "&6Server Rank: &f$rank &8/ &7$ranktotal",
                    "&9Adventure Score: &f$score",
                    "&dDungeons Cleared: &f$dungeons",
                    "&cCombat Level: &f$combat",
                    "",
                    "&aElite Kills: &f$kills",
                    "&bHighest Elite Level: &f$highestkill",
                    "&eQuests Completed: &f$quests",
                    "");
            List<String> hover = List.of(
                    "", "", "", "",
                    "Your server-wide position based on Adventure Score.",
                    "Higher-level Elite kills award more score. Elite deaths reduce it.",
                    "Successful dungeon clears. Entering or abandoning a run does not count.",
                    "Your overall level from armor and your two strongest weapon skills.",
                    "",
                    "Confirmed non-exploit Elite Mob kills.",
                    "The highest-level Elite Mob you have defeated legitimately.",
                    "EliteMobs quests you have fully completed.",
                    "");
            for (int i = 0; i < text.size(); i++) {
                fileConfiguration.set("statsText" + i, text.get(i));
                fileConfiguration.set("statsHover" + i, hover.get(i));
            }
        }

        migrateExactString("statsChestMenuName", "&2EliteMobs Stats", "&6Adventure Record");
        migrateExactString("dialog.titleStats", "Stats", "&6Adventure Record");
        migrateExactInt("statsEliteKillsSlot", 12, 14);
        migrateExactInt("statsMaxEliteLevelKilledSlot", 13, 15);
        migrateExactInt("statsQuestsCompletedSlot", 15, 16);
        migrateExactInt("statsScoreSlot", 16, 11);

        migrateExactItem("statsEliteKillsItem", Material.DIAMOND_SWORD,
                "&4Elite Kills: &c$kills", List.of("&fAmount of EliteMobs killed."),
                Material.DIAMOND_SWORD, "&a&lElite Kills &8• &f$kills",
                List.of("&7Confirmed non-exploit Elite Mob kills."));
        migrateExactItem("statsMaxEliteLevelKilledItem", Material.GOLDEN_SWORD,
                "&4Max Lvl Killed: &c$maxKill", List.of(
                        "&fElite Mob levels are based on the tier",
                        "&fof your gear! Higher tiers, higher",
                        "&fElite Mob levels!\n",
                        "&eNote: only non-exploity kills get counted!"),
                Material.BEACON, "&b&lPeak Elite Level &8• &f$maxKill",
                List.of("&7Your highest legitimate Elite Mob kill."));
        migrateExactItem("statsQuestsCompletedItem", Material.LECTERN,
                "&5Quests Completed: &d$questsCompleted", List.of(
                        "&fAmount of EliteMobs quests completed.",
                        "&fYou can accept quests by talking to NPCs!"),
                Material.WRITABLE_BOOK, "&e&lQuests Completed &8• &f$questsCompleted",
                List.of("&7EliteMobs quests fully completed."));
        migrateExactItem("statsScoreItem", Material.ITEM_FRAME,
                "&3Score: &b$score", List.of(
                        "&fYour EliteMobs score. It goes up",
                        "&fwhen you kill and elite mob,",
                        "&fand it goes down when you die",
                        "&fto an elite. Higher level",
                        "&felites give more score."),
                Material.EXPERIENCE_BOTTLE, "&9&lAdventure Score &8• &f$score",
                List.of("&7Higher-level Elite kills award more score.",
                        "&7Elite deaths reduce it."));
    }

    private void migrateExactInt(String path, int legacyValue, int replacement) {
        if (fileConfiguration.isSet(path) && fileConfiguration.getInt(path) == legacyValue)
            fileConfiguration.set(path, replacement);
    }

    private void migrateExactItem(String path,
                                  Material legacyMaterial,
                                  String legacyName,
                                  List<String> legacyLore,
                                  Material replacementMaterial,
                                  String replacementName,
                                  List<String> replacementLore) {
        if (!isExactString(path + ".material", legacyMaterial.name()) ||
                !isExactColorString(path + ".name", legacyName) ||
                !isExactColorList(path + ".lore", legacyLore)) return;
        fileConfiguration.set(path + ".material", replacementMaterial.name());
        fileConfiguration.set(path + ".name", replacementName);
        fileConfiguration.set(path + ".lore", replacementLore);
    }

    private void migrateLegacySpawnAction() {
        if (!isExactString("commandsText5", "") ||
                !isExactString("commandsHover5", "") ||
                !isExactString("commandsCommand5", "")) return;
        fileConfiguration.set("commandsText5", "&5/em spawntp &7- Spawn");
        fileConfiguration.set("commandsHover5", "CLICK TO USE\nTeleport to the server spawn!");
        fileConfiguration.set("commandsCommand5", "/em spawntp");
    }

    private void migrateExactString(String path, String legacyValue, String replacement) {
        if (isExactString(path, legacyValue)) fileConfiguration.set(path, replacement);
    }

    private void migrateExactColorString(String path, String legacyValue, String replacement) {
        if (isExactColorString(path, legacyValue)) fileConfiguration.set(path, replacement);
    }

    private boolean isExactString(String path, String value) {
        return fileConfiguration.isSet(path) && value.equals(fileConfiguration.getString(path));
    }

    private boolean isExactColorString(String path, String value) {
        if (!fileConfiguration.isSet(path)) return false;
        String configuredValue = fileConfiguration.getString(path);
        return configuredValue != null && normalizeSectionColorCodes(configuredValue).equals(normalizeSectionColorCodes(value));
    }

    private boolean isExactColorList(String path, List<String> values) {
        if (!fileConfiguration.isSet(path)) return false;
        List<String> configuredValues = fileConfiguration.getStringList(path);
        if (configuredValues.size() != values.size()) return false;
        for (int i = 0; i < values.size(); i++)
            if (!normalizeSectionColorCodes(configuredValues.get(i)).equals(normalizeSectionColorCodes(values.get(i))))
                return false;
        return true;
    }

    private void migrateExactColorList(String path, List<String> legacyValues, List<String> replacementValues) {
        if (isExactColorList(path, legacyValues)) fileConfiguration.set(path, replacementValues);
    }

    private String normalizeSectionColorCodes(String value) {
        return value.replace('§', '&');
    }

    @Override
    public void processAdditionalFields() {

        migrateLegacyQuickActionDefaults();
        migrateWipVisualDefaults();

        doStatsPage = ConfigurationEngine.setBoolean(fileConfiguration, "doStatsPage", true);
        doGearPage = ConfigurationEngine.setBoolean(fileConfiguration, "doGearPage", true);
        doTeleportsPage = ConfigurationEngine.setBoolean(fileConfiguration, "doTeleportsPage", true);
        doCommandsPage = ConfigurationEngine.setBoolean(fileConfiguration, "doCommandsPage", true);
        doQuestTrackingPage = ConfigurationEngine.setBoolean(fileConfiguration, "doQuestTrackingPage", true);
        doBossTrackingPage = ConfigurationEngine.setBoolean(fileConfiguration, "doBossTrackingPage", true);

        landingOverviewLines = ConfigurationEngine.setList(
                List.of("Compact player overview shown on the /em dialog and inventory header.",
                        "Available placeholders: $money, $combatLevel, $activeQuests and $score.",
                        "$threat remains available as a legacy alias for $combatLevel."),
                file, fileConfiguration, "landingOverviewLines",
                List.of("<g:#B8860B:#F0C040>Coins</g>: &f$money &8• <g:#A63D2F:#E97932>Combat Level</g>: &f$combatLevel",
                        "<g:#2E7D4F:#69C56F>Active Quests</g>: &f$activeQuests &8• <g:#355CA8:#5FA9E8>Score</g>: &f$score"), true);

        dialogGearSummaryLines = ConfigurationEngine.setList(
                List.of("Modern combat summary shown in the Gear dialog.",
                        "Placeholders: $combatLevel, $referenceLevel, $weaponLevel, $weaponSkill,",
                        "$weaponSkillLevel, $weaponFactor, $critChance, $enchantmentBonus, $weaponPerks,",
                        "$armorLevel, $armorSkillLevel, $defenseMatch, $health, $maxHealth, $armorPerks",
                        "and $threatMultiplier."),
                file, fileConfiguration, "dialog.gearSummaryLines",
                List.of("<g:#A63D2F:#E97932>⚔ Equipment Matchup</g>",
                        "<g:#B8860B:#F0C040>Combat Level</g>: &f$combatLevel",
                        "<g:#A63D2F:#E97932>Held Weapon</g>: &fLv. $weaponLevel &8• &7$weaponSkill: &fLv. $weaponSkillLevel",
                        "<g:#A63D2F:#E97932>Weapon factor vs Lv.</g> &f$referenceLevel: &f×$weaponFactor",
                        "<g:#A04468:#E07A9A>Critical chance</g>: &f$critChance% &8• &7Equipment bonus: &f+$enchantmentBonus%",
                        "&7Selected weapon perks: &f$weaponPerks",
                        "<g:#355CA8:#5FA9E8>Melee armor</g>: &f$armorLevel &8• &7Armor Skill: &fLv. $armorSkillLevel",
                        "<g:#355CA8:#5FA9E8>Melee reduction vs Lv.</g> &f$referenceLevel: &f$defenseMatch%",
                        "<g:#2E7D4F:#69C56F>Health</g>: &f$health&7/&f$maxHealth",
                        "&7Selected armor perks: &f$armorPerks",
                        "<g:#B8860B:#F0C040>Threat generated</g>: &f×$threatMultiplier",
                        "&8Equipment matchup only; skills and temporary effects apply in combat."), true);
        gearUnarmedLabel = ConfigurationEngine.setString(
                List.of("Weapon skill label shown when no recognized weapon is held."),
                file, fileConfiguration, "dialog.gearUnarmedLabel", "Unarmed", true);
        gearNoSelectedPerksLabel = ConfigurationEngine.setString(
                List.of("Text shown in the Combat Gear profile when no applicable perks are selected."),
                file, fileConfiguration, "dialog.gearNoSelectedPerksLabel", "None selected", true);
        dialogGearHelmetLabel = ConfigurationEngine.setString(file, fileConfiguration,
                "dialog.gearHelmetLabel", "Helmet", true);
        dialogGearChestplateLabel = ConfigurationEngine.setString(file, fileConfiguration,
                "dialog.gearChestplateLabel", "Chestplate", true);
        dialogGearLeggingsLabel = ConfigurationEngine.setString(file, fileConfiguration,
                "dialog.gearLeggingsLabel", "Leggings", true);
        dialogGearBootsLabel = ConfigurationEngine.setString(file, fileConfiguration,
                "dialog.gearBootsLabel", "Boots", true);
        dialogGearMainHandLabel = ConfigurationEngine.setString(file, fileConfiguration,
                "dialog.gearMainHandLabel", "Main Hand", true);
        dialogGearOffHandLabel = ConfigurationEngine.setString(file, fileConfiguration,
                "dialog.gearOffHandLabel", "Off Hand", true);
        dialogQuestFallbackFormat = ConfigurationEngine.setString(
                List.of("Fallback quest label used when a quest has no configured display name.",
                        "Available placeholder: $id."),
                file, fileConfiguration, "dialog.questFallbackFormat", "Quest $id", true);

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
        indexLineCreator(6, "&bp. $statsPage &8- " + STATS_TITLE, "Click to go!", "$statsPage", fileConfiguration, file);
        indexLineCreator(7, "&bp. $gearPage &8- " + GEAR_TITLE, "Click to go!", "$gearPage", fileConfiguration, file);
        indexLineCreator(8, "&bp. $teleportsPage &8- " + TELEPORTS_TITLE, "Click to go!", "$teleportsPage", fileConfiguration, file);
        indexLineCreator(9, "&bp. $commandsPage &8- " + COMMANDS_TITLE, "Open useful EliteMobs actions", "$commandsPage", fileConfiguration, file);
        indexLineCreator(10, "&bp. $questsPage &8- " + QUESTS_TITLE, "Click to go!", "$questsPage", fileConfiguration, file);
        indexLineCreator(11, "&bp. $bossTrackingPage &8- " + BOSS_TITLE, "Click to go!", "$bossTrackingPage", fileConfiguration, file);
        indexLineCreator(12, "&bp. $skillsPage &8- " + SKILLS_TITLE, "Click to go!", "$skillsPage", fileConfiguration, file);
        indexLineCreator(13, PARTY_TITLE, "Create, invite, or leave a party", "/em party menu", fileConfiguration, file);


        statsLineCreator(0, "&6&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        statsLineCreator(1, STATS_TITLE, "", "", fileConfiguration, file);
        statsLineCreator(2, "&6&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        statsLineCreator(3, "", "", "", fileConfiguration, file);
        statsLineCreator(4, "<g:#B8860B:#F0C040>★ Server Rank</g>: &f$rank &8/ &7$ranktotal",
                "Your server-wide position based on Adventure Score.", "", fileConfiguration, file);
        statsLineCreator(5, "<g:#355CA8:#5FA9E8>Adventure Score</g>: &f$score",
                "Higher-level Elite kills award more score. Elite deaths reduce it.", "", fileConfiguration, file);
        statsLineCreator(6, "<g:#6D3AA8:#A855F7>Dungeons Cleared</g>: &f$dungeons",
                "Successful dungeon clears. Entering or abandoning a run does not count.", "", fileConfiguration, file);
        statsLineCreator(7, "<g:#A63D2F:#E97932>Combat Level</g>: &f$combat",
                "Your overall level from armor and your two strongest weapon skills.", "", fileConfiguration, file);
        statsLineCreator(8, "", "", "", fileConfiguration, file);
        statsLineCreator(9, "<g:#2E7D4F:#69C56F>Elite Kills</g>: &f$kills", "Confirmed non-exploit Elite Mob kills.", "", fileConfiguration, file);
        statsLineCreator(10, "<g:#267A78:#58B8A9>Highest Elite Level</g>: &f$highestkill",
                "The highest-level Elite Mob you have defeated legitimately.", "", fileConfiguration, file);
        statsLineCreator(11, "<g:#B8860B:#F0C040>Quests Completed</g>: &f$quests",
                "EliteMobs quests you have fully completed.", "", fileConfiguration, file);
        statsLineCreator(12, "", "", "", fileConfiguration, file);

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
        gearLineCreator(10, "Combat Lv. $combatLevel | Crit $critChance%",
                "Weapon: $weaponSkill Lv. $weaponSkillLevel\nGear Lv. $weaponLevel vs Elite Lv. $referenceLevel",
                "", fileConfiguration, file);
        gearLineCreator(11, "Weapon ×$weaponFactor | Melee armor $defenseMatch%",
                "Melee armor rating: $armorLevel\nArmor skill: Lv. $armorSkillLevel\nEquipment damage bonus: +$enchantmentBonus%",
                "", fileConfiguration, file);
        gearLineCreator(12, "Threat ×$threatMultiplier | HP $health/$maxHealth",
                "Selected weapon perks: $weaponPerks\nSelected armor perks: $armorPerks",
                "", fileConfiguration, file);

        teleportLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        teleportLineCreator(1, "&2&lTeleports", "", "", fileConfiguration, file);
        teleportLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        teleportLineCreator(3, "&r&0Spawn", "Teleport to spawn!", "/em spawntp", fileConfiguration, file);
        teleportLineCreator(4, "&r" + PlayerStatusScreen.convertLightColorsToBlack(AdventurersGuildConfig.getAdventurersGuildMenuName()), "Teleport to the Adventurer's Guild Hub!", "/ag", fileConfiguration, file);

        onTeleportHover = ConfigurationEngine.setString(file, fileConfiguration, "onTeleportsHover", "Click to teleport!", true);

        commandsLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        commandsLineCreator(1, "&3&lQuick Actions:", "", "", fileConfiguration, file);
        commandsLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        commandsLineCreator(3, "", "", "", fileConfiguration, file);
        commandsLineCreator(4, "&5/ag &7- EliteMobs Hub", "CLICK TO USE\n" +
                "The place where you can find\n" +
                "NPCs that give quests, buy and\n" +
                "sell items, give advice and more!", "/ag", fileConfiguration, file);
        commandsLineCreator(5, "&5/em spawntp &7- Spawn", "CLICK TO USE\nTeleport to the server spawn!", "/em spawntp", fileConfiguration, file);
        commandsLineCreator(6, "&5/em shareitem &7- Share Item", "CLICK TO USE\n" +
                "Shares the item you're holding\n" +
                "on chat!", "/em shareitem", fileConfiguration, file);
        commandsLineCreator(7, "", "", "", fileConfiguration, file);
        commandsLineCreator(8, "", "", "", fileConfiguration, file);
        commandsLineCreator(9, "", "", "", fileConfiguration, file);
        commandsLineCreator(10, "", "", "", fileConfiguration, file);
        commandsLineCreator(11, "", "", "", fileConfiguration, file);
        commandsLineCreator(12, "", "", "", fileConfiguration, file);

        bossTrackerLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);
        bossTrackerLineCreator(1, "&4&lBoss Tracker:", "Big bosses get displayed here!", "", fileConfiguration, file);
        bossTrackerLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", fileConfiguration, file);

        onBossTrackHover = ConfigurationEngine.setString(file, fileConfiguration, "onBossTrackHover", "Click to track/untrack!", true);

        teleportChestMenuName = ConfigurationEngine.setString(file, fileConfiguration, "teleportChestMenuName", TELEPORTS_TITLE, true);
        teleportSpawnItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "teleportSpawnItem",
                ItemStackGenerator.generateItemStack(Material.COMPASS,
                        "&6&lSpawn", List.of("&7Teleport to the server spawn.", "", "&aClick to teleport!")), true);
        teleportSpawnSlot = ConfigurationEngine.setInt(fileConfiguration, "teleportSpawnSlot", 45);
        teleportGuildItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "teleportGuildItem",
                ItemStackGenerator.generateItemStack(Material.END_PORTAL_FRAME,
                        "&6&lEliteMobs Hub", List.of("&7Visit the Adventurer's Guild.", "", "&aClick to teleport!")), true);
        teleportGuildSlot = ConfigurationEngine.setInt(fileConfiguration, "teleportGuildSlot", 49);

        bossTrackerChestMenuName = ConfigurationEngine.setString(file, fileConfiguration, "bossTrackerChestMenuName", BOSS_TITLE, true);

        //inventory-based menus for bedrock
        indexChestMenuName = ConfigurationEngine.setString(file, fileConfiguration, "indexChestMenuName", ROOT_TITLE, true);
        indexHeaderItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "indexHeaderItem",
                ItemStackGenerator.generateItemStack(Material.PAPER,
                        "&5&l/ag &7- &6EliteMobs Hub",
                        new ArrayList<>(List.of("CLICK TO USE",
                                "The place where you can find",
                                "NPCs that give quests, buy and",
                                "sell items, give advice and more!"))), true);
        indexHeaderSlot = ConfigurationEngine.setInt(fileConfiguration, "indexHeaderSlot", 4);

        indexStatsItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "indexStatsItem",
                ItemStackGenerator.generateItemStack(Material.MAP,
                        STATS_TITLE,
                        List.of("Click to go!")), true);
        indexStatsSlot = ConfigurationEngine.setInt(fileConfiguration, "indexStatsSlot", 10);

        indexGearItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "indexGearItem",
                ItemStackGenerator.generateItemStack(Material.DIAMOND_SWORD,
                        GEAR_TITLE,
                        List.of("Click to go!")), true);
        indexGearSlot = ConfigurationEngine.setInt(fileConfiguration, "indexGearSlot", 12);

        indexTeleportsItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "indexTeleportsItem",
                ItemStackGenerator.generateItemStack(Material.END_PORTAL_FRAME,
                        TELEPORTS_TITLE,
                        List.of("Click to go!")), true);
        indexTeleportsSlot = ConfigurationEngine.setInt(fileConfiguration, "indexTeleportsSlot", 14);

        indexCommandsItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "indexCommandsItem",
                ItemStackGenerator.generateItemStack(Material.JUKEBOX,
                        COMMANDS_TITLE,
                        List.of("&7Open the Hub, teleport to spawn", "&7or share your held Elite item.", "", "&aClick to view!")), true);
        indexCommandsSlot = ConfigurationEngine.setInt(fileConfiguration, "indexCommandsSlot", 16);


        indexQuestTrackingItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "indexQuestTrackingItem",
                ItemStackGenerator.generateItemStack(Material.WRITABLE_BOOK,
                        QUESTS_TITLE,
                        List.of("Click to go!")), true);
        indexQuestTrackingSlot = ConfigurationEngine.setInt(fileConfiguration, "indexQuestTrackingSlot", 20);

        indexBossTrackingItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "indexBossTrackingItem",
                ItemStackGenerator.generateItemStack(Material.TARGET,
                        BOSS_TITLE,
                        List.of("Click to go!")), true);

        indexBossTrackingSlot = ConfigurationEngine.setInt(fileConfiguration, "indexBossTrackingSlot", 24);

        indexSkillsItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "indexSkillsItem",
                ItemStackGenerator.generateItemStack(Material.EXPERIENCE_BOTTLE,
                        SKILLS_TITLE,
                        List.of("&7View your skill levels", "&7and XP progress.", "", "&aClick to view!")), true);
        indexSkillsSlot = ConfigurationEngine.setInt(fileConfiguration, "indexSkillsSlot", 22);

        indexPartyItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "indexPartyItem",
                ItemStackGenerator.generateItemStack(Material.PLAYER_HEAD,
                        PARTY_TITLE,
                        List.of("&7Create or manage a party", "&7and enter dungeons together.", "", "&aClick for party controls!")), true);
        indexPartySlot = ConfigurationEngine.setInt(fileConfiguration, "indexPartySlot", 26);


        backItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "backItem",
                ItemStackGenerator.generateItemStack(Material.BARRIER, "&cBack"), true);

        gearChestMenuName = ConfigurationEngine.setString(file, fileConfiguration, "gearChestMenuName", GEAR_TITLE, true);
        gearDamageItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "gearDamageItem",
                ItemStackGenerator.generateItemStack(Material.DIAMOND_SWORD,
                        "<g:#A63D2F:#E97932>⚔ Held Weapon</g>: &fLv. $weaponLevel",
                        new ArrayList<>(List.of("&7$weaponSkill skill: &fLv. $weaponSkillLevel",
                                "&7Weapon factor vs Lv. $referenceLevel: &f×$weaponFactor",
                                "&7Critical chance: &f$critChance%",
                                "&7Equipment damage bonus: &f+$enchantmentBonus%", "",
                                "&7Selected weapon perks:", "&f$weaponPerks"))), true);
        gearDamageSlot = ConfigurationEngine.setInt(fileConfiguration, "gearDamageSlot", 23);
        gearArmorItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "gearArmorItem",
                ItemStackGenerator.generateItemStack(Material.SHIELD,
                        "<g:#355CA8:#5FA9E8>⛨ Armor Rating</g>: &f$armorLevel",
                        new ArrayList<>(List.of("&7Armor skill: &fLv. $armorSkillLevel",
                                "&7Melee reduction vs Lv. $referenceLevel: &f$defenseMatch%",
                                "&7Health: &f$health&7/&f$maxHealth", "",
                                "&7Selected armor perks:", "&f$armorPerks"))), true);
        gearArmorSlot = ConfigurationEngine.setInt(fileConfiguration, "gearArmorSlot", 24);
        gearThreatItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "gearThreatItem",
                ItemStackGenerator.generateItemStack(Material.TARGET,
                        "<g:#B8860B:#F0C040>★ Combat Level</g>: &f$combatLevel",
                        new ArrayList<>(List.of("&7Progression baseline: &fLv. $combatLevel",
                                "&7Threat generated: &f×$threatMultiplier", "",
                                "&8Natural scaling may also use distance,",
                                "&8nearby players, party size and randomness."))), true);
        gearThreatSlot = ConfigurationEngine.setInt(fileConfiguration, "gearThreatSlot", 25);

        statsChestMenuName = ConfigurationEngine.setString(file, fileConfiguration, "statsChestMenuName", STATS_TITLE, true);
        statsRankUnavailableText = ConfigurationEngine.setString(
                List.of("Text used for $rank while the persisted score ranking cache is still loading."),
                file, fileConfiguration, "statsRankUnavailableText", "Calculating...", true);
        statsRankTotalUnavailableText = ConfigurationEngine.setString(
                List.of("Text used for $ranktotal while the persisted score ranking cache is still loading."),
                file, fileConfiguration, "statsRankTotalUnavailableText", "?", true);

        statsRankItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "statsRankItem",
                ItemStackGenerator.generateItemStack(Material.NETHER_STAR,
                        "<g:#B8860B:#F0C040>★ Server Rank</g> &8• &f$rank &7/ $ranktotal",
                        List.of("&7Competition rank by Adventure Score.",
                                "&7Equal scores share the same rank.")), true);
        statsRankSlot = ConfigurationEngine.setInt(fileConfiguration, "statsRankSlot", 10);

        statsDungeonsCompletedItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "statsDungeonsCompletedItem",
                ItemStackGenerator.generateItemStack(Material.ENDER_EYE,
                        "<g:#6D3AA8:#A855F7>Dungeons Cleared</g> &8• &f$dungeons",
                        List.of("&7Counts successful clears only.",
                                "&7Entering or abandoning does not count.")), true);
        statsDungeonsCompletedSlot = ConfigurationEngine.setInt(fileConfiguration, "statsDungeonsCompletedSlot", 12);

        statsCombatLevelItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "statsCombatLevelItem",
                ItemStackGenerator.generateItemStack(Material.TARGET,
                        "<g:#A63D2F:#E97932>Combat Level</g> &8• &f$combat",
                        List.of("&7Calculated from armor and your two",
                                "&7strongest weapon skills.")), true);
        statsCombatLevelSlot = ConfigurationEngine.setInt(fileConfiguration, "statsCombatLevelSlot", 13);

        statsEliteKillsItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "statsEliteKillsItem",
                ItemStackGenerator.generateItemStack(Material.DIAMOND_SWORD,
                        "<g:#2E7D4F:#69C56F>Elite Kills</g> &8• &f$kills",
                        List.of("&7Confirmed non-exploit Elite Mob kills.")), true);
        statsEliteKillsSlot = ConfigurationEngine.setInt(fileConfiguration, "statsEliteKillsSlot", 14);

        statsMaxEliteLevelKilledItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "statsMaxEliteLevelKilledItem",
                ItemStackGenerator.generateItemStack(Material.BEACON,
                        "<g:#267A78:#58B8A9>Peak Elite Level</g> &8• &f$maxKill",
                        List.of("&7Your highest legitimate Elite Mob kill.")), true);
        statsMaxEliteLevelKilledSlot = ConfigurationEngine.setInt(fileConfiguration, "statsMaxEliteLevelKilledSlot", 15);

        statsQuestsCompletedItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "statsQuestsCompletedItem",
                ItemStackGenerator.generateItemStack(Material.WRITABLE_BOOK,
                        "<g:#B8860B:#F0C040>Quests Completed</g> &8• &f$questsCompleted",
                        List.of("&7EliteMobs quests fully completed.")), true);
        statsQuestsCompletedSlot = ConfigurationEngine.setInt(fileConfiguration, "statsQuestsCompletedSlot", 16);

        statsScoreItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "statsScoreItem",
                ItemStackGenerator.generateItemStack(Material.EXPERIENCE_BOTTLE,
                        "<g:#355CA8:#5FA9E8>Adventure Score</g> &8• &f$score",
                        List.of("&7Higher-level Elite kills award more score.",
                                "&7Elite deaths reduce it.")), true);
        statsScoreSlot = ConfigurationEngine.setInt(fileConfiguration, "statsScoreSlot", 11);

        commandsChestMenuName = ConfigurationEngine.setString(file, fileConfiguration, "commandsChestMenuName", COMMANDS_TITLE, true);

        commandsAGItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "commandsAGItem",
                ItemStackGenerator.generateItemStack(Material.END_PORTAL_FRAME,
                        "&5/ag",
                        new ArrayList<>(List.of("&fClick to use!",
                                "&fThe place where you can find",
                                "&fNPCs that give quests, buy and",
                                "&fsell items, give advice and more!"))), true);
        commandsAGSlot = ConfigurationEngine.setInt(fileConfiguration, "commandsAGSlot", 11);

        commandsSpawnItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "commandsSpawnItem",
                ItemStackGenerator.generateItemStack(Material.COMPASS,
                        "&5/em spawntp",
                        new ArrayList<>(List.of("&fClick to use!", "&fTeleports you to the server spawn."))), true);
        commandsSpawnSlot = ConfigurationEngine.setInt(fileConfiguration, "commandsSpawnSlot", 13);

        commandsShareItemItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "commandsShareItemItem",
                ItemStackGenerator.generateItemStack(Material.PAPER,
                        "&5/em shareitem",
                        new ArrayList<>(List.of("&fClick to use!",
                                "&fShares the Elite Item you're holding",
                                "&fon chat!"))), true);
        commandsShareItemSlot = ConfigurationEngine.setInt(fileConfiguration, "commandsShareItemSlot", 15);

        skillsItemDisplayName = ConfigurationEngine.setString(file, fileConfiguration, "skillsItemDisplayName", SKILLS_TITLE, true);
        skillsItemLore1 = ConfigurationEngine.setString(file, fileConfiguration, "skillsItemLore1", "&7View your skill levels", true);
        skillsItemLore2 = ConfigurationEngine.setString(file, fileConfiguration, "skillsItemLore2", "&7and XP progress.", true);
        skillsItemClickLore = ConfigurationEngine.setString(file, fileConfiguration, "skillsItemClickLore", "&eClick to view!", true);
        skillsPageHeader = ConfigurationEngine.setString(file, fileConfiguration, "skillsPageHeader", "<g:#6D3AA8:#A855F7>⚗ Your Skills</g>", true);
        skillsPageLevelFormat = ConfigurationEngine.setString(file, fileConfiguration, "skillsPageLevelFormat", "&6$skillName &7Lv.&e$level", true);
        skillsPageXpFormat = ConfigurationEngine.setString(file, fileConfiguration, "skillsPageXpFormat", "&8$progressBar &7$currentXp/$nextXp", true);
        skillItemDisplayNameFormat = ConfigurationEngine.setString(file, fileConfiguration, "skillItemDisplayNameFormat", "&6&lLevel $level $skillName", true);
        skillItemSelectLore1 = ConfigurationEngine.setString(file, fileConfiguration, "skillItemSelectLore1", "&eClick to select passive skills", true);
        skillItemSelectLore2 = ConfigurationEngine.setString(file, fileConfiguration, "skillItemSelectLore2", "&eand see more details!", true);

        dialogTitlePlayerStatus = ConfigurationEngine.setString(
                List.of("Title for the main player status dialog menu."),
                file, fileConfiguration, "dialog.titlePlayerStatus", ROOT_TITLE, true);

        dialogTitleStats = ConfigurationEngine.setString(
                List.of("Title for the Stats dialog section."),
                file, fileConfiguration, "dialog.titleStats", STATS_TITLE, true);

        dialogTitleGear = ConfigurationEngine.setString(
                List.of("Title for the Gear dialog section."),
                file, fileConfiguration, "dialog.titleGear", GEAR_TITLE, true);

        dialogTitleTeleports = ConfigurationEngine.setString(
                List.of("Title for the Teleports dialog section."),
                file, fileConfiguration, "dialog.titleTeleports", TELEPORTS_TITLE, true);

        dialogTitleCommands = ConfigurationEngine.setString(
                List.of("Title for the Quick Actions dialog section."),
                file, fileConfiguration, "dialog.titleCommands", COMMANDS_TITLE, true);

        dialogTitleQuests = ConfigurationEngine.setString(
                List.of("Title for the Quests dialog section."),
                file, fileConfiguration, "dialog.titleQuests", QUESTS_TITLE, true);

        dialogTitleBossTracking = ConfigurationEngine.setString(
                List.of("Title for the Boss Tracking dialog section."),
                file, fileConfiguration, "dialog.titleBossTracking", BOSS_TITLE, true);

        dialogTitleSkills = ConfigurationEngine.setString(
                List.of("Title for the Skills dialog section."),
                file, fileConfiguration, "dialog.titleSkills", SKILLS_TITLE, true);

        dialogTitleParty = ConfigurationEngine.setString(
                List.of("Title for the Party dialog section."),
                file, fileConfiguration, "dialog.titleParty", PARTY_TITLE, true);
        dialogPartyDescription = ConfigurationEngine.setString(
                List.of("Description shown on the Party dialog section."),
                file, fileConfiguration, "dialog.partyDescription",
                "&7Team up with as many as four other players. Parties share nearby quest progress, vote on loot, and enter dungeons together.", true);
        dialogPartyCreateButton = ConfigurationEngine.setString(
                List.of("Button used to create a party."),
                file, fileConfiguration, "dialog.partyCreateButton", "<g:#2E7D4F:#69C56F>+ Create a Party</g>", true);
        dialogPartyInviteButton = ConfigurationEngine.setString(
                List.of("Button used to prepare the party invite command."),
                file, fileConfiguration, "dialog.partyInviteButton", "<g:#267A78:#58B8A9>✉ Invite a Player</g>", true);
        dialogPartyLeaveButton = ConfigurationEngine.setString(
                List.of("Button used to leave a party."),
                file, fileConfiguration, "dialog.partyLeaveButton", "<g:#7A1F2B:#C2414A>✘ Leave Party</g>", true);
        dialogPartyInviteTitle = ConfigurationEngine.setString(
                List.of("Title for the dialog that lists players available for a party invitation."),
                file, fileConfiguration, "dialog.partyInviteTitle", "<g:#A04468:#E07A9A>✉ Invite a Player</g>", true);
        dialogPartyInvitePlayerButton = ConfigurationEngine.setString(
                List.of("Button for an available party invite target. Use $player for their name."),
                file, fileConfiguration, "dialog.partyInvitePlayerButton", "<g:#267A78:#58B8A9>✉ Invite</g> &f$player", true);
        dialogPartyInvitePlayerTooltip = ConfigurationEngine.setString(
                List.of("Tooltip for a party invite target. Use $player for their name."),
                file, fileConfiguration, "dialog.partyInvitePlayerTooltip", "&7Invite $player to your party.", true);
        dialogPartyInviteNoPlayers = ConfigurationEngine.setString(
                List.of("Message shown when nobody online can currently be invited."),
                file, fileConfiguration, "dialog.partyInviteNoPlayers", "&7No available players are online right now.", true);

        dialogNoActiveQuests = ConfigurationEngine.setString(
                List.of("Message shown when the player has no active quests."),
                file, fileConfiguration, "dialog.noActiveQuests", "No active quests", true);

        dialogNoTrackableBosses = ConfigurationEngine.setString(
                List.of("Message shown when there are no bosses available to track."),
                file, fileConfiguration, "dialog.noTrackableBosses", "&7No trackable bosses are currently active.", true);

        dialogActiveQuestsFormat = ConfigurationEngine.setString(
                List.of("Message showing the number of active quests. Use $amount for the quest count."),
                file, fileConfiguration, "dialog.activeQuestsFormat", "You have $amount active quest(s).", true);

        dialogBackButton = ConfigurationEngine.setString(
                List.of("Text for the back button in dialog sections."),
                file, fileConfiguration, "dialog.backButton", "\u2190 Back to Menu", true);
    }

}
