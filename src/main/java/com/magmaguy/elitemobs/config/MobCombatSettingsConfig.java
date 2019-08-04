package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class MobCombatSettingsConfig {

    public static boolean doNaturalMobSpawning;
    public static boolean doSpawnersSpawnEliteMobs;
    public static double aggressiveMobConversionPercentage;
    public static int perTierLevelIncrease;
    public static int superMobsStackRange;
    public static int naturalElitemobLevelCap;
    public static double eliteCreeperExplosionMultiplier;
    public static boolean doEliteArmor;
    public static boolean doEliteHelmets;
    public static boolean enableVisualEffectsForNaturalMobs;
    public static boolean disableVisualEffectsForSpawnerMobs;
    public static boolean enableWarningVisualEffects;
    public static boolean enableDeathMessages;
    public static boolean displayHealthOnHit;
    public static boolean displayDamageOnHit;
    public static boolean onlyShowHealthForEliteMobs;
    public static boolean onlyShowDamageForEliteMobs;
    public static boolean increaseDifficultyWithSpawnDistance;
    public static double distanceToIncrement;
    public static double levelToIncrement;
    public static boolean obfuscateMobPowers;
    public static double damageToEliteMultiplier, damageToPlayerMultiplier;
    public static boolean showCustomBossLocation;
    public static String bossLocationMessage;

    public static void initializeConfig() {

        File file = ConfigurationEngine.fileCreator("MobCombatSettings.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        doNaturalMobSpawning = ConfigurationEngine.setBoolean(fileConfiguration, "doNaturalEliteMobSpawning", true);
        doSpawnersSpawnEliteMobs = ConfigurationEngine.setBoolean(fileConfiguration, "doSpawnersSpawnEliteMobs", false);
        aggressiveMobConversionPercentage = ConfigurationEngine.setDouble(fileConfiguration, "eliteMobsSpawnPercentage", 0.2);
        perTierLevelIncrease = ConfigurationEngine.setInt(fileConfiguration, "levelsBetweenEliteMobTiers", 10);
        superMobsStackRange = ConfigurationEngine.setInt(fileConfiguration, "superMobStackRange", 15);
        naturalElitemobLevelCap = ConfigurationEngine.setInt(fileConfiguration, "naturalEliteMobLevelCap", 1500);
        eliteCreeperExplosionMultiplier = ConfigurationEngine.setDouble(fileConfiguration, "eliteCreeperExplosionMultiplier", 1D);
        doEliteArmor = ConfigurationEngine.setBoolean(fileConfiguration, "doElitesWearArmor", true);
        doEliteHelmets = ConfigurationEngine.setBoolean(fileConfiguration, "doElitesWearHelmets", true);
        enableVisualEffectsForNaturalMobs = ConfigurationEngine.setBoolean(fileConfiguration, "doNaturalEliteMobVisualEffects", true);
        disableVisualEffectsForSpawnerMobs = ConfigurationEngine.setBoolean(fileConfiguration, "doSpawnerEliteMobVisualEffects", false);
        enableWarningVisualEffects = ConfigurationEngine.setBoolean(fileConfiguration, "doPowerBuildupVisualEffects", true);
        enableDeathMessages = ConfigurationEngine.setBoolean(fileConfiguration, "doCustomEliteMobsDeathMessages", true);
        displayHealthOnHit = ConfigurationEngine.setBoolean(fileConfiguration, "doDisplayMobHealthOnHit", true);
        displayDamageOnHit = ConfigurationEngine.setBoolean(fileConfiguration, "doDisplayMobDamageOnHit", true);
        onlyShowHealthForEliteMobs = ConfigurationEngine.setBoolean(fileConfiguration, "doEliteMobHealthDisplayOnly", true);
        onlyShowDamageForEliteMobs = ConfigurationEngine.setBoolean(fileConfiguration, "doEliteMobDamageDisplayOnly", true);
        increaseDifficultyWithSpawnDistance = ConfigurationEngine.setBoolean(fileConfiguration, "doIncreaseEliteMobLevelBasedOnSpawnDistance", false);
        distanceToIncrement = ConfigurationEngine.setDouble(fileConfiguration, "distanceBetweenIncrements", 100);
        levelToIncrement = ConfigurationEngine.setDouble(fileConfiguration, "levelIncreaseAtIncrements", 1);
        obfuscateMobPowers = ConfigurationEngine.setBoolean(fileConfiguration, "hideEliteMobPowersUntilAggro", true);
        damageToEliteMultiplier = ConfigurationEngine.setDouble(fileConfiguration, "damageToEliteMobMultiplier", 1);
        damageToPlayerMultiplier = ConfigurationEngine.setDouble(fileConfiguration, "damageToPlayerMultiplier", 1);
        showCustomBossLocation = ConfigurationEngine.setBoolean(fileConfiguration, "showCustomBossLocation", true);
        bossLocationMessage = ConfigurationEngine.setString(fileConfiguration, "bossLocationMessage", "&7[EM] &2[Click to track!]");

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);

    }

}
