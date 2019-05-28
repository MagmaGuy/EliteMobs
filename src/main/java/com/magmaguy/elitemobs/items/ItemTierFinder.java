package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ItemsDropSettingsConfig;
import com.magmaguy.elitemobs.items.itemconstructor.LoreGenerator;
import com.magmaguy.elitemobs.mobconstructor.CombatSystem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemTierFinder {

    public static double findPlayerTier(Player player) {

        return (4 * findArmorSetTier(player) + findWeaponTier(player)) / 5;

    }

    public static double findArmorSetTier(Player player) {

        double totalArmorThreat = 0;

        if (player.getEquipment().getHelmet() != null) {

            ItemStack helmet = player.getEquipment().getHelmet();

            totalArmorThreat += findBattleTier(helmet);

        }

        if (player.getEquipment().getChestplate() != null) {

            ItemStack chestplate = player.getEquipment().getChestplate();

            totalArmorThreat += findBattleTier(chestplate);

        }

        if (player.getEquipment().getLeggings() != null) {

            ItemStack leggings = player.getEquipment().getLeggings();

            totalArmorThreat += findBattleTier(leggings);

        }

        if (player.getEquipment().getBoots() != null) {

            ItemStack boots = player.getEquipment().getBoots();

            totalArmorThreat += findBattleTier(boots);

        }

        double averageArmorThreat = totalArmorThreat / 4;

        return averageArmorThreat;

    }

    public static double findWeaponTier(Player player) {

        List<ItemStack> itemList = new ArrayList<>();

        if (player.getInventory().getItemInOffHand() != null && !player.getInventory().getItemInOffHand().getType().equals(Material.AIR)) {

            itemList.add(player.getInventory().getItemInOffHand());

        }

        for (int i = 0; i < 9; i++) {

            if (player.getInventory().getItem(i) != null && !player.getInventory().getItem(i).getType().equals(Material.AIR)) {

                Material material = player.getInventory().getItem(i).getType();

                if (material.equals(Material.DIAMOND_SWORD) || material.equals(Material.DIAMOND_AXE) ||
                        material.equals(Material.IRON_SWORD) || material.equals(Material.IRON_AXE) ||
                        material.equals(Material.STONE_SWORD) || material.equals(Material.STONE_AXE) ||
                        material.equals(Material.GOLDEN_SWORD) || material.equals(Material.GOLDEN_AXE) ||
                        material.equals(Material.WOODEN_SWORD) || material.equals(Material.WOODEN_AXE) ||
                        material.equals(Material.BOW)) {

                    itemList.add(player.getInventory().getItem(i));

                }

            }

        }

        double highestTier = 0;

        if (itemList.size() > 0) {

            for (ItemStack itemStack : itemList) {

                double currentTier = findBattleTier(itemStack);

                if (currentTier > highestTier) {

                    highestTier = currentTier;

                }

            }

        }

        return highestTier;

    }

    private static final double IRON_TIER = CombatSystem.IRON_TIER_LEVEL;
    private static final double STONE_CHAIN_TIER = CombatSystem.IRON_TIER_LEVEL;
    private static final double GOLD_WOOD_LEATHER_TIER = CombatSystem.GOLD_WOOD_LEATHER_TIER_LEVEL;
    private static final double DIAMOND_TIER = CombatSystem.DIAMOND_TIER_LEVEL;

    public static double findGenericTier(Material material, HashMap<Enchantment, Integer> enchantmentList) {

        double tier = 0;

        switch (material) {
            case DIAMOND_PICKAXE:
            case DIAMOND_SHOVEL:
                return DIAMOND_TIER + findMainEnchantment(enchantmentList, Enchantment.DIG_SPEED);
            case STONE_PICKAXE:
            case STONE_SHOVEL:
                return STONE_CHAIN_TIER + findMainEnchantment(enchantmentList, Enchantment.DIG_SPEED);
            case IRON_PICKAXE:
            case IRON_SHOVEL:
                return IRON_TIER + findMainEnchantment(enchantmentList, Enchantment.DIG_SPEED);
            case GOLDEN_PICKAXE:
            case GOLDEN_SHOVEL:
            case WOODEN_PICKAXE:
            case WOODEN_SHOVEL:
                return GOLD_WOOD_LEATHER_TIER + findMainEnchantment(enchantmentList, Enchantment.DIG_SPEED);
        }

        if (tier == 0)
            tier = findBattleTier(material, enchantmentList);

        return tier;

    }

    public static double findBattleTier(Material material, HashMap<Enchantment, Integer> enchantmentList) {

        return parseMaterials(material, findMainEnchantment(enchantmentList, Enchantment.DAMAGE_ALL), findMainEnchantment(enchantmentList, Enchantment.PROTECTION_ENVIRONMENTAL), findMainEnchantment(enchantmentList, Enchantment.ARROW_DAMAGE));

    }

    public static double findBattleTier(ItemStack itemStack) {

        Material material = itemStack.getType();

        return parseMaterials(material, findMainEnchantment(itemStack, Enchantment.DAMAGE_ALL), findMainEnchantment(itemStack, Enchantment.PROTECTION_ENVIRONMENTAL), findMainEnchantment(itemStack, Enchantment.ARROW_DAMAGE));

    }

    private static double parseMaterials(Material material, int mainEnchantment, int mainEnchantment2, int mainEnchantment3) {
        switch (material) {
            case DIAMOND_SWORD:
            case DIAMOND_AXE:
                return DIAMOND_TIER + mainEnchantment;
            case DIAMOND_BOOTS:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_HELMET:
            case DIAMOND_LEGGINGS:
                return DIAMOND_TIER + mainEnchantment2;
            case IRON_AXE:
            case IRON_SWORD:
                return IRON_TIER + mainEnchantment;
            case IRON_BOOTS:
            case IRON_CHESTPLATE:
            case IRON_HELMET:
            case IRON_LEGGINGS:
                return IRON_TIER + mainEnchantment2;
            case SHIELD:
                return IRON_TIER + mainEnchantment2;
            case CHAINMAIL_BOOTS:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_LEGGINGS:
                return STONE_CHAIN_TIER + mainEnchantment2;
            case STONE_SWORD:
            case STONE_AXE:
                return STONE_CHAIN_TIER + mainEnchantment;
            case GOLDEN_BOOTS:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_HELMET:
            case GOLDEN_LEGGINGS:
            case LEATHER_BOOTS:
            case LEATHER_CHESTPLATE:
            case LEATHER_HELMET:
            case LEATHER_LEGGINGS:
                return GOLD_WOOD_LEATHER_TIER + mainEnchantment2;
            case WOODEN_SWORD:
            case WOODEN_AXE:
            case GOLDEN_SWORD:
            case GOLDEN_AXE:
                return GOLD_WOOD_LEATHER_TIER + mainEnchantment;
            case BOW:
                return IRON_TIER + mainEnchantment3;
            case DIAMOND_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case GOLDEN_HOE:
            case WOODEN_HOE:
                if (!ConfigValues.itemsDropSettingsConfig.getBoolean(ItemsDropSettingsConfig.HOES_AS_WEAPONS)) return 0;
                if (material.equals(Material.DIAMOND_HOE))
                    return DIAMOND_TIER + mainEnchantment;
                if (material.equals(Material.IRON_HOE))
                    return IRON_TIER + mainEnchantment;
                if (material.equals(Material.STONE_HOE))
                    return STONE_CHAIN_TIER + mainEnchantment;
                if (material.equals(Material.GOLDEN_HOE) || material.equals(Material.WOODEN_HOE))
                    return GOLD_WOOD_LEATHER_TIER + mainEnchantment;
            case TRIDENT:
                return DIAMOND_TIER + mainEnchantment;
            default:
                return 0;
        }
    }

    private static int findMainEnchantment(HashMap<Enchantment, Integer> enchantmentMap, Enchantment enchantment) {

        int enchantments = 0;

        if (!enchantmentMap.isEmpty())
            for (Enchantment currentEnchantment : enchantmentMap.keySet())
                if (currentEnchantment.equals(enchantment))
                    enchantments += enchantmentMap.get(currentEnchantment);

        return enchantments;

    }

    private static int findMainEnchantment(ItemStack itemStack, Enchantment enchantment) {

        int enchantments = 0;

        if (ObfuscatedSignatureLoreData.obfuscatedSignatureDetector(itemStack)) {
            String deobfuscatedString = itemStack.getItemMeta().getLore().get(0).replace("§", "");
            if (deobfuscatedString.contains(LoreGenerator.OBFUSCATED_ENCHANTMENTS))
                return findObfuscatedMainEnchantment(deobfuscatedString, enchantment);
        }

        if (!itemStack.getEnchantments().isEmpty())
            for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet())
                if (entry.getKey().equals(enchantment))
                    enchantments += entry.getValue();

        return enchantments;

    }

    private static int findObfuscatedMainEnchantment(String deobfuscatedLore, Enchantment enchantment) {
        for (String string : deobfuscatedLore.split(","))
            if (string.contains(enchantment.getName()))
                return Integer.parseInt(string.split(":")[1]);

        return 0;
    }

}
