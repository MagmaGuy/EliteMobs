package com.magmaguy.elitemobs.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

public class AdventurersGuildConfig {

    public static final String ENABLE_ADVENTURERS_GUILD = "Enable adventurer's guild";
    public static final String ADD_MAX_HEALTH = "Add max health when unlocking higher guild ranks";
    public static final String GUILD_WORLD_NAME = "Adventurer's Guild world name";
    public static final String GUILD_WORLD_LOCATION = "Guild world coordinates";
    public static final String AG_TELEPORT = "Teleport players to the adventurers guild using /ag";
    private static final String RANK_NAMES = "Adventurers Guild Rank Names";
    public static final String RANK_NAMES_0 = RANK_NAMES + "0";
    public static final String RANK_NAMES_1 = RANK_NAMES + "1";
    public static final String RANK_NAMES_2 = RANK_NAMES + "2";
    public static final String RANK_NAMES_3 = RANK_NAMES + "3";
    public static final String RANK_NAMES_4 = RANK_NAMES + "4";
    public static final String RANK_NAMES_5 = RANK_NAMES + "5";
    public static final String RANK_NAMES_6 = RANK_NAMES + "6";
    public static final String RANK_NAMES_7 = RANK_NAMES + "7";
    public static final String RANK_NAMES_8 = RANK_NAMES + "8";
    public static final String RANK_NAMES_9 = RANK_NAMES + "9";
    public static final String RANK_NAMES_10 = RANK_NAMES + "10";
    public static final String RANK_NAMES_11 = RANK_NAMES + "11";

    private static FileConfiguration fileConfiguration;
    private static File file;

    public static FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    public static boolean getBoolean(String entry) {
        return fileConfiguration.getBoolean(entry);
    }

    public static String getString(String entry) {
        return fileConfiguration.getString(entry);
    }

    public static List<String> getStringList(String entry) {
        return fileConfiguration.getStringList(entry);
    }

    public static int getInt(String entry) {
        return fileConfiguration.getInt(entry);
    }

    public static double getDouble(String entry) {
        return fileConfiguration.getDouble(entry);
    }

    public static File getFile() {
        return file;
    }

    public static void saveConfig() {
        try {
            fileConfiguration.save(file);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[EliteMobs] Error saving configuration " + file.getName() + "! Report this to the developer!");
        }
    }

    public static void initializeConfig() {
        file = EliteConfigGenerator.getFile("AdventurersGuild.yml");
        fileConfiguration = EliteConfigGenerator.getFileConfiguration(file);

        fileConfiguration.addDefault(ENABLE_ADVENTURERS_GUILD, true);
        fileConfiguration.addDefault(ADD_MAX_HEALTH, true);
        fileConfiguration.addDefault(GUILD_WORLD_NAME, "EliteMobs_adventurers_guild");
        fileConfiguration.addDefault(GUILD_WORLD_LOCATION, "208.5,88,236.5,-80,0");
        fileConfiguration.addDefault(AG_TELEPORT, true);
        fileConfiguration.addDefault(RANK_NAMES_0, "&8Peaceful Villager");
        fileConfiguration.addDefault(RANK_NAMES_1, "&fCasual Adventurer");
        fileConfiguration.addDefault(RANK_NAMES_2, "&fAdventurer");
        fileConfiguration.addDefault(RANK_NAMES_3, "&fProfessional Adventurer");
        fileConfiguration.addDefault(RANK_NAMES_4, "&2Elite Adventurer");
        fileConfiguration.addDefault(RANK_NAMES_5, "&2Master Adventurer");
        fileConfiguration.addDefault(RANK_NAMES_6, "&2Bloodhound");
        fileConfiguration.addDefault(RANK_NAMES_7, "&1Slayer");
        fileConfiguration.addDefault(RANK_NAMES_8, "&1Exterminator");
        fileConfiguration.addDefault(RANK_NAMES_9, "&5&lElite Hunter");
        fileConfiguration.addDefault(RANK_NAMES_10, "&5Hero");
        fileConfiguration.addDefault(RANK_NAMES_11, "&6&l&oLegend");

        EliteConfigGenerator.saveDefaults(file, fileConfiguration);
    }

}
