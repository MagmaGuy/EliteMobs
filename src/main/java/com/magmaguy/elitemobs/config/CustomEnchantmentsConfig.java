package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

public class CustomEnchantmentsConfig{

    public static final String CONFIG_NAME = "CustomEnchantments.yml";
    private CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    private Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public static String HUNTER_NAME = "Hunter enchantment name";
    public static String HUNTER_SPAWN_BONUS = "Percentual elite mob increase in spawn rate around the player per hunter enchantment level";

    public void initializeConfig() {

        configuration.addDefault(HUNTER_NAME, "Hunter");
        configuration.addDefault(HUNTER_SPAWN_BONUS, 2);

        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
