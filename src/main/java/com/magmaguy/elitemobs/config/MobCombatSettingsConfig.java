package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class MobCombatSettingsConfig extends ConfigurationFile {
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
    @Getter
    private static double normalizedBaselineDamage;
    @Getter
    private static double normalizedBaselineHealth;
    @Getter
    private static double normalizedDamageToEliteMultiplier;
    @Getter
    private static double normalizedDamageToPlayerMultiplier;
    @Getter
    private static boolean normalizeRegionalBosses;
    @Getter
    private static String fullHealMessage;
    @Getter
    private static double strengthAndWeaknessDamageMultipliers;
    @Getter
    private static double resistanceDamageMultiplier;
    @Getter
    private static double blockingDamageReduction;

    public MobCombatSettingsConfig() {
        super("MobCombatSettings.yml");
    }

    @Override
    public void initializeValues() {
        doNaturalMobSpawning = ConfigurationEngine.setBoolean(
                List.of("Sets if naturally spawned elites will spawn. Note: event mobs like the zombie king are not naturally spawned elites! You will have to disable events if you want to disable event bosses."),
                fileConfiguration, "doNaturalEliteMobSpawning", true);
        doSpawnersSpawnEliteMobs = ConfigurationEngine.setBoolean(
                List.of("Sets if spawns spawned from mob spawners can be converted to elites. Not recommended!"),
                fileConfiguration, "doSpawnersSpawnEliteMobs", false);
        aggressiveMobConversionPercentage = ConfigurationEngine.setDouble(
                List.of("Sets the percentage of naturally spawned mobs that get converted to elite mobs."),
                fileConfiguration, "eliteMobsSpawnPercentage", 0.05);
        superMobsStackRange = Math.max(ConfigurationEngine.setInt(
                List.of("Sets the super mob range to scan for super mob stacking"),
                fileConfiguration, "superMobStackRange", 15), 2);
        naturalEliteMobLevelCap = ConfigurationEngine.setInt(
                List.of("Sets the maximum level elites can spawn at.", "Note: elite mob level is based on what armor and weapons players are wearing, and armor only scales up to level 200."),
                fileConfiguration, "naturalEliteMobsLevelCap", 250);
        doEliteArmor = ConfigurationEngine.setBoolean(
                List.of("Sets if elites will wear armor based on their level. This is for visual purposes only and does not affect combat."),
                fileConfiguration, "doElitesWearArmor", true);
        doEliteHelmets = ConfigurationEngine.setBoolean(
                List.of("Sets if elites will wear helmets based on their level. This will prevent them from easily burning away during the daytime."),
                fileConfiguration, "doElitesWearHelmets", true);
        enableVisualEffectsForNaturalMobs = ConfigurationEngine.setBoolean(
                List.of("Sets if elites will have visual trails around them warning players about what players they have."),
                fileConfiguration, "doNaturalEliteMobVisualEffects", true);
        disableVisualEffectsForSpawnerMobs = ConfigurationEngine.setBoolean(
                List.of("Sets if elites spawned from spawners will do visual effects."),
                fileConfiguration, "doSpawnerEliteMobVisualEffects", false);
        enableWarningVisualEffects = ConfigurationEngine.setBoolean(
                List.of("Sets if some powers will do the warning phase of the power. This is very important as warning phases usually mean the power can be dodged, and the visual lets players know where to dodge to."),
                fileConfiguration, "doPowerBuildupVisualEffects", true);
        enableDeathMessages = ConfigurationEngine.setBoolean(
                List.of("Sets if custom death messages will be used when players die from elites."),
                fileConfiguration, "doCustomEliteMobsDeathMessages", true);
        displayHealthOnHit = ConfigurationEngine.setBoolean(
                List.of("Sets if EliteMobs will show health indicators for elites."),
                fileConfiguration, "doDisplayMobHealthOnHit", true);
        displayDamageOnHit = ConfigurationEngine.setBoolean(
                List.of("Sets if EliteMobs will show damage indicators for damage done to elites."),
                fileConfiguration, "doDisplayMobDamageOnHit", true);
        increaseDifficultyWithSpawnDistance = ConfigurationEngine.setBoolean(
                List.of("Sets if the level of elites will increased based on the distance from spawn.",
                        "This is a value added on top of their normal level, meaning that if a player is wearing level 100 gear near spawn and the boss has +1 level due from the distanced from spawn, the boss will spawn at level 101.",
                        "This option is generally not recommended, especially if you have a random tp system on your server."),
                fileConfiguration, "doIncreaseEliteMobLevelBasedOnSpawnDistance", false);
        distanceToIncrement = ConfigurationEngine.setDouble(
                List.of("Sets the distance between level increments for distance-based level increases."),
                fileConfiguration, "distanceBetweenIncrements", 100);
        levelToIncrement = ConfigurationEngine.setDouble(
                List.of("Sets how many levels increase at each distance increment for distance-based level increases."),
                fileConfiguration, "levelIncreaseAtIncrements", 1);
        obfuscateMobPowers = ConfigurationEngine.setBoolean(
                List.of("Sets if the powers of elites will be hidden until they enter combat. This is recommended for performance reasons."),
                fileConfiguration, "hideEliteMobPowersUntilAggro", true);
        damageToEliteMultiplier = ConfigurationEngine.setDouble(
                List.of("Sets the multiplier for the damage dealt to all bosses spawned by EliteMobs, except those using the normalized damage system (regional dungeon bosses). Higher values increase the damage dealt, making bosses easier to kill.", "2.0 = 200%, 0.5 = 50%"),
                fileConfiguration, "damageToEliteMobMultiplierV2", 1);
        damageToPlayerMultiplier = ConfigurationEngine.setDouble(
                List.of("Sets the multiplier for the damage dealt to players by elites. Higher values increase the amount of damage dealt by bosses, except those using the normalized damage system (regional dungeon bosses), making bosses hit harder.", "2.0 = 200%, 0.5 = 50%"),
                fileConfiguration, "damageToPlayerMultiplierV2", 1);
        showCustomBossLocation = ConfigurationEngine.setBoolean(
                List.of("Sets if special bosses can be tracked."),
                fileConfiguration, "showCustomBossLocation", true);
        bossLocationMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players to track a boss location."),
                file, fileConfiguration, "bossLocationMessage", "&7[EM] &2[Click to track!]", true);
        //Accepts placeholders $players, $level and $name
        commandsOnDeath = ConfigurationEngine.setList(
                List.of("Sets the commands that run when an elite dies. Valid placeholders are:", "$level for the level of the boss", "$name for the name of the boss", "$players will make the command run for each player that participated in the kill. As an example, if Bob and Steve killed a boss, 'broadcast $players killed the boss!' will run 'bob killed the boss' and 'steve killed the boss!'"),
                file, fileConfiguration, "commandsOnEliteMobDeath", Collections.emptyList(), false);
        //Accepts placeholder $playerDamage
        bossKillParticipationMessage = ConfigurationEngine.setString(
                List.of("Sets teh message sent to players that participate in big boss kills."),
                file, fileConfiguration, "bossKillParticipationMessage", "&eYour damage: &2$playerDamage", true);
        regenerateCustomBossHealthOnCombatEnd = ConfigurationEngine.setBoolean(
                List.of("Sets if bosses regenerate health when they go out of combat. Strongly recommended."),
                fileConfiguration, "regenerateCustomBossHealthOnCombatEnd", true);
        defaultOtherWorldBossLocationMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players that are trying to track bosses currently in a different world."),
                file, fileConfiguration, "defaultOtherWorldBossLocationMessage", "$name: In different world!", true);
        weakTextColor = ConfigurationEngine.setString(
                List.of("Sets the prefix added to damage indicators when players hit a boss with something that boss is weak against."),
                file, fileConfiguration, "weakTextColor", "&9", false);
        resistTextColor = ConfigurationEngine.setString(
                List.of("Sets the prefix added to damage indicators when players hit a boss with something that boss is strong against."),
                file, fileConfiguration, "resistTextColor", "&c", false);
        weakText = ConfigurationEngine.setString(
                List.of("Sets the message that appears when players hit the boss with something that boss is weak against."),
                file, fileConfiguration, "weakText", "&9&lWeak!", true);
        resistText = ConfigurationEngine.setString(
                List.of("Sets the message that appears when players hit the boss with something that boss is strong against."),
                file, fileConfiguration, "resistText", "&c&lResist!", true);
        doWeakEffect = ConfigurationEngine.setBoolean(
                List.of("Sets if visuals will be used to show that a boss is weak against an attack."),
                fileConfiguration, "doWeakEffect", true);
        doResistEffect = ConfigurationEngine.setBoolean(
                List.of("Sets if visuals will be used to show that a boss is strong against an attack."),
                fileConfiguration, "doResistEffect", true);
        normalizedDamageToEliteMultiplier = ConfigurationEngine.setDouble(
                List.of("Sets the multiplier for the damage dealt to bosses using the normalized damage system (regional dungeon bosses). Higher values increase the damage dealt, making bosses easier to kill.", "2.0 = 200%, 0.5 = 50%"),
                fileConfiguration, "damageToEliteMobMultiplier", 1);
        normalizedDamageToPlayerMultiplier = ConfigurationEngine.setDouble(
                List.of("Sets the multiplier for the damage dealt to players by bosses using the normalized damage system (regional dungeon bosses). Higher values increase the amount of damage dealt by bosses, making bosses hit harder.", "2.0 = 200%, 0.5 = 50%"),
                fileConfiguration, "damageToPlayerMultiplier", 1);
        normalizedBaselineDamage = ConfigurationEngine.setDouble(
                List.of("Sets the baseline damage for custom bosses using the normalized damage (usually regional bosses)."),
                fileConfiguration, "normalizedRegionalBossBaselineDamageV2", 3);
        normalizedBaselineHealth = ConfigurationEngine.setDouble(
                List.of("Sets the baseline health for custom bosses using the normalized health (usually regional bosses)."),
                fileConfiguration, "normalizedRegionalBossBaselineHealthV3", 4);
        normalizeRegionalBosses = ConfigurationEngine.setBoolean(
                List.of("Sets if regional bosses will used the normalized combat system.", "This is very strongly recommended, and premade content will not be balanced properly if modified."),
                fileConfiguration, "normalizeRegionalBosses", true);
        fullHealMessage = ConfigurationEngine.setString(
                List.of("Sets the message that appears when a boss heals from going out of combat."),
                file, fileConfiguration, "fullHealMessage", "&2FULL HEAL!", true);
        strengthAndWeaknessDamageMultipliers = ConfigurationEngine.setDouble(
                List.of("Sets the multipliers applied to attacks bosses are strong and weak against."),
                fileConfiguration, "strengthAndWeaknessDamageMultipliers", 2D);
        resistanceDamageMultiplier = ConfigurationEngine.setDouble(
                List.of("Sets the multiplier applied to damage reduction from the resistance potion effect for players."),
                fileConfiguration, "resistanceDamageMultiplier", 1);
        blockingDamageReduction = ConfigurationEngine.setDouble(
                List.of("Sets the multiplier applied to damage reduction when a player is holding up a shield for melee attacks (powers excluded)."),
                fileConfiguration, "blockingDamageReduction", 0.8);
    }
}
