package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ValidMobsConfig {

    public static final String ALLOW_AGGRESSIVE_ELITEMOBS = "Allow aggressive EliteMobs";
    public static final String ALLOW_PASSIVE_SUPERMOBS = "Allow Passive SuperMobs";
    public static final String VALID_SUPERMOBS = "Valid Super Mobs.";
    public static final String CHICKEN = VALID_SUPERMOBS + "CHICKEN";
    public static final String COW = VALID_SUPERMOBS + "COW";
    public static final String MUSHROOM_COW = VALID_SUPERMOBS + "MUSHROOM_COW";
    public static final String PIG = VALID_SUPERMOBS + "PIG";
    public static final String SHEEP = VALID_SUPERMOBS + "SHEEP";
    public static final String VALID_AGGRESSIVE_ELITEMOBS = "Valid aggressive EliteMobs.";
    public static final String BLAZE = VALID_AGGRESSIVE_ELITEMOBS + "BLAZE";
    public static final String CAVE_SPIDER = VALID_AGGRESSIVE_ELITEMOBS + "CAVE_SPIDER";
    public static final String CREEPER = VALID_AGGRESSIVE_ELITEMOBS + "CREEPER";
    public static final String DROWNED = VALID_AGGRESSIVE_ELITEMOBS + "DROWNED";
    public static final String ENDERMAN = VALID_AGGRESSIVE_ELITEMOBS + "ENDERMAN";
    public static final String ENDERMITE = VALID_AGGRESSIVE_ELITEMOBS + "ENDERMITE";
    public static final String HUSK = VALID_AGGRESSIVE_ELITEMOBS + "HUSK";
    public static final String IRON_GOLEM = VALID_AGGRESSIVE_ELITEMOBS + "IRON_GOLEM";
    public static final String PIG_ZOMBIE = VALID_AGGRESSIVE_ELITEMOBS + "PIG_ZOMBIE";
    public static final String PHANTOM = VALID_AGGRESSIVE_ELITEMOBS + "PHANTOM";
    public static final String POLAR_BEAR = VALID_AGGRESSIVE_ELITEMOBS + "POLAR_BEAR";
    public static final String SILVERFISH = VALID_AGGRESSIVE_ELITEMOBS + "SILVERFISH";
    public static final String STRAY = VALID_AGGRESSIVE_ELITEMOBS + "STRAY";
    public static final String SKELETON = VALID_AGGRESSIVE_ELITEMOBS + "SKELETON";
    public static final String WITHER_SKELETON = VALID_AGGRESSIVE_ELITEMOBS + "WITHER_SKELETON";
    public static final String SPIDER = VALID_AGGRESSIVE_ELITEMOBS + "SPIDER";
    public static final String VEX = VALID_AGGRESSIVE_ELITEMOBS + "VEX";
    public static final String VINDICATOR = VALID_AGGRESSIVE_ELITEMOBS + "VINDICATOR";
    public static final String WITCH = VALID_AGGRESSIVE_ELITEMOBS + "WITCH";
    public static final String ZOMBIE = VALID_AGGRESSIVE_ELITEMOBS + "ZOMBIE";

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("ValidMobs.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        fileConfiguration.addDefault(ALLOW_AGGRESSIVE_ELITEMOBS, true);
        fileConfiguration.addDefault(BLAZE, true);
        fileConfiguration.addDefault(CAVE_SPIDER, true);
        fileConfiguration.addDefault(CREEPER, true);
        fileConfiguration.addDefault(DROWNED, true);
        fileConfiguration.addDefault(ENDERMAN, true);
        fileConfiguration.addDefault(ENDERMITE, true);
        fileConfiguration.addDefault(HUSK, true);
        fileConfiguration.addDefault(IRON_GOLEM, true);
        fileConfiguration.addDefault(PIG_ZOMBIE, true);
        fileConfiguration.addDefault(PHANTOM, true);
        fileConfiguration.addDefault(POLAR_BEAR, true);
        fileConfiguration.addDefault(SILVERFISH, true);
        fileConfiguration.addDefault(SKELETON, true);
        fileConfiguration.addDefault(WITHER_SKELETON, true);
        fileConfiguration.addDefault(SPIDER, true);
        fileConfiguration.addDefault(STRAY, true);
        fileConfiguration.addDefault(VEX, true);
        fileConfiguration.addDefault(VINDICATOR, true);
        fileConfiguration.addDefault(WITCH, true);
        fileConfiguration.addDefault(ZOMBIE, true);
        fileConfiguration.addDefault(ALLOW_PASSIVE_SUPERMOBS, true);
        fileConfiguration.addDefault(CHICKEN, true);
        fileConfiguration.addDefault(COW, true);
        fileConfiguration.addDefault(MUSHROOM_COW, true);
        fileConfiguration.addDefault(PIG, true);
        fileConfiguration.addDefault(SHEEP, true);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
