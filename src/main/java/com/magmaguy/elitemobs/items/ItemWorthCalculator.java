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

package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.items.itemconstructor.LoreGenerator;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.magmaguy.elitemobs.config.EconomySettingsConfig.*;

public class ItemWorthCalculator {

    public static double determineItemWorth(ItemStack itemStack) {

        double itemWorth = 1 + Math.round((itemTypeWorth(itemStack.getType()) + getAllEnchantmentsValue(itemStack) + potionEffectValue(itemStack)) * 100.0) / 100.0;

        return itemWorth;

    }

    public static double determineItemWorth(Material material, HashMap<Enchantment, Integer> enchantmentsMap) {

        double itemWorth = 1 + Math.round((itemTypeWorth(material) + getAllEnchantmentsValue(enchantmentsMap)) * 100.0) / 100.0;

        return itemWorth;

    }

    public static double determineItemWorth(Material material, HashMap<Enchantment, Integer> enchantmentsMap, List<String> potionsMap) {

        double itemWorth = 1 + Math.round((itemTypeWorth(material) + getAllEnchantmentsValue(enchantmentsMap) + potionEffectValue(potionsMap)) * 100.0) / 100.0;

        return itemWorth;

    }

    public static double determineResaleWorth(ItemStack itemStack) {

        double resaleWorth = Math.round((determineItemWorth(itemStack) * (ConfigValues.economyConfig.getDouble(EconomySettingsConfig.RESALE_VALUE) / 100)) * 100.0) / 100.0;

        return resaleWorth;

    }

    private static double configGetter(String string) {

        return ConfigValues.economyConfig.getDouble(string);

    }

    public static double itemTypeWorth(Material material) {

        switch (material) {

            case DIAMOND_AXE:
                return configGetter(DIAMOND_AXE);
            case DIAMOND_BARDING:
                return configGetter(DIAMOND_BARDING);
            case DIAMOND_BLOCK:
                return configGetter(DIAMOND_BLOCK);
            case DIAMOND_BOOTS:
                return configGetter(DIAMOND_BOOTS);
            case DIAMOND_CHESTPLATE:
                return configGetter(DIAMOND_CHESTPLATE);
            case DIAMOND_HELMET:
                return configGetter(DIAMOND_HELMET);
            case DIAMOND_HOE:
                return configGetter(DIAMOND_HOE);
            case DIAMOND_LEGGINGS:
                return configGetter(DIAMOND_LEGGINGS);
            case DIAMOND_ORE:
                return configGetter(DIAMOND_ORE);
            case DIAMOND_PICKAXE:
                return configGetter(DIAMOND_PICKAXE);
            case DIAMOND_SPADE:
                return configGetter(DIAMOND_SPADE);
            case DIAMOND_SWORD:
                return configGetter(DIAMOND_SWORD);
            case IRON_AXE:
                return configGetter(IRON_AXE);
            case IRON_BARDING:
                return configGetter(IRON_BARDING);
            case IRON_BLOCK:
                return configGetter(IRON_BLOCK);
            case IRON_BOOTS:
                return configGetter(IRON_BOOTS);
            case IRON_CHESTPLATE:
                return configGetter(IRON_CHESTPLATE);
            case IRON_HELMET:
                return configGetter(IRON_HELMET);
            case IRON_HOE:
                return configGetter(IRON_HOE);
            case IRON_INGOT:
                return configGetter(IRON_INGOT);
            case IRON_LEGGINGS:
                return configGetter(IRON_LEGGINGS);
            case IRON_NUGGET:
                return configGetter(IRON_NUGGET);
            case IRON_ORE:
                return configGetter(IRON_ORE);
            case IRON_PICKAXE:
                return configGetter(IRON_PICKAXE);
            case IRON_SPADE:
                return configGetter(IRON_SPADE);
            case IRON_SWORD:
                return configGetter(IRON_SWORD);
            case BOW:
                return configGetter(BOW);
            case SHIELD:
                return configGetter(SHIELD);
            case CHAINMAIL_BOOTS:
                return configGetter(CHAINMAIL_BOOTS);
            case CHAINMAIL_CHESTPLATE:
                return configGetter(CHAINMAIL_CHESTPLATE);
            case CHAINMAIL_HELMET:
                return configGetter(CHAINMAIL_HELMET);
            case CHAINMAIL_LEGGINGS:
                return configGetter(CHAINMAIL_LEGGINGS);
            case STONE_SWORD:
                return configGetter(STONE_SWORD);
            case STONE_SPADE:
                return configGetter(STONE_SPADE);
            case STONE_AXE:
                return configGetter(STONE_AXE);
            case STONE_PICKAXE:
                return configGetter(STONE_PICKAXE);
            case GOLD_AXE:
                return configGetter(GOLD_AXE);
            case GOLD_BARDING:
                return configGetter(GOLD_BARDING);
            case GOLD_BLOCK:
                return configGetter(GOLD_BLOCK);
            case GOLD_BOOTS:
                return configGetter(GOLD_BOOTS);
            case GOLD_CHESTPLATE:
                return configGetter(GOLD_CHESTPLATE);
            case GOLD_HELMET:
                return configGetter(GOLD_HELMET);
            case GOLD_HOE:
                return configGetter(GOLD_HOE);
            case GOLD_INGOT:
                return configGetter(GOLD_INGOT);
            case GOLD_LEGGINGS:
                return configGetter(GOLD_LEGGINGS);
            case GOLD_NUGGET:
                return configGetter(GOLD_NUGGET);
            case GOLD_ORE:
                return configGetter(GOLD_ORE);
            case GOLD_PICKAXE:
                return configGetter(GOLD_PICKAXE);
            case GOLD_SPADE:
                return configGetter(GOLD_SPADE);
            case GOLD_SWORD:
                return configGetter(GOLD_SWORD);
            case GOLDEN_APPLE:
                return configGetter(GOLDEN_APPLE);
            case GOLDEN_CARROT:
                return configGetter(GOLDEN_CARROT);
            case LEATHER_BOOTS:
                return configGetter(LEATHER_BOOTS);
            case LEATHER_CHESTPLATE:
                return configGetter(LEATHER_CHESTPLATE);
            case LEATHER_HELMET:
                return configGetter(LEATHER_HELMET);
            case LEATHER_LEGGINGS:
                return configGetter(LEATHER_LEGGINGS);
            case WOOD_SWORD:
                return configGetter(WOOD_SWORD);
            case WOOD_AXE:
                return configGetter(WOOD_AXE);
            case WOOD_PICKAXE:
                return configGetter(WOOD_PICKAXE);
            case WOOD_HOE:
                return configGetter(WOOD_HOE);
            default:
                return configGetter(OTHER);

        }

    }

