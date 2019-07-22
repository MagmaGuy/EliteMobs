package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

public class ItemsDropSettingsConfig {

    public static final String CONFIG_NAME = "ItemsDropSettings.yml";

    public static final String ELITE_ITEM_FLAT_DROP_RATE = "EliteMob base percentual plugin item drop chance";
    public static final String ELITE_ITEM_TIER_DROP_RATE = "EliteMob plugin item percentual drop chance increase per tier";
    public static final String PROCEDURAL_ITEM_WEIGHT = "Procedurally generated item weight";
    public static final String WEIGHED_ITEM_WEIGHT = "Weighed item weight";
    public static final String FIXED_ITEM_WEIGHT = "Fixed item weight";
    public static final String LIMITED_ITEM_WEIGHT = "Limited item weight";
    public static final String SCALABLE_ITEM_WEIGHT = "Scalable item weight";
    public static final String SPAWNER_DEFAULT_LOOT_MULTIPLIER = "Drop multiplied default loot from elite mobs spawned in spawners";
    public static final String DEFAULT_LOOT_MULTIPLIER = "EliteMob default loot multiplier";
    public static final String MAXIMUM_LEVEL_FOR_LOOT_MULTIPLIER = "Maximum level of the Elite Mob default multiplier";
    public static final String EXPERIENCE_LOOT_MULTIPLIER = "EliteMob xp multiplier";
    public static final String MAXIMUM_LOOT_TIER = "Maximum loot tier (REQUIRES INCREASING SHARPNESS AND PROTECTION ENCHANTMENTS TO WORK PROPERLY, READ GITHUB WIKI!)";
    public static final String ENABLE_CUSTOM_ENCHANTMENT_SYSTEM = "Enable visual enchantment system (check the github wiki before changing)";
    public static final String ENABLE_RARE_DROP_EFFECT = "Enable rare drop visual effect";
    public static final String HOES_AS_WEAPONS = "Enable hoes as weapons";

    public static final String ELITE_ENCHANTMENT_NAME = "Elite Enchantment name";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(ELITE_ITEM_FLAT_DROP_RATE, 025.00);
        configuration.addDefault(ELITE_ITEM_TIER_DROP_RATE, 005.00);
        configuration.addDefault(PROCEDURAL_ITEM_WEIGHT, 90);
        configuration.addDefault(WEIGHED_ITEM_WEIGHT, 1);
        configuration.addDefault(FIXED_ITEM_WEIGHT, 10);
        configuration.addDefault(LIMITED_ITEM_WEIGHT, 3);
        configuration.addDefault(SCALABLE_ITEM_WEIGHT, 6);
        configuration.addDefault(SPAWNER_DEFAULT_LOOT_MULTIPLIER, true);
        configuration.addDefault(DEFAULT_LOOT_MULTIPLIER, 0.0);
        configuration.addDefault(MAXIMUM_LEVEL_FOR_LOOT_MULTIPLIER, 200);
        configuration.addDefault(EXPERIENCE_LOOT_MULTIPLIER, 1.0);
        configuration.addDefault(MAXIMUM_LOOT_TIER, 5);
        configuration.addDefault(ENABLE_CUSTOM_ENCHANTMENT_SYSTEM, true);
        configuration.addDefault(ELITE_ENCHANTMENT_NAME, "&6Elite");
        configuration.addDefault(ENABLE_RARE_DROP_EFFECT, true);

        configuration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
