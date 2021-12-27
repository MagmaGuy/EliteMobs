package com.magmaguy.elitemobs.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class MobCombatSettingsConfig {
    @Getter
    private static boolean doNaturalMobSpawning;
    @Getter
    private static boolean doSpawnersSpawnEliteMobs;
    @Getter
    private static double aggressiveMobConversionPercentage;
    @Getter
    private static int superMobsStackRange;
    @Getter
    private static int naturalEliteMobLevelCap;
    @Getter
    private static boolean doEliteArmor;
    @Getter
    private static boolean doEliteHelmets;
    @Getter
    private static boolean enableVisualEffectsForNaturalMobs;
    @Getter
    private static boolean disableVisualEffectsForSpawnerMobs;
    @Getter
    private static boolean enableWarningVisualEffects;
    @Getter
    private static boolean enableDeathMessages;
    @Getter
    private static boolean displayHealthOnHit;
    @Getter
    private static boolean displayDamageOnHit;
    @Getter
    private static boolean increaseDifficultyWithSpawnDistance;
    @Getter
    private static double distanceToIncrement;
    @Getter
    private static double levelToIncrement;
    @Getter
    private static boolean obfuscateMobPowers;
    @Getter
    private static double damageToEliteMultiplier;
    @Getter
    private static double damageToPlayerMultiplier;
    @Getter
    private static boolean showCustomBossLocation;
    @Getter
    private static String bossLocationMessage;
    @Getter
    private static List<String> commandsOnDeath;
    @Getter
    private static String bossKillParticipationMessage;
    @Getter
    private static boolean regenerateCustomBossHealthOnCombatEnd;
    @Getter
    private static String defaultOtherWorldBossLocationMessage;
    private MobCombatSettingsConfig() {
    }
    @Getter
    private static String weakTextColor;
    @Getter
    private static String resistTextColor;
    @Getter
    private static String weakText;
    @Getter
    private static String resistText;
    @Getter
    private static boolean doWeakEffect;
    @Getter
    private static boolean doResistEffect;

    public static void initializeConfig() {

        File file = ConfigurationEngine.fileCreator("MobCombatSettings.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        doNaturalMobSpawning = ConfigurationEngine.setBoolean(fileConfiguration, "doNaturalEliteMobSpawning", true);
        doSpawnersSpawnEliteMobs = ConfigurationEngine.setBoolean(fileConfiguration, "doSpawnersSpawnEliteMobs", false);
        aggressiveMobConversionPercentage = ConfigurationEngine.setDouble(fileConfiguration, "eliteMobsSpawnPercentage", 0.05);
        superMobsStackRange = Math.max(ConfigurationEngine.setInt(fileConfiguration, "superMobStackRange", 15), 2);
        naturalEliteMobLevelCap = ConfigurationEngine.setInt(fileConfiguration, "naturalEliteMobsLevelCap", 250);
        doEliteArmor = ConfigurationEngine.setBoolean(fileConfiguration, "doElitesWearArmor", true);
        doEliteHelmets = ConfigurationEngine.setBoolean(fileConfiguration, "doElitesWearHelmets", true);
        enableVisualEffectsForNaturalMobs = ConfigurationEngine.setBoolean(fileConfiguration, "doNaturalEliteMobVisualEffects", true);
        disableVisualEffectsForSpawnerMobs = ConfigurationEngine.setBoolean(fileConfiguration, "doSpawnerEliteMobVisualEffects", false);
        enableWarningVisualEffects = ConfigurationEngine.setBoolean(fileConfiguration, "doPowerBuildupVisualEffects", true);
        enableDeathMessages = ConfigurationEngine.setBoolean(fileConfiguration, "doCustomEliteMobsDeathMessages", true);
        displayHealthOnHit = ConfigurationEngine.setBoolean(fileConfiguration, "doDisplayMobHealthOnHit", true);
        displayDamageOnHit = ConfigurationEngine.setBoolean(fileConfiguration, "doDisplayMobDamageOnHit", true);
        increaseDifficultyWithSpawnDistance = ConfigurationEngine.setBoolean(fileConfiguration, "doIncreaseEliteMobLevelBasedOnSpawnDistance", false);
        distanceToIncrement = ConfigurationEngine.setDouble(fileConfiguration, "distanceBetweenIncrements", 100);
        levelToIncrement = ConfigurationEngine.setDouble(fileConfiguration, "levelIncreaseAtIncrements", 1);
        obfuscateMobPowers = ConfigurationEngine.setBoolean(fileConfiguration, "hideEliteMobPowersUntilAggro", true);
        damageToEliteMultiplier = ConfigurationEngine.setDouble(fileConfiguration, "damageToEliteMobMultiplier", 1);
        damageToPlayerMultiplier = ConfigurationEngine.setDouble(fileConfiguration, "damageToPlayerMultiplier", 1);
        showCustomBossLocation = ConfigurationEngine.setBoolean(fileConfiguration, "showCustomBossLocation", true);
        bossLocationMessage = ConfigurationEngine.setString(fileConfiguration, "bossLocationMessage", "&7[EM] &2[Click to track!]");
        //Accepts placeholders $players, $level and $name
        commandsOnDeath = ConfigurationEngine.setList(fileConfiguration, "commandsOnEliteMobDeath", Collections.emptyList());
        //Accepts placeholder $playerDamage
        bossKillParticipationMessage = ConfigurationEngine.setString(fileConfiguration, "bossKillParticipationMessage", "&eYour damage: &2$playerDamage");
        regenerateCustomBossHealthOnCombatEnd = ConfigurationEngine.setBoolean(fileConfiguration, "regenerateCustomBossHealthOnCombatEnd", true);
        defaultOtherWorldBossLocationMessage = ConfigurationEngine.setString(fileConfiguration, "defaultOtherWorldBossLocationMessage", "$name: In different world!");
        weakTextColor = ConfigurationEngine.setString(fileConfiguration, "weakTextColor", "&9");
        resistTextColor = ConfigurationEngine.setString(fileConfiguration, "resistTextColor", "&c");
        weakText = ConfigurationEngine.setString(fileConfiguration, "weakText", "&9&lWeak!");
        resistText = ConfigurationEngine.setString(fileConfiguration, "resistText", "&c&lResist!");
        doWeakEffect = ConfigurationEngine.setBoolean(fileConfiguration, "doWeakEffect", true);
        doResistEffect = ConfigurationEngine.setBoolean(fileConfiguration, "doResistEffect", true);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }
}