    private static double getAllEnchantmentsValue(ItemStack itemStack) {

        double enchantmentsValue = 0;

        if (!itemStack.getEnchantments().isEmpty()) {

            for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {

                enchantmentsValue += enchantmentWorthGetter(entry.getKey()) * entry.getValue();

            }

        }

        return enchantmentsValue;

    }

    private static double getAllEnchantmentsValue(HashMap<Enchantment, Integer> enchantmentsMap) {

        double enchantmentsValue = 0;

        if (!enchantmentsMap.isEmpty()) {

            for (Enchantment enchantment : enchantmentsMap.keySet()) {

                enchantmentsValue += enchantmentWorthGetter(enchantment) * enchantmentsMap.get(enchantment);

            }

        }

        return enchantmentsValue;

    }

    public static double enchantmentWorthGetter(Enchantment enchantment) {

        if (enchantment == null) return 0;

        String enchantmentName = enchantment.getName();

        switch (enchantmentName) {

            case "ARROW_DAMAGE":
                return configGetter(ARROW_DAMAGE);
            case "ARROW_FIRE":
                return configGetter(ARROW_FIRE);
            case "ARROW_INFINITE":
                return configGetter(ARROW_INFINITE);
            case "ARROW_KNOCKBACK":
                return configGetter(ARROW_KNOCKBACK);
            case "BINDING_CURSE":
                return configGetter(BINDING_CURSE);
            case "DAMAGE_ALL":
                return configGetter(DAMAGE_ALL);
            case "DAMAGE_ARTHROPODS":
                return configGetter(DAMAGE_ARTHROPODS);
            case "DAMAGE_UNDEAD":
                return configGetter(DAMAGE_UNDEAD);
            case "DEPTH_STRIDER":
                return configGetter(DEPTH_STRIDER);
            case "DIG_SPEED":
                return configGetter(DIG_SPEED);
            case "DURABILITY":
                return configGetter(DURABILITY);
            case "FIRE_ASPECT":
                return configGetter(FIRE_ASPECT);
            case "FROST_WALKER":
                return configGetter(FROST_WALKER);
            case "KNOCKBACK":
                return configGetter(KNOCKBACK);
            case "LOOT_BONUS_BLOCKS":
                return configGetter(LOOT_BONUS_BLOCKS);
            case "LOOT_BONUS_MOBS":
                return configGetter(LOOT_BONUS_MOBS);
            case "LUCK":
                return configGetter(LUCK_ENCHANTMENT);
            case "LURE":
                return configGetter(LURE);
            case "MENDING":
                return configGetter(MENDING);
            case "OXYGEN":
                return configGetter(OXYGEN);
            case "PROTECTION_ENVIRONMENTAL":
                return configGetter(PROTECTION_ENVIRONMENTAL);
            case "PROTECTION_EXPLOSIONS":
                return configGetter(PROTECTION_EXPLOSIONS);
            case "PROTECTION_FALL":
                return configGetter(PROTECTION_FALL);
            case "PROTECTION_PROJECTILE":
                return configGetter(PROTECTION_PROJECTILE);
            case "PROTECTION_FIRE":
                return configGetter(PROTECTION_FIRE);
            case "SILK_TOUCH":
                return configGetter(SILK_TOUCH);
            case "SWEEPING_EDGE":
                return configGetter(SWEEPING_EDGE);
            case "THORNS":
                return configGetter(THORNS);
            case "VANISHING_CURSE":
                return configGetter(VANISHING_CURSE);
            case "WATER_WORKER":
                return configGetter(WATER_WORKER);
            default:
                return 0;
        }

    }

