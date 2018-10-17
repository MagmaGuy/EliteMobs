package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

public class CustomEnchantmentsConfig{

    public static final String CONFIG_NAME = "CustomEnchantments.yml";
    private CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    private Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public static String HUNTER_NAME = "HunterEnchantment enchantment name";
    public static String HUNTER_SPAWN_BONUS = "Percentual elite mob increase in spawn rate around the player per hunter enchantment level";
    public static String FLAMETHROWER_NAME = "FlamethrowerEnchantment enchantment name";

    public void initializeConfig() {

        configuration.addDefault(HUNTER_NAME, "HunterEnchantment");
        configuration.addDefault(HUNTER_SPAWN_BONUS, 2);
        configuration.addDefault(FLAMETHROWER_NAME, "FlamethrowerEnchantment");

        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
