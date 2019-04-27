/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.mobconstructor.CombatSystem;
import org.bukkit.configuration.Configuration;

import java.util.Arrays;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class EconomySettingsConfig {

    public static final String CONFIG_NAME = "EconomySettings.yml";
    public static final String ENABLE_ECONOMY = "Enable economy";
    public static final String RESALE_VALUE = "Item resale value (percentage)";
    public static final String LOWEST_PROCEDURALLY_SIMULATED_LOOT = "Procedurally Generated Loot.Lowest shop item tier";
    public static final String HIGHEST_PROCEDURALLY_SIMULATED_LOOT = "Procedurally Generated Loot.Highest shop item tier";
    public static final String CURRENCY_NAME = "Currency name";
    public static final String SHOP_NAME = "Shop name";
    public static final String CUSTOM_SHOP_NAME = "Custom shop name";
    public static final String BUY_OR_SELL_SHOP_NAME = "Buy or sell shop name";
    public static final String SIGNATURE_ITEM_LOCATION_SHOPS = "Reroll button location for EliteMobs Shops";
    public static final String SHOP_VALID_SLOTS = "Valid chest slots for EliteMobs Shop";
    public static final String CUSTOM_SHOP_VALID_SLOTS = "Valid chest slots for EliteMobs Custom Shop";
    public static final String SELL_SHOP_NAME = "Sell shop name";
    public static final String SELL_SHOP_VALID_SLOTS = "Valid chest slots for EliteMobs Sell Shop";
    public static final String SELL_SHOP_INFO_SLOT = "Sell shop info slot";
    public static final String SELL_SHOP_INFO_NAME = "Sell shop info item name";
    public static final String SELL_SHOP_INFO_LORE = "Sell shop info item lore";
    public static final String SELL_SHOP_CANCEL_SLOT = "Sell shop cancel slot";
    public static final String SELL_SHOP_CANCEL_NAME = "Sell shop cancel name";
    public static final String SELL_SHOP_CANCEL_LORE = "Sell shop cancel lore";
    public static final String SELL_SHOP_CONFIRM_SLOT = "Sell shop confirm slot";
    public static final String SELL_SHOP_CONFIRM_NAME = "Sell shop confirm name";
    public static final String SELL_SHOP_CONFIRM_LORE = "Sell shop confirm lore";

    public static final String USE_VAULT = "Use Vault instead of Elite Coins (BAD IDEA)";

    public static final String CURRENCY_SHOWER_MULTIPLIER = "Tier multiplier for currency payout on Elite Mob death";
    public static final String CURRENCY_LOOT_MESSAGE = "Currency loot message";
    public static final String ENABLE_CURRENCY_SHOWER = "Enable currency shower";
    public static final String CURRENCY_SHOWER_MESSAGE_1 = "Currency shower message for 1 currency";
    public static final String CURRENCY_SHOWER_MESSAGE_5 = "Currency shower message for 5 currency";
    public static final String CURRENCY_SHOWER_MESSAGE_10 = "Currency shower message for 10 currency";
    public static final String CURRENCY_SHOWER_MESSAGE_20 = "Currency shower message for 20 currency";
    public static final String CURRENCY_AG_NOTIFICATION = "Adventurers Guild notification message";

    private static final String MATERIAL_WORTH = "Material worth.";
    public static final String DIAMOND_AXE = MATERIAL_WORTH + "DIAMOND_AXE";
    public static final String DIAMOND_BARDING = MATERIAL_WORTH + "DIAMOND_BARDING";
    public static final String DIAMOND_BLOCK = MATERIAL_WORTH + "DIAMOND_BLOCK";
    public static final String DIAMOND_BOOTS = MATERIAL_WORTH + "DIAMOND_BOOTS";
    public static final String DIAMOND_CHESTPLATE = MATERIAL_WORTH + "DIAMOND_CHESTPLATE";
    public static final String DIAMOND_HELMET = MATERIAL_WORTH + "DIAMOND_HELMET";
    public static final String DIAMOND_HOE = MATERIAL_WORTH + "DIAMOND_HOE";
    public static final String DIAMOND_LEGGINGS = MATERIAL_WORTH + "DIAMOND_LEGGINGS";
    public static final String DIAMOND_ORE = MATERIAL_WORTH + "DIAMOND_ORE";
    public static final String DIAMOND_PICKAXE = MATERIAL_WORTH + "DIAMOND_PICKAXE";
    public static final String DIAMOND_SPADE = MATERIAL_WORTH + "DIAMOND_SPADE";
    public static final String DIAMOND_SWORD = MATERIAL_WORTH + "DIAMOND_SWORD";
    public static final String IRON_AXE = MATERIAL_WORTH + "IRON_AXE";
    public static final String IRON_BARDING = MATERIAL_WORTH + "IRON_BARDING";
    public static final String IRON_BLOCK = MATERIAL_WORTH + "IRON_BLOCK";
    public static final String IRON_BOOTS = MATERIAL_WORTH + "IRON_BOOTS";
    public static final String IRON_CHESTPLATE = MATERIAL_WORTH + "IRON_CHESTPLATE";
    public static final String IRON_HELMET = MATERIAL_WORTH + "IRON_HELMET";
    public static final String IRON_HOE = MATERIAL_WORTH + "IRON_HOE";
    public static final String IRON_INGOT = MATERIAL_WORTH + "IRON_INGOT";
    public static final String IRON_LEGGINGS = MATERIAL_WORTH + "IRON_LEGGINGS";
    public static final String IRON_NUGGET = MATERIAL_WORTH + "IRON_NUGGET";
    public static final String IRON_ORE = MATERIAL_WORTH + "IRON_ORE";
    public static final String IRON_PICKAXE = MATERIAL_WORTH + "IRON_PICKAXE";
    public static final String IRON_SPADE = MATERIAL_WORTH + "IRON_SPADE";
    public static final String IRON_SWORD = MATERIAL_WORTH + "IRON_SWORD";
    public static final String SHIELD = MATERIAL_WORTH + "SHIELD";
    public static final String BOW = MATERIAL_WORTH + "BOW";
    public static final String CHAINMAIL_BOOTS = MATERIAL_WORTH + "CHAINMAIL_BOOTS";
    public static final String CHAINMAIL_CHESTPLATE = MATERIAL_WORTH + "CHAINMAIL_CHESTPLATE";
    public static final String CHAINMAIL_HELMET = MATERIAL_WORTH + "CHAINMAIL_HELMET";
    public static final String CHAINMAIL_LEGGINGS = MATERIAL_WORTH + "CHAINMAIL_LEGGINGS";
    public static final String STONE_SWORD = MATERIAL_WORTH + "STONE_SWORD";
    public static final String STONE_SPADE = MATERIAL_WORTH + "STONE_SPADE";
    public static final String STONE_PICKAXE = MATERIAL_WORTH + "STONE_PICKAXE";
    public static final String STONE_AXE = MATERIAL_WORTH + "STONE_AXE";
    public static final String GOLD_AXE = MATERIAL_WORTH + "GOLD_AXE";
    public static final String GOLD_BARDING = MATERIAL_WORTH + "GOLD_BARDING";
    public static final String GOLD_BLOCK = MATERIAL_WORTH + "GOLD_BLOCK";
    public static final String GOLD_BOOTS = MATERIAL_WORTH + "GOLD_BOOTS";
    public static final String GOLD_CHESTPLATE = MATERIAL_WORTH + "GOLD_CHESTPLATE";
    public static final String GOLD_HELMET = MATERIAL_WORTH + "GOLD_HELMET";
    public static final String GOLD_HOE = MATERIAL_WORTH + "GOLD_HOE";
    public static final String GOLD_INGOT = MATERIAL_WORTH + "GOLD_INGOT";
    public static final String GOLD_LEGGINGS = MATERIAL_WORTH + "GOLD_LEGGINGS";
    public static final String GOLD_NUGGET = MATERIAL_WORTH + "GOLD_NUGGET";
    public static final String GOLD_ORE = MATERIAL_WORTH + "GOLD_ORE";
    public static final String GOLD_PICKAXE = MATERIAL_WORTH + "GOLD_PICKAXE";
    public static final String GOLD_SPADE = MATERIAL_WORTH + "GOLD_SPADE";
    public static final String GOLD_SWORD = MATERIAL_WORTH + "GOLD_SWORD";
    public static final String GOLDEN_APPLE = MATERIAL_WORTH + "GOLDEN_APPLE";
    public static final String GOLDEN_CARROT = MATERIAL_WORTH + "GOLDEN_CARROT";
    public static final String LEATHER_BOOTS = MATERIAL_WORTH + "LEATHER_BOOTS";
    public static final String LEATHER_CHESTPLATE = MATERIAL_WORTH + "LEATHER_CHESTPLATE";
    public static final String LEATHER_HELMET = MATERIAL_WORTH + "LEATHER_HELMET";
    public static final String LEATHER_LEGGINGS = MATERIAL_WORTH + "LEATHER_LEGGINGS";
    public static final String WOOD_SWORD = MATERIAL_WORTH + "WOOD_SWORD";
    public static final String WOOD_AXE = MATERIAL_WORTH + "WOOD_AXE";
    public static final String WOOD_PICKAXE = MATERIAL_WORTH + "WOOD_PICKAXE";
    public static final String WOOD_HOE = MATERIAL_WORTH + "WOOD_HOE";
    public static final String OTHER = MATERIAL_WORTH + "OTHER";

    private static final String ENCHANTMENT_WORTH = "Enchantment worth.";
    public static final String ARROW_DAMAGE = ENCHANTMENT_WORTH + "ARROW_DAMAGE";
    public static final String ARROW_FIRE = ENCHANTMENT_WORTH + "ARROW_FIRE";
    public static final String ARROW_INFINITE = ENCHANTMENT_WORTH + "ARROW_INFINITE";
    public static final String ARROW_KNOCKBACK = ENCHANTMENT_WORTH + "ARROW_KNOCKBACK";
    public static final String BINDING_CURSE = ENCHANTMENT_WORTH + "BINDING_CURSE";
    public static final String DAMAGE_ALL = ENCHANTMENT_WORTH + "DAMAGE_ALL";
    public static final String DAMAGE_ARTHROPODS = ENCHANTMENT_WORTH + "DAMAGE_ARTHROPODS";
    public static final String DAMAGE_UNDEAD = ENCHANTMENT_WORTH + "DAMAGE_UNDEAD";
    public static final String DEPTH_STRIDER = ENCHANTMENT_WORTH + "DEPTH_STRIDER";
    public static final String DIG_SPEED = ENCHANTMENT_WORTH + "DIG_SPEED";
    public static final String DURABILITY = ENCHANTMENT_WORTH + "DURABILITY";
    public static final String FIRE_ASPECT = ENCHANTMENT_WORTH + "FIRE_ASPECT";
    public static final String FROST_WALKER = ENCHANTMENT_WORTH + "FROST_WALKER";
    public static final String KNOCKBACK = ENCHANTMENT_WORTH + "KNOCKBACK";
    public static final String LOOT_BONUS_BLOCKS = ENCHANTMENT_WORTH + "LOOT_BONUS_BLOCKS";
    public static final String LOOT_BONUS_MOBS = ENCHANTMENT_WORTH + "LOOT_BONUS_MOBS";
    public static final String LUCK_ENCHANTMENT = ENCHANTMENT_WORTH + "LUCK";
    public static final String LURE = ENCHANTMENT_WORTH + "LURE";
    public static final String MENDING = ENCHANTMENT_WORTH + "MENDING";
    public static final String OXYGEN = ENCHANTMENT_WORTH + "OXYGEN";
    public static final String PROTECTION_ENVIRONMENTAL = ENCHANTMENT_WORTH + "PROTECTION_ENVIRONMENTAL";
    public static final String PROTECTION_EXPLOSIONS = ENCHANTMENT_WORTH + "PROTECTION_EXPLOSIONS";
    public static final String PROTECTION_FALL = ENCHANTMENT_WORTH + "PROTECTION_FALL";
    public static final String PROTECTION_PROJECTILE = ENCHANTMENT_WORTH + "PROTECTION_PROJECTILE";
    public static final String PROTECTION_FIRE = ENCHANTMENT_WORTH + "PROTECTION_FIRE";
    public static final String SILK_TOUCH = ENCHANTMENT_WORTH + "SILK_TOUCH";
    public static final String SWEEPING_EDGE = ENCHANTMENT_WORTH + "SWEEPING_EDGE";
    public static final String THORNS = ENCHANTMENT_WORTH + "THORNS";
    public static final String VANISHING_CURSE = ENCHANTMENT_WORTH + "VANISHING_CURSE";
    public static final String WATER_WORKER = ENCHANTMENT_WORTH + "WATER_WORKER";

    public static final String FLAMETHROWER = ENCHANTMENT_WORTH + "FLAMETHROWER";
    public static final String HUNTER = ENCHANTMENT_WORTH + "HUNTER";

    private static final String POTION_EFFECT_WORTH = "Potion effect worth.";
    public static final String ABSORPTION = POTION_EFFECT_WORTH + "ABSORPTION";
    public static final String BLINDNESS = POTION_EFFECT_WORTH + "BLINDNESS";
    public static final String CONFUSION = POTION_EFFECT_WORTH + "CONFUSION";
    public static final String DAMAGE_RESISTANCE = POTION_EFFECT_WORTH + "DAMAGE_RESISTANCE";
    public static final String FAST_DIGGING = POTION_EFFECT_WORTH + "FAST_DIGGING";
    public static final String FIRE_RESISTANCE = POTION_EFFECT_WORTH + "FIRE_RESISTANCE";
    public static final String GLOWING = POTION_EFFECT_WORTH + "GLOWING";
    public static final String HARM = POTION_EFFECT_WORTH + "HARM";
    public static final String HEAL = POTION_EFFECT_WORTH + "HEAL";
    public static final String HEALTH_BOOST = POTION_EFFECT_WORTH + "HEALTH_BOOST";
    public static final String HUNGER = POTION_EFFECT_WORTH + "HUNGER";
    public static final String INCREASE_DAMAGE = POTION_EFFECT_WORTH + "INCREASE_DAMAGE";
    public static final String INVISIBILITY = POTION_EFFECT_WORTH + "INVISIBILITY";
    public static final String JUMP = POTION_EFFECT_WORTH + "JUMP";
    public static final String LEVITATION = POTION_EFFECT_WORTH + "LEVITATION";
    public static final String LUCK_POTION = POTION_EFFECT_WORTH + "LUCK";
    public static final String NIGHT_VISION = POTION_EFFECT_WORTH + "NIGHT_VISION";
    public static final String POISON = POTION_EFFECT_WORTH + "POISON";
    public static final String REGENERATION = POTION_EFFECT_WORTH + "REGENERATION";
    public static final String SATURATION = POTION_EFFECT_WORTH + "SATURATION_NAME";
    public static final String SLOW = POTION_EFFECT_WORTH + "SLOW";
    public static final String SLOW_DIGGING = POTION_EFFECT_WORTH + "SLOW_DIGGING";
    public static final String SPEED = POTION_EFFECT_WORTH + "SPEED";
    public static final String UNLUCK = POTION_EFFECT_WORTH + "UNLUCK";
    public static final String WATER_BREATHING = POTION_EFFECT_WORTH + "WATER_BREATHING";
    public static final String WEAKNESS = POTION_EFFECT_WORTH + "WEAKNESS";
    public static final String WITHER = POTION_EFFECT_WORTH + "WITHER";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        double diamondLevel = CombatSystem.DIAMOND_TIER_LEVEL + 10;
        double ironLevel = CombatSystem.IRON_TIER_LEVEL + 10;
        double stoneChainLevel = CombatSystem.STONE_CHAIN_TIER_LEVEL + 10;
        double goldWoodLeatherLevel = CombatSystem.GOLD_WOOD_LEATHER_TIER_LEVEL + 10;

        configuration.addDefault(ENABLE_ECONOMY, true);
        configuration.addDefault(RESALE_VALUE, 5);
        configuration.addDefault(LOWEST_PROCEDURALLY_SIMULATED_LOOT, 1);
        configuration.addDefault(HIGHEST_PROCEDURALLY_SIMULATED_LOOT, 5);
        configuration.addDefault(CURRENCY_NAME, "Elite Coins");
        configuration.addDefault(SHOP_NAME, "[EM] Shop");
        configuration.addDefault(CUSTOM_SHOP_NAME, "[EM] Custom Shop");
        configuration.addDefault(BUY_OR_SELL_SHOP_NAME, "[EM] Buy/Sell");
        configuration.addDefault(SIGNATURE_ITEM_LOCATION_SHOPS, 4);
        configuration.addDefault(SHOP_VALID_SLOTS, Arrays.asList(9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
                19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45,
                46, 47, 48, 49, 50, 51, 52, 53));
        configuration.addDefault(CUSTOM_SHOP_VALID_SLOTS, Arrays.asList(9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
                19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45,
                46, 47, 48, 49, 50, 51, 52, 53));

        configuration.addDefault(SELL_SHOP_NAME, "[EM] Sell Menu");
        configuration.addDefault(SELL_SHOP_INFO_SLOT, 8);
        configuration.addDefault(SELL_SHOP_INFO_NAME, "&cWarning!");
        configuration.addDefault(SELL_SHOP_INFO_LORE, Arrays.asList(
                "&cYou can only sell special",
                "&cElite Mobs drops in this",
                "&cshop! These should have",
                "&ca value on their lore."));
        configuration.addDefault(SELL_SHOP_CANCEL_SLOT, 27);
        configuration.addDefault(SELL_SHOP_CANCEL_NAME, "&4Cancel");
        configuration.addDefault(SELL_SHOP_CANCEL_LORE, Arrays.asList("&cCancel sale!"));
        configuration.addDefault(SELL_SHOP_CONFIRM_SLOT, 35);
        configuration.addDefault(SELL_SHOP_CONFIRM_NAME, "&2Confirm sale!");
        configuration.addDefault(SELL_SHOP_CONFIRM_LORE, Arrays.asList("&aSell items for", "&a$currency_amount $currency_name"));
        configuration.addDefault(SELL_SHOP_VALID_SLOTS, Arrays.asList(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33,
                34, 37, 38, 39, 40, 41, 42, 43));

        configuration.addDefault(USE_VAULT, false);

        configuration.addDefault(CURRENCY_SHOWER_MULTIPLIER, 1);
        configuration.addDefault(CURRENCY_LOOT_MESSAGE, "&7[EM] You've picked up &a$amount $currency_name!");
        configuration.addDefault(ENABLE_CURRENCY_SHOWER, true);
        configuration.addDefault(CURRENCY_SHOWER_MESSAGE_1, "&7[EM] You've picked up 1 $currency_name!");
        configuration.addDefault(CURRENCY_SHOWER_MESSAGE_5, "&7[EM] You've picked up &f5 &7$currency_name!");
        configuration.addDefault(CURRENCY_SHOWER_MESSAGE_10, "&7[EM] You've picked up &a10 &7$currency_name!");
        configuration.addDefault(CURRENCY_SHOWER_MESSAGE_20, "&7[EM] You've picked up &220 &7$currency_name!");
        configuration.addDefault(CURRENCY_AG_NOTIFICATION, "&7[EM] Extra spending money? Try &a/ag &7or &a/em shop&7!");

        configuration.addDefault(DIAMOND_AXE, diamondLevel);
        configuration.addDefault(DIAMOND_BARDING, diamondLevel);
        configuration.addDefault(DIAMOND_BLOCK, diamondLevel);
        configuration.addDefault(DIAMOND_BOOTS, diamondLevel);
        configuration.addDefault(DIAMOND_CHESTPLATE, diamondLevel);
        configuration.addDefault(DIAMOND_HELMET, diamondLevel);
        configuration.addDefault(DIAMOND_HOE, diamondLevel);
        configuration.addDefault(DIAMOND_LEGGINGS, diamondLevel);
        configuration.addDefault(DIAMOND_ORE, diamondLevel);
        configuration.addDefault(DIAMOND_PICKAXE, diamondLevel);
        configuration.addDefault(DIAMOND_SPADE, diamondLevel);
        configuration.addDefault(DIAMOND_SWORD, diamondLevel);
        configuration.addDefault(IRON_AXE, ironLevel);
        configuration.addDefault(IRON_BARDING, ironLevel);
        configuration.addDefault(IRON_BLOCK, ironLevel);
        configuration.addDefault(IRON_BOOTS, ironLevel);
        configuration.addDefault(IRON_CHESTPLATE, ironLevel);
        configuration.addDefault(IRON_HELMET, ironLevel);
        configuration.addDefault(IRON_HOE, ironLevel);
        configuration.addDefault(IRON_INGOT, ironLevel);
        configuration.addDefault(IRON_LEGGINGS, ironLevel);
        configuration.addDefault(IRON_NUGGET, ironLevel);
        configuration.addDefault(IRON_ORE, ironLevel);
        configuration.addDefault(IRON_PICKAXE, ironLevel);
        configuration.addDefault(IRON_SPADE, ironLevel);
        configuration.addDefault(IRON_SWORD, ironLevel);
        configuration.addDefault(SHIELD, ironLevel);
        configuration.addDefault(BOW, ironLevel);
        configuration.addDefault(CHAINMAIL_BOOTS, stoneChainLevel);
        configuration.addDefault(CHAINMAIL_CHESTPLATE, stoneChainLevel);
        configuration.addDefault(CHAINMAIL_HELMET, stoneChainLevel);
        configuration.addDefault(CHAINMAIL_LEGGINGS, stoneChainLevel);
        configuration.addDefault(STONE_SWORD, stoneChainLevel);
        configuration.addDefault(STONE_AXE, stoneChainLevel);
        configuration.addDefault(STONE_PICKAXE, stoneChainLevel);
        configuration.addDefault(STONE_SPADE, stoneChainLevel);
        configuration.addDefault(GOLD_AXE, goldWoodLeatherLevel);
        configuration.addDefault(GOLD_BARDING, goldWoodLeatherLevel);
        configuration.addDefault(GOLD_BLOCK, goldWoodLeatherLevel);
        configuration.addDefault(GOLD_BOOTS, goldWoodLeatherLevel);
        configuration.addDefault(GOLD_CHESTPLATE, goldWoodLeatherLevel);
        configuration.addDefault(GOLD_HELMET, goldWoodLeatherLevel);
        configuration.addDefault(GOLD_HOE, goldWoodLeatherLevel);
        configuration.addDefault(GOLD_INGOT, goldWoodLeatherLevel);
        configuration.addDefault(GOLD_LEGGINGS, goldWoodLeatherLevel);
        configuration.addDefault(GOLD_NUGGET, goldWoodLeatherLevel);
        configuration.addDefault(GOLD_ORE, goldWoodLeatherLevel);
        configuration.addDefault(GOLD_PICKAXE, goldWoodLeatherLevel);
        configuration.addDefault(GOLD_SPADE, goldWoodLeatherLevel);
        configuration.addDefault(GOLD_SWORD, goldWoodLeatherLevel);
        configuration.addDefault(GOLDEN_APPLE, goldWoodLeatherLevel);
        configuration.addDefault(GOLDEN_CARROT, goldWoodLeatherLevel);
        configuration.addDefault(LEATHER_BOOTS, goldWoodLeatherLevel);
        configuration.addDefault(LEATHER_CHESTPLATE, goldWoodLeatherLevel);
        configuration.addDefault(LEATHER_HELMET, goldWoodLeatherLevel);
        configuration.addDefault(LEATHER_LEGGINGS, goldWoodLeatherLevel);
        configuration.addDefault(WOOD_SWORD, goldWoodLeatherLevel);
        configuration.addDefault(WOOD_AXE, goldWoodLeatherLevel);
        configuration.addDefault(WOOD_PICKAXE, goldWoodLeatherLevel);
        configuration.addDefault(WOOD_HOE, goldWoodLeatherLevel);
        configuration.addDefault(OTHER, 1);

        configuration.addDefault(ARROW_DAMAGE, diamondLevel);
        configuration.addDefault(ARROW_FIRE, diamondLevel / 2);
        configuration.addDefault(ARROW_INFINITE, diamondLevel / 2);
        configuration.addDefault(ARROW_KNOCKBACK, diamondLevel / 2);
        configuration.addDefault(BINDING_CURSE, -1);
        configuration.addDefault(DAMAGE_ALL, diamondLevel);
        configuration.addDefault(DAMAGE_ARTHROPODS, diamondLevel / 2);
        configuration.addDefault(DAMAGE_UNDEAD, diamondLevel / 2);
        configuration.addDefault(DEPTH_STRIDER, 1);
        configuration.addDefault(DIG_SPEED, diamondLevel);
        configuration.addDefault(DURABILITY, diamondLevel);
        configuration.addDefault(FIRE_ASPECT, diamondLevel / 2);
        configuration.addDefault(FROST_WALKER, 1);
        configuration.addDefault(KNOCKBACK, diamondLevel / 2);
        configuration.addDefault(LOOT_BONUS_BLOCKS, diamondLevel);
        configuration.addDefault(LOOT_BONUS_MOBS, diamondLevel / 2);
        configuration.addDefault(LUCK_ENCHANTMENT, 1);
        configuration.addDefault(LURE, 1);
        configuration.addDefault(MENDING, diamondLevel * 2);
        configuration.addDefault(OXYGEN, 1);
        configuration.addDefault(PROTECTION_ENVIRONMENTAL, diamondLevel);
        configuration.addDefault(PROTECTION_EXPLOSIONS, diamondLevel / 2);
        configuration.addDefault(PROTECTION_FALL, diamondLevel / 2);
        configuration.addDefault(PROTECTION_FIRE, diamondLevel / 2);
        configuration.addDefault(PROTECTION_PROJECTILE, diamondLevel);
        configuration.addDefault(SILK_TOUCH, diamondLevel);
        configuration.addDefault(SWEEPING_EDGE, diamondLevel);
        configuration.addDefault(THORNS, diamondLevel);
        configuration.addDefault(VANISHING_CURSE, -1);
        configuration.addDefault(WATER_WORKER, 1);

        configuration.addDefault(FLAMETHROWER, 5);
        configuration.addDefault(HUNTER, 3);

        configuration.addDefault(ABSORPTION, (diamondLevel) * 10);
        configuration.addDefault(BLINDNESS, (diamondLevel / 2) * 10);
        configuration.addDefault(CONFUSION, (diamondLevel / 2) * 10);
        configuration.addDefault(DAMAGE_RESISTANCE, (diamondLevel) * 10);
        configuration.addDefault(FAST_DIGGING, (diamondLevel) * 10);
        configuration.addDefault(FIRE_RESISTANCE, (diamondLevel / 2) * 10);
        configuration.addDefault(GLOWING, (1) * 10);
        configuration.addDefault(HARM, (diamondLevel) * 10);
        configuration.addDefault(HEAL, (diamondLevel) * 10);
        configuration.addDefault(HEALTH_BOOST, (diamondLevel) * 10);
        configuration.addDefault(HUNGER, (1) * 10);
        configuration.addDefault(INCREASE_DAMAGE, (diamondLevel) * 10);
        configuration.addDefault(INVISIBILITY, (diamondLevel / 2) * 10);
        configuration.addDefault(JUMP, (diamondLevel / 2) * 10);
        configuration.addDefault(LEVITATION, (diamondLevel / 2) * 10);
        configuration.addDefault(LUCK_POTION, (1) * 10);
        configuration.addDefault(NIGHT_VISION, (1) * 10);
        configuration.addDefault(POISON, (diamondLevel / 2) * 10);
        configuration.addDefault(REGENERATION, (diamondLevel) * 10);
        configuration.addDefault(SATURATION, (diamondLevel / 2) * 10);
        configuration.addDefault(SLOW, (diamondLevel / 2) * 10);
        configuration.addDefault(SLOW_DIGGING, (1) * 10);
        configuration.addDefault(SPEED, (diamondLevel / 2) * 10);
        configuration.addDefault(UNLUCK, (1) * 10);
        configuration.addDefault(WATER_BREATHING, (1) * 10);
        configuration.addDefault(WEAKNESS, (diamondLevel) * 10);
        configuration.addDefault(WITHER, (diamondLevel / 2) * 10);

        configuration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
