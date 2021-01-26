package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MobCombatSettingsConfig {

    public static boolean doNaturalMobSpawning;
    public static boolean doSpawnersSpawnEliteMobs;
    public static double aggressiveMobConversionPercentage;
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
    public static List<String> commandsOnDeath;
    public static String bossKillParticipationMessage;
    public static boolean regenerateCustomBossHealthOnCombatEnd;
    public static String defaultOtherWorldBossLocationMessage;

    public static void initializeConfig() {

        File file = ConfigurationEngine.fileCreator("MobCombatSettings.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        doNaturalMobSpawning = ConfigurationEngine.setBoolean(fileConfiguration, "doNaturalEliteMobSpawning", true);
        doSpawnersSpawnEliteMobs = ConfigurationEngine.setBoolean(fileConfiguration, "doSpawnersSpawnEliteMobs", false);
        aggressiveMobConversionPercentage = ConfigurationEngine.setDouble(fileConfiguration, "eliteMobsSpawnPercentage", 0.05);
        superMobsStackRange = ConfigurationEngine.setInt(fileConfiguration, "superMobStackRange", 15);
        naturalElitemobLevelCap = ConfigurationEngine.setInt(fileConfiguration, "naturalEliteMobsLevelCap", 250);
        eliteCreeperExplosionMultiplier = ConfigurationEngine.setDouble(fileConfiguration, "eliteCreeperExplosionsMultiplier", 10D);
        doEliteArmor = ConfigurationEngine.setBoolean(fileConfiguration, "doElitesWearArmor", true);
        doEliteHelmets = ConfigurationEngine.setBoolean(fileConfiguration, "doElitesWearHelmets", true);
        enableVisualEffectsForNaturalMobs = ConfigurationEngine.setBoolean(fileConfiguration, "doNaturalEliteMobVisualEffects", true);
        disableVisualEffectsForSpawnerMobs = ConfigurationEngine.setBoolean(fileConfiguration, "doSpawnerEliteMobVisualEffects", false);
        enableWarningVisualEffects = ConfigurationEngine.setBoolean(fileConfiguration, "doPowerBuildupVisualEffects", true);
        enableDeathMessages = ConfigurationEngine.setBoolean(fileConfiguration, "doCustomEliteMobsDeathMessages", true);
        displayHealthOnHit = ConfigurationEngine.setBoolean(fileConfiguration, "doDisplayMobHealthOnHit", true);
        displayDamageOnHit = ConfigurationEngine.setBoolean(fileConfiguration, "doDisplayMobDamageOnHit", true);
        //todo: this option is currently dead
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
        //Accepts placeholders $players, $level and $name
        commandsOnDeath = ConfigurationEngine.setList(fileConfiguration, "commandsOnEliteMobDeath", new ArrayList());
        //Accepts placeholder $playerDamage
        bossKillParticipationMessage = ConfigurationEngine.setString(fileConfiguration, "bossKillParticipationMessage", "&eYour damage: &2$playerDamage");
        regenerateCustomBossHealthOnCombatEnd = ConfigurationEngine.setBoolean(fileConfiguration, "regenerateCustomBossHealthOnCombatEnd", true);
        defaultOtherWorldBossLocationMessage = ConfigurationEngine.setString(fileConfiguration, "defaultOtherWorldBossLocationMessage", "$name: In different world!");

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);

    }

}