    private static double potionEffectValue(ItemStack itemStack) {

        double potionEffectValue = 0;
        if (!itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) return potionEffectValue;

        String potionEffectLoreLine = itemStack.getItemMeta().getLore().get(0).replace("§", "");
        if (!potionEffectLoreLine.contains(LoreGenerator.OBFUSCATED_POTIONS)) return potionEffectValue;

        for (String substring : potionEffectLoreLine.split(",")) {

            List<String> parsedSubstring = Arrays.asList(substring.split(":"));
            if (parsedSubstring.size() > 1) {
                potionEffectValue += getPotionEffectValue(parsedSubstring.get(0)) * (Integer.valueOf(parsedSubstring.get(1)) + 1);
            }

        }

        return potionEffectValue;

    }

    private static double potionEffectValue(List<String> potionString) {

        double potionEffectValue = 0;

        if (potionString == null || potionString.isEmpty()) return potionEffectValue;

        for (String string : potionString)
            potionEffectValue += getPotionEffectValue(string.split(",")[0]) * (Integer.parseInt(string.split(",")[1]) + 1);

        return potionEffectValue;

    }

    private static double getPotionEffectValue(String string) {

        switch (string) {

            case "ABSORPTION":
                return configGetter(ABSORPTION);
            case "BLINDNESS":
                return configGetter(BLINDNESS);
            case "CONFUSION":
                return configGetter(CONFUSION);
            case "DAMAGE_RESISTANCE":
                return configGetter(DAMAGE_RESISTANCE);
            case "FAST_DIGGING":
                return configGetter(FAST_DIGGING);
            case "FIRE_RESISTANCE":
                return configGetter(FIRE_RESISTANCE);
            case "GLOWING":
                return configGetter(GLOWING);
            case "HARM":
                return configGetter(HARM);
            case "HEAL":
                return configGetter(HEAL);
            case "HEALTH_BOOST":
                return configGetter(HEALTH_BOOST);
            case "HUNGER":
                return configGetter(HUNGER);
            case "INCREASE_DAMAGE":
                return configGetter(INCREASE_DAMAGE);
            case "INVISIBILITY":
                return configGetter(INVISIBILITY);
            case "JUMP":
                return configGetter(JUMP);
            case "LEVITATION":
                return configGetter(LEVITATION);
            case "LUCK":
                return configGetter(LUCK_POTION);
            case "NIGHT_VISION":
                return configGetter(NIGHT_VISION);
            case "POISON":
                return configGetter(POISON);
            case "REGENERATION":
                return configGetter(REGENERATION);
            case "SATURATION":
                return configGetter(SATURATION);
            case "SLOW":
                return configGetter(SLOW);
            case "SLOW_DIGGING":
                return configGetter(SLOW_DIGGING);
            case "SPEED":
                return configGetter(SPEED);
            case "UNLUCK":
                return configGetter(UNLUCK);
            case "WATER_BREATHING":
                return configGetter(WATER_BREATHING);
            case "WEAKNESS":
                return configGetter(WEAKNESS);
            case "WITHER":
                return configGetter(WITHER);
            default:
                return 0;
        }

    }

}
