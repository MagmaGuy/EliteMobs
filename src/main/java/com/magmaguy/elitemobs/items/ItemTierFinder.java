package com.magmaguy.elitemobs.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
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

            totalArmorThreat += findItemTier(helmet);

        }

        if (player.getEquipment().getChestplate() != null) {

            ItemStack chestplate = player.getEquipment().getChestplate();

            totalArmorThreat += findItemTier(chestplate);

        }

        if (player.getEquipment().getLeggings() != null) {

            ItemStack leggings = player.getEquipment().getLeggings();

            totalArmorThreat += findItemTier(leggings);

        }

        if (player.getEquipment().getBoots() != null) {

            ItemStack boots = player.getEquipment().getBoots();

            totalArmorThreat += findItemTier(boots);

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
                        material.equals(Material.GOLD_SWORD) || material.equals(Material.GOLD_AXE) ||
                        material.equals(Material.WOOD_SWORD) || material.equals(Material.WOOD_AXE) ||
                        material.equals(Material.BOW)) {

                    itemList.add(player.getInventory().getItem(i));

                }

            }

        }

        double highestTier = 0;

        if (itemList.size() > 0) {

            for (ItemStack itemStack : itemList) {

                double currentTier = findItemTier(itemStack);

                if (currentTier > highestTier) {

                    highestTier = currentTier;

                }

            }

        }

        return highestTier;

    }

    public static double findItemTier(ItemStack itemStack) {

        //Divide the tier rank by the 4 armor slots and 1 weapon slot
        double diamondTier = 1;
        double ironTier = 0.66;
        double stoneChainTier = 0.25;
        double goldWoodLeatherTier = 0;

        Material material = itemStack.getType();

        switch (material) {

            case DIAMOND_AXE:
                return diamondTier + findMeleeWeaponEnchantments(itemStack);
            case DIAMOND_BOOTS:
                return diamondTier + findArmorEnchantments(itemStack);
            case DIAMOND_CHESTPLATE:
                return diamondTier + findArmorEnchantments(itemStack);
            case DIAMOND_HELMET:
                return diamondTier + findArmorEnchantments(itemStack);
            case DIAMOND_LEGGINGS:
                return diamondTier + findArmorEnchantments(itemStack);
            case DIAMOND_SWORD:
                return diamondTier + findMeleeWeaponEnchantments(itemStack);
            case IRON_AXE:
                return ironTier + findMeleeWeaponEnchantments(itemStack);
            case IRON_BOOTS:
                return ironTier + findArmorEnchantments(itemStack);
            case IRON_CHESTPLATE:
                return ironTier + findArmorEnchantments(itemStack);
            case IRON_HELMET:
                return ironTier + findArmorEnchantments(itemStack);
            case IRON_LEGGINGS:
                return ironTier + findArmorEnchantments(itemStack);
            case IRON_SWORD:
                return ironTier + findMeleeWeaponEnchantments(itemStack);
            case BOW:
                return ironTier + findRangedWeaponEnchantments(itemStack);
            case SHIELD:
                return ironTier + findArmorEnchantments(itemStack);
            case CHAINMAIL_BOOTS:
                return stoneChainTier + findArmorEnchantments(itemStack);
            case CHAINMAIL_CHESTPLATE:
                return stoneChainTier + findArmorEnchantments(itemStack);
            case CHAINMAIL_HELMET:
                return stoneChainTier + findArmorEnchantments(itemStack);
            case CHAINMAIL_LEGGINGS:
                return stoneChainTier + findArmorEnchantments(itemStack);
            case STONE_SWORD:
                return stoneChainTier + findMeleeWeaponEnchantments(itemStack);
            case GOLD_AXE:
                return goldWoodLeatherTier + findMeleeWeaponEnchantments(itemStack);
            case GOLD_BOOTS:
                return goldWoodLeatherTier + findArmorEnchantments(itemStack);
            case GOLD_CHESTPLATE:
                return goldWoodLeatherTier + findArmorEnchantments(itemStack);
            case GOLD_HELMET:
                return goldWoodLeatherTier + findArmorEnchantments(itemStack);
            case GOLD_LEGGINGS:
                return goldWoodLeatherTier + findArmorEnchantments(itemStack);
            case GOLD_SWORD:
                return goldWoodLeatherTier + findMeleeWeaponEnchantments(itemStack);
            case LEATHER_BOOTS:
                return goldWoodLeatherTier + findArmorEnchantments(itemStack);
            case LEATHER_CHESTPLATE:
                return goldWoodLeatherTier + findArmorEnchantments(itemStack);
            case LEATHER_HELMET:
                return goldWoodLeatherTier + findArmorEnchantments(itemStack);
            case LEATHER_LEGGINGS:
                return goldWoodLeatherTier + findArmorEnchantments(itemStack);
            case WOOD_SWORD:
                return goldWoodLeatherTier + findMeleeWeaponEnchantments(itemStack);
            case WOOD_AXE:
                return goldWoodLeatherTier + findMeleeWeaponEnchantments(itemStack);
            default:
                return 0;

        }

    }

    private static int findArmorEnchantments(ItemStack itemStack) {

        int enchantments = 0;

        if (!itemStack.getEnchantments().isEmpty()) {

            for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {

                if (entry.getKey().equals(Enchantment.PROTECTION_ENVIRONMENTAL)) {

                    enchantments += entry.getValue();

                }

            }

        }

        return enchantments;

    }

    private static int findMeleeWeaponEnchantments(ItemStack itemStack) {

        int enchantments = 0;

        if (!itemStack.getEnchantments().isEmpty()) {

            for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {

                if (entry.getKey().equals(Enchantment.DAMAGE_ALL)) {

                    enchantments += entry.getValue();

                }

            }

        }

        return enchantments;

    }

    private static int findRangedWeaponEnchantments(ItemStack itemStack) {

        int enchantments = 0;

        if (!itemStack.getEnchantments().isEmpty()) {

            for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {

                if (entry.getKey().equals(Enchantment.ARROW_DAMAGE)) {

                    enchantments += entry.getValue();

                }

            }

        }

        return enchantments;

    }

}
