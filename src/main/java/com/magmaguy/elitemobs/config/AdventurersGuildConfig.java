package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class AdventurersGuildConfig {

    public static boolean enableAdventurersGuild;
    public static boolean addMaxHealth;
    public static String guildWorldName;
    public static String guildWorldLocation;
    public static boolean agTeleport;
    private static String RANK_NAMES = "Adventurers Guild Rank Names";
    public static String rankNames0 = RANK_NAMES + "0";
    public static String rankNames1 = RANK_NAMES + "1";
    public static String rankNames2 = RANK_NAMES + "2";
    public static String rankNames3 = RANK_NAMES + "3";
    public static String rankNames4 = RANK_NAMES + "4";
    public static String rankNames5 = RANK_NAMES + "5";
    public static String rankNames6 = RANK_NAMES + "6";
    public static String rankNames7 = RANK_NAMES + "7";
    public static String rankNames8 = RANK_NAMES + "8";
    public static String rankNames9 = RANK_NAMES + "9";
    public static String rankNames10 = RANK_NAMES + "10";
    public static String rankNames11 = RANK_NAMES + "11";

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("AdventurersGuild.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        enableAdventurersGuild = ConfigurationEngine.setBoolean(fileConfiguration, "Enable adventurer's guild", true);
        addMaxHealth = ConfigurationEngine.setBoolean(fileConfiguration, "Add max health when unlocking higher guild ranks", true);
        guildWorldName = ConfigurationEngine.setString(fileConfiguration, "Adventurer's Guild world name", "EliteMobs_adventurers_guild");
        guildWorldLocation = ConfigurationEngine.setString(fileConfiguration, "Guild world coordinates", "208.5,88,236.5,-80,0");
        agTeleport = ConfigurationEngine.setBoolean(fileConfiguration, "Teleport players to the adventurers guild using /ag", true);
        rankNames0 = ConfigurationEngine.setString(fileConfiguration, "0", "&8Peaceful Villager");
        rankNames1 = ConfigurationEngine.setString(fileConfiguration, "1", "&fCasual Adventurer");
        rankNames2 = ConfigurationEngine.setString(fileConfiguration, "2", "&fAdventurer");
        rankNames3 = ConfigurationEngine.setString(fileConfiguration, "3", "&fProfessional Adventurer");
        rankNames4 = ConfigurationEngine.setString(fileConfiguration, "4", "&2Elite Adventurer");
        rankNames5 = ConfigurationEngine.setString(fileConfiguration, "5", "&2Master Adventurer");
        rankNames6 = ConfigurationEngine.setString(fileConfiguration, "6", "&2Bloodhound");
        rankNames7 = ConfigurationEngine.setString(fileConfiguration, "7", "&1Slayer");
        rankNames8 = ConfigurationEngine.setString(fileConfiguration, "8", "&1Exterminator");
        rankNames9 = ConfigurationEngine.setString(fileConfiguration, "9", "&5&lElite Hunter");
        rankNames10 = ConfigurationEngine.setString(fileConfiguration, "10", "&5Hero");
        rankNames11 = ConfigurationEngine.setString(fileConfiguration, "11", "&6&l&oLegend");

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
