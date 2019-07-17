package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.items.customenchantments.CustomEnchantmentCache;
import org.bukkit.configuration.Configuration;

public class ItemsDropSettingsConfig {

    public static final String CONFIG_NAME = "ItemsDropSettings.yml";

    public static final String ENABLE_PLUGIN_LOOT = "Enable plugin loot";
    public static final String DROP_CUSTOM_ITEMS = "Elite Mobs can drop custom loot";
    public static final String LORE_WORTH = "Item worth";
    public static final String LORE_RESALE_WORTH = "Item resale worth";
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
    public static final String POTION_EFFECT_NAME = "Potion effect name.";
    public static final String ABSORPTION_NAME = POTION_EFFECT_NAME + "ABSORPTION";
    public static final String BLINDNESS_NAME = POTION_EFFECT_NAME + "BLINDNESS";
    public static final String CONDUIT_POWER_NAME = POTION_EFFECT_NAME + "CONDUIT_POWER";
    public static final String CONFUSION_NAME = POTION_EFFECT_NAME + "CONFUSION";
    public static final String DAMAGE_RESISTANCE_NAME = POTION_EFFECT_NAME + "DAMAGE_RESISTANCE";
    public static final String DOLPHINS_GRACE_NAME = POTION_EFFECT_NAME + "DOLPHINS_GRACE";
    public static final String FAST_DIGGING_NAME = POTION_EFFECT_NAME + "FAST_DIGGING";
    public static final String FIRE_RESISTANCE_NAME = POTION_EFFECT_NAME + "FIRE_RESISTANCE";
    public static final String GLOWING_NAME = POTION_EFFECT_NAME + "GLOWING";
    public static final String HARM_NAME = POTION_EFFECT_NAME + "HARM";
    public static final String HEAL_NAME = POTION_EFFECT_NAME + "HEAL";
    public static final String HEAL_BOOST_NAME = POTION_EFFECT_NAME + "HEAL_BOOST";
    public static final String HUNGER_NAME = POTION_EFFECT_NAME + "HUNGER";
    public static final String INCREASE_DAMAGE_NAME = POTION_EFFECT_NAME + "INCREASE_DAMAGE";
    public static final String INVISIBILITY_NAME = POTION_EFFECT_NAME + "INVISIBILITY";
    public static final String JUMP_NAME = POTION_EFFECT_NAME + "JUMP";
    public static final String LEVITATION_NAME = POTION_EFFECT_NAME + "LEVITATION";
    public static final String LUCK_POTION_NAME = POTION_EFFECT_NAME + "LUCK";
    public static final String NIGHT_VISION_NAME = POTION_EFFECT_NAME + "NIGHT_VISION";
    public static final String POISON_NAME = POTION_EFFECT_NAME + "POISON";
    public static final String REGENERATION_NAME = POTION_EFFECT_NAME + "REGENERATION";
    public static final String SATURATION_NAME = POTION_EFFECT_NAME + "SATURATION";
    public static final String SLOW_NAME = POTION_EFFECT_NAME + "SLOW";
    public static final String SLOW_DIGGING_NAME = POTION_EFFECT_NAME + "SLOW_DIGGING";
    public static final String SLOW_FALLING_NAME = POTION_EFFECT_NAME + "SLOW_FALLING";
    public static final String SPEED_NAME = POTION_EFFECT_NAME + "SPEED";
    public static final String UNLUCK_NAME = POTION_EFFECT_NAME + "UNLUCK";
    public static final String WATER_BREATHING_NAME = POTION_EFFECT_NAME + "WATER_BREATHING";
    public static final String WEAKNESS_NAME = POTION_EFFECT_NAME + "WEAKNESS";
    public static final String WITHER_NAME = POTION_EFFECT_NAME + "WITHER";
    public static final String ENABLE_RARE_DROP_EFFECT = "Enable rare drop visual effect";
    public static final String HOES_AS_WEAPONS = "Enable hoes as weapons";


    /*
    Custom enchantments
     */
    public static final String CUSTOM_ENCHANTMENT_NAME = "Custom Enchantment name.";
    public static final String FLAMETHROWER_NAME = CUSTOM_ENCHANTMENT_NAME + CustomEnchantmentCache.flamethrowerEnchantment.getKey();
    public static final String HUNTER_NAME = CUSTOM_ENCHANTMENT_NAME + CustomEnchantmentCache.hunterEnchantment.getKey();

    public static final String ELITE_ENCHANTMENT_NAME = "Elite Enchantment name";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(ENABLE_PLUGIN_LOOT, true);
        configuration.addDefault(DROP_CUSTOM_ITEMS, true);
        configuration.addDefault(LORE_WORTH, "Worth $worth $currencyName");
        configuration.addDefault(LORE_RESALE_WORTH, "$resale $currencyName resale value");
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
        configuration.addDefault(ABSORPTION_NAME, "Absorption");
        configuration.addDefault(BLINDNESS_NAME, "Blindness");
        configuration.addDefault(CONDUIT_POWER_NAME, "Conduit Power");
        configuration.addDefault(CONFUSION_NAME, "Nausea");
        configuration.addDefault(DAMAGE_RESISTANCE_NAME, "Resistance");
        configuration.addDefault(DOLPHINS_GRACE_NAME, "Dolphin's grace");
        configuration.addDefault(FAST_DIGGING_NAME, "Haste");
        configuration.addDefault(FIRE_RESISTANCE_NAME, "Fire Resistance");
        configuration.addDefault(GLOWING_NAME, "Glowing");
        configuration.addDefault(HARM_NAME, "Instant Damage");
        configuration.addDefault(HEAL_NAME, "Instant Health");
        configuration.addDefault(HEAL_BOOST_NAME, "Health Boost");
        configuration.addDefault(HUNGER_NAME, "Hunger");
        configuration.addDefault(INCREASE_DAMAGE_NAME, "Strength");
        configuration.addDefault(INVISIBILITY_NAME, "Invisibility");
        configuration.addDefault(JUMP_NAME, "Jump Boost");
        configuration.addDefault(LEVITATION_NAME, "Levitation");
        configuration.addDefault(LUCK_POTION_NAME, "Luck");
        configuration.addDefault(NIGHT_VISION_NAME, "Night Vision");
        configuration.addDefault(POISON_NAME, "Poison");
        configuration.addDefault(REGENERATION_NAME, "Regeneration");
        configuration.addDefault(SATURATION_NAME, "Saturation");
        configuration.addDefault(SLOW_NAME, "Slowness");
        configuration.addDefault(SLOW_DIGGING_NAME, "Mining Fatigue");
        configuration.addDefault(SLOW_FALLING_NAME, "Slow Falling");
        configuration.addDefault(SPEED_NAME, "Speed");
        configuration.addDefault(UNLUCK_NAME, "Bad Luck");
        configuration.addDefault(WATER_BREATHING_NAME, "Water Breathing");
        configuration.addDefault(WEAKNESS_NAME, "Weakness");
        configuration.addDefault(WITHER_NAME, "Wither");

        configuration.addDefault(FLAMETHROWER_NAME, "Flamethrower");
        configuration.addDefault(HUNTER_NAME, "Hunter");

        configuration.addDefault(ELITE_ENCHANTMENT_NAME, "&6Elite");

        configuration.addDefault(ENABLE_RARE_DROP_EFFECT, true);

        configuration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
