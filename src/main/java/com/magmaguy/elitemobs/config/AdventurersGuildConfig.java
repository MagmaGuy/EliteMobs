package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

public class AdventurersGuildConfig {

    public static final String CONFIG_NAME = "AdventurersGuild.yml";

    public static final String ENABLE_ADVENTURERS_GUILD = "Enable adventurer's guild";
    public static final String ADD_MAX_HEALTH = "Add max health when unlocking higher guild ranks";
    public static final String GUILD_WORLD_NAME = "Adventurer's Guild world name";
    public static final String GUILD_WORLD_LOCATION = "Guild world coordinates";
    public static final String AG_TELEPORT = "Teleport players to the adventurers guild using /ag";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig(){

        configuration.addDefault(ENABLE_ADVENTURERS_GUILD, true);
        configuration.addDefault(ADD_MAX_HEALTH, true);
        configuration.addDefault(GUILD_WORLD_NAME, "EliteMobs_adventurers_guild");
        configuration.addDefault(GUILD_WORLD_LOCATION, "208.5,88,236.5,-80,0");
        configuration.addDefault(AG_TELEPORT, true);

        configuration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
