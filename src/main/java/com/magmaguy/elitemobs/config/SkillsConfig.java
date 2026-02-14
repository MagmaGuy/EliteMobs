package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.List;

/**
 * Configuration for the skill leveling system.
 */
public class SkillsConfig extends ConfigurationFile {

    @Getter
    private static boolean skillSystemEnabled;
    @Getter
    private static String levelUpMessage;
    @Getter
    private static String levelUpSound;
    @Getter
    private static boolean showMilestoneTitles;
    @Getter
    private static int milestoneInterval;
    @Getter
    private static String skillsMenuTitle;
    @Getter
    private static boolean showXPGainMessages;
    @Getter
    private static double armorXPMultiplier;
    @Getter
    private static boolean showCombatLevelDisplay;
    @Getter
    private static boolean showXPBar;
    @Getter
    private static String skillBarTitleFormat;
    @Getter
    private static String skillLevelUpTitleFormat;
    @Getter
    private static String combatLevelFormat;

    public SkillsConfig() {
        super("skills.yml");
    }

    @Override
    public void initializeValues() {
        skillSystemEnabled = ConfigurationEngine.setBoolean(
                List.of("Enables or disables the skill leveling system.",
                        "When enabled, players will earn XP for different weapon types and armor."),
                fileConfiguration, "skillSystemEnabled", true);

        levelUpMessage = ConfigurationEngine.setString(
                List.of("Message shown when a player levels up a skill.",
                        "Placeholders: $skill - skill name, $level - new level"),
                file, fileConfiguration, "levelUpMessage",
                "&a&lSKILL UP! &r&a$skill &7is now level &e$level", true);

        levelUpSound = ConfigurationEngine.setString(
                List.of("Sound played when a player levels up a skill.",
                        "See: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html"),
                file, fileConfiguration, "levelUpSound", "ENTITY_PLAYER_LEVELUP", true);

        showMilestoneTitles = ConfigurationEngine.setBoolean(
                List.of("Whether to show title messages for milestone levels (every X levels)."),
                fileConfiguration, "showMilestoneTitles", true);

        milestoneInterval = ConfigurationEngine.setInt(
                List.of("How often milestone title messages are shown (every X levels)."),
                fileConfiguration, "milestoneInterval", 10);

        skillsMenuTitle = ConfigurationEngine.setString(
                List.of("Title of the skills menu."),
                file, fileConfiguration, "skillsMenuTitle", "&5&lYour Skills", true);

        showXPGainMessages = ConfigurationEngine.setBoolean(
                List.of("Whether to show messages when gaining skill XP.",
                        "This can be spammy, so disabled by default."),
                fileConfiguration, "showXPGainMessages", false);

        armorXPMultiplier = ConfigurationEngine.setDouble(
                List.of("Multiplier for armor XP. Default is 0.333 (1/3 of weapon XP)."),
                fileConfiguration, "armorXPMultiplier", 0.333);

        showCombatLevelDisplay = ConfigurationEngine.setBoolean(
                List.of("Whether to show a combat level display above players.",
                        "Combat level is the average of the two highest weapon skills and armor."),
                fileConfiguration, "showCombatLevelDisplay", true);

        showXPBar = ConfigurationEngine.setBoolean(
                List.of("Whether to show an animated XP progress bar when gaining skill XP.",
                        "The bar shows level progress and plays effects on level up."),
                fileConfiguration, "showXPBar", true);
        skillBarTitleFormat = ConfigurationEngine.setString(
                List.of("Format for the skill XP bar title.",
                        "Use $skillName for the skill name, $level for the level, $xpText for the XP gained text."),
                file, fileConfiguration, "skillBarTitleFormat", "&6$skillName &7Lv.$level$xpText", true);
        skillLevelUpTitleFormat = ConfigurationEngine.setString(
                List.of("Format for the skill level up boss bar title.",
                        "Use $skillName for the skill name, $level for the new level."),
                file, fileConfiguration, "skillLevelUpTitleFormat", "&e&l\u2726 $skillName LEVEL UP! &7\u2192 &eLv.$level &e&l\u2726", true);
        combatLevelFormat = ConfigurationEngine.setString(
                List.of("Format for displaying the combat level.",
                        "Use $level for the combat level number."),
                file, fileConfiguration, "combatLevelFormat", "&6&lCombat Lv. $level", true);
    }
}