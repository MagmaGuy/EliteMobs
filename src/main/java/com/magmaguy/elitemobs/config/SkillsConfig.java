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
    private static String skillsMenuTitle;
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
    private static SkillsConfig instance;

    public SkillsConfig() {
        super("skills.yml");
    }

    public static void toggleSkillSystem(boolean enabled) {
        skillSystemEnabled = enabled;
        instance.fileConfiguration.set("skillSystemEnabled", enabled);
        ConfigurationEngine.fileSaverCustomValues(instance.fileConfiguration, instance.file);
    }

    @Override
    public void initializeValues() {
        instance = this;
        skillSystemEnabled = ConfigurationEngine.setBoolean(
                List.of("Enables or disables the skill leveling system.",
                        "When enabled, players will earn XP for different weapon types and armor."),
                fileConfiguration, "skillSystemEnabled", true);

        skillsMenuTitle = ConfigurationEngine.setString(
                List.of("Title of the skills menu."),
                file, fileConfiguration, "skillsMenuTitle", "&5&lYour Skills", true);

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