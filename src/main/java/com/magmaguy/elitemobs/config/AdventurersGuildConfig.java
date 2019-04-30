package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

public class AdventurersGuildConfig {

    public static final String CONFIG_NAME = "AdventurersGuild.yml";

    public static final String ENABLE_ADVENTURERS_GUILD = "Enable adventurer's guild";
    public static final String ADD_MAX_HEALTH = "Add max health when unlocking higher guild ranks";
    public static final String GUILD_WORLD_NAME = "Adventurer's Guild world name";
    public static final String GUILD_WORLD_LOCATION = "Guild world coordinates";
    public static final String AG_TELEPORT = "Teleport players to the adventurers guild using /ag";
    private static final String RANK_NAMES = "Adventurers Guild Rank Names";
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
    public static final String RANK_NAMES_12 = RANK_NAMES + "12";
    public static final String RANK_NAMES_13 = RANK_NAMES + "13";
    public static final String RANK_NAMES_14 = RANK_NAMES + "14";
    public static final String RANK_NAMES_15 = RANK_NAMES + "15";
    public static final String RANK_NAMES_16 = RANK_NAMES + "16";
    public static final String RANK_NAMES_17 = RANK_NAMES + "17";
    public static final String RANK_NAMES_18 = RANK_NAMES + "18";
    public static final String RANK_NAMES_19 = RANK_NAMES + "19";
    public static final String RANK_NAMES_20 = RANK_NAMES + "20";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(ENABLE_ADVENTURERS_GUILD, true);
        configuration.addDefault(ADD_MAX_HEALTH, true);
        configuration.addDefault(GUILD_WORLD_NAME, "EliteMobs_adventurers_guild");
        configuration.addDefault(GUILD_WORLD_LOCATION, "208.5,88,236.5,-80,0");
        configuration.addDefault(AG_TELEPORT, true);
        configuration.addDefault(RANK_NAMES_1, "&8Peaceful Villager");
        configuration.addDefault(RANK_NAMES_2, "&8Commoner");
        configuration.addDefault(RANK_NAMES_3, "&8Farmer");
        configuration.addDefault(RANK_NAMES_4, "&8Bard");
        configuration.addDefault(RANK_NAMES_5, "&8Barkeep");
        configuration.addDefault(RANK_NAMES_6, "&8Blacksmith");
        configuration.addDefault(RANK_NAMES_7, "&8Merchant");
        configuration.addDefault(RANK_NAMES_8, "&8Wanderer");
        configuration.addDefault(RANK_NAMES_9, "&8Ranger");
        configuration.addDefault(RANK_NAMES_10, "&fCasual Adventurer");
        configuration.addDefault(RANK_NAMES_11, "&fAdventurer");
        configuration.addDefault(RANK_NAMES_12, "&fProfessional Adventurer");
        configuration.addDefault(RANK_NAMES_13, "&2Elite Adventurer");
        configuration.addDefault(RANK_NAMES_14, "&2Master Adventurer");
        configuration.addDefault(RANK_NAMES_15, "&2Bloodhound");
        configuration.addDefault(RANK_NAMES_16, "&1Slayer");
        configuration.addDefault(RANK_NAMES_17, "&1Exterminator");
        configuration.addDefault(RANK_NAMES_18, "&5&lElite Hunter");
        configuration.addDefault(RANK_NAMES_19, "&5Hero");
        configuration.addDefault(RANK_NAMES_20, "&6&l&oLegend");

        configuration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
