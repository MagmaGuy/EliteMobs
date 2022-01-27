package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.combatsystem.CombatSystem;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemTierFinder {

    private ItemTierFinder() {
    }

    private static final int IRON_TIER = CombatSystem.IRON_TIER_LEVEL;
    private static final int STONE_CHAIN_TIER = CombatSystem.IRON_TIER_LEVEL;
    private static final int GOLD_WOOD_LEATHER_TIER = CombatSystem.GOLD_WOOD_LEATHER_TIER_LEVEL;
    private static final int DIAMOND_TIER = CombatSystem.DIAMOND_TIER_LEVEL;

    public static int findBattleTier(ItemStack itemStack) {
        return parseBattleMaterials(itemStack);
    }

    private static int parseBattleMaterials(ItemStack itemStack) {
        if (itemStack == null || itemStack.getItemMeta() == null) return 0;
        int mainEnchantment = 0;
        Material material = itemStack.getType();
        switch (material) {
            case DIAMOND_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case GOLDEN_HOE:
            case WOODEN_HOE:
                if (!ItemSettingsConfig.isUseHoesAsWeapons()) return 0;
            case DIAMOND_SWORD:
            case DIAMOND_AXE:
            case TRIDENT:
            case IRON_AXE:
            case IRON_SWORD:
            case STONE_SWORD:
            case STONE_AXE:
            case WOODEN_SWORD:
            case WOODEN_AXE:
            case GOLDEN_SWORD:
            case GOLDEN_AXE:
                mainEnchantment = findMainEnchantment(itemStack, Enchantment.DAMAGE_ALL);
                break;
            case DIAMOND_BOOTS:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_HELMET:
            case DIAMOND_LEGGINGS:
            case IRON_BOOTS:
            case IRON_CHESTPLATE:
            case IRON_HELMET:
            case IRON_LEGGINGS:
            case SHIELD:
            case CHAINMAIL_BOOTS:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_LEGGINGS:
            case GOLDEN_BOOTS:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_HELMET:
            case GOLDEN_LEGGINGS:
            case LEATHER_BOOTS:
            case LEATHER_CHESTPLATE:
            case LEATHER_HELMET:
            case LEATHER_LEGGINGS:
            case TURTLE_HELMET:
                mainEnchantment = findMainEnchantment(itemStack, Enchantment.PROTECTION_ENVIRONMENTAL);
                break;
            case BOW:
            case CROSSBOW:
                mainEnchantment = findMainEnchantment(itemStack, Enchantment.ARROW_DAMAGE);
                break;
            default:
                if (!VersionChecker.serverVersionOlderThan(16, 0)) {
                    if (material.equals(Material.NETHERITE_HELMET) ||
                            material.equals(Material.NETHERITE_CHESTPLATE) ||
                            material.equals(Material.NETHERITE_LEGGINGS) ||
                            material.equals(Material.NETHERITE_BOOTS))
                        mainEnchantment = findMainEnchantment(itemStack, Enchantment.PROTECTION_ENVIRONMENTAL);
                    if (material.equals(Material.NETHERITE_SWORD) ||
                            material.equals(Material.NETHERITE_AXE) ||
                            material.equals(Material.NETHERITE_HOE) && ItemSettingsConfig.isUseHoesAsWeapons())
                        mainEnchantment = findMainEnchantment(itemStack, Enchantment.DAMAGE_ALL);
                }
        }
        return getMaterialTier(material) + mainEnchantment;
    }

    public static int getMaterialTier(Material material) {
        switch (material) {
            case TRIDENT:
                return CombatSystem.TRIDENT_TIER_LEVEL;
            case DIAMOND_SWORD:
            case DIAMOND_AXE:
            case DIAMOND_BOOTS:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_HELMET:
            case DIAMOND_LEGGINGS:
                return DIAMOND_TIER;
            case IRON_AXE:
            case IRON_SWORD:
            case IRON_BOOTS:
            case IRON_CHESTPLATE:
            case IRON_HELMET:
            case IRON_LEGGINGS:
            case SHIELD:
            case BOW:
            case CROSSBOW:
                return IRON_TIER;
            case CHAINMAIL_BOOTS:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_LEGGINGS:
            case STONE_SWORD:
            case STONE_AXE:
                return STONE_CHAIN_TIER;
            case GOLDEN_BOOTS:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_HELMET:
            case GOLDEN_LEGGINGS:
            case LEATHER_BOOTS:
            case LEATHER_CHESTPLATE:
            case LEATHER_HELMET:
            case LEATHER_LEGGINGS:
            case TURTLE_HELMET:
            case WOODEN_SWORD:
            case WOODEN_AXE:
            case GOLDEN_SWORD:
            case GOLDEN_AXE:
                return GOLD_WOOD_LEATHER_TIER;
            case DIAMOND_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case GOLDEN_HOE:
            case WOODEN_HOE:
                if (!ItemSettingsConfig.isUseHoesAsWeapons()) return 0;
                if (material.equals(Material.DIAMOND_HOE))
                    return DIAMOND_TIER;
                if (material.equals(Material.IRON_HOE))
                    return IRON_TIER;
                if (material.equals(Material.STONE_HOE))
                    return STONE_CHAIN_TIER;
                if (material.equals(Material.GOLDEN_HOE) || material.equals(Material.WOODEN_HOE))
                    return GOLD_WOOD_LEATHER_TIER;
            default:
                if (!VersionChecker.serverVersionOlderThan(16, 0)) {
                    if (material.equals(Material.NETHERITE_HELMET) ||
                            material.equals(Material.NETHERITE_CHESTPLATE) ||
                            material.equals(Material.NETHERITE_LEGGINGS) ||
                            material.equals(Material.NETHERITE_BOOTS))
                        return CombatSystem.NETHERITE_TIER_LEVEL;
                    if (material.equals(Material.NETHERITE_SWORD) ||
                            material.equals(Material.NETHERITE_AXE) ||
                            material.equals(Material.NETHERITE_HOE) && ItemSettingsConfig.isUseHoesAsWeapons())
                        return CombatSystem.NETHERITE_TIER_LEVEL;
                }
                return 0;
        }
    }

    public static int mainHandCombatParser(ItemStack itemStack) {

        Material material = itemStack.getType();

        return parseMainHandMaterials(material, findMainEnchantment(itemStack, Enchantment.DAMAGE_ALL), findMainEnchantment(itemStack, Enchantment.PROTECTION_ENVIRONMENTAL), findMainEnchantment(itemStack, Enchantment.ARROW_DAMAGE));

    }

    private static int parseMainHandMaterials(Material material, int mainEnchantment, int mainEnchantment2, int mainEnchantment3) {
        switch (material) {
            case DIAMOND_SWORD:
            case DIAMOND_AXE:
                return DIAMOND_TIER + mainEnchantment;
            case TRIDENT:
                return CombatSystem.TRIDENT_TIER_LEVEL + mainEnchantment;
            case IRON_AXE:
            case IRON_SWORD:
                return IRON_TIER + mainEnchantment;
            case STONE_SWORD:
            case STONE_AXE:
                return STONE_CHAIN_TIER + mainEnchantment;
            case WOODEN_SWORD:
            case WOODEN_AXE:
            case GOLDEN_SWORD:
            case GOLDEN_AXE:
                return GOLD_WOOD_LEATHER_TIER + mainEnchantment;
            case BOW:
            case CROSSBOW:
                return IRON_TIER + mainEnchantment3;
            case DIAMOND_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case GOLDEN_HOE:
            case WOODEN_HOE:
                if (!ItemSettingsConfig.isUseHoesAsWeapons()) return 0;
                if (material.equals(Material.DIAMOND_HOE))
                    return DIAMOND_TIER + mainEnchantment;
                if (material.equals(Material.IRON_HOE))
                    return IRON_TIER + mainEnchantment;
                if (material.equals(Material.STONE_HOE))
                    return STONE_CHAIN_TIER + mainEnchantment;
                if (material.equals(Material.GOLDEN_HOE) || material.equals(Material.WOODEN_HOE))
                    return GOLD_WOOD_LEATHER_TIER + mainEnchantment;
            default:
                if (!VersionChecker.serverVersionOlderThan(16, 0))
                    if (material.equals(Material.NETHERITE_SWORD) ||
                            material.equals(Material.NETHERITE_AXE) ||
                            material.equals(Material.NETHERITE_HOE) && ItemSettingsConfig.isUseHoesAsWeapons())
                        return CombatSystem.NETHERITE_TIER_LEVEL + mainEnchantment;
                return 0;
        }
    }

    public static boolean isWeaponMaterial(ItemStack itemStack) {
        if (itemStack == null) return false;
        Material material = itemStack.getType();
        switch (material) {
            case DIAMOND_SWORD:
            case DIAMOND_AXE:
            case TRIDENT:
            case IRON_AXE:
            case IRON_SWORD:
            case STONE_SWORD:
            case STONE_AXE:
            case WOODEN_SWORD:
            case WOODEN_AXE:
            case GOLDEN_SWORD:
            case GOLDEN_AXE:
            case BOW:
            case CROSSBOW:
                return true;
            case DIAMOND_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case GOLDEN_HOE:
            case WOODEN_HOE:
                if (!ItemSettingsConfig.isUseHoesAsWeapons()) return false;
                return true;
            default:
                if (!VersionChecker.serverVersionOlderThan(16, 0))
                    if (material.equals(Material.NETHERITE_SWORD) ||
                            material.equals(Material.NETHERITE_AXE) ||
                            material.equals(Material.NETHERITE_HOE) && ItemSettingsConfig.isUseHoesAsWeapons())
                        return true;
                return false;
        }
    }

    private static int findMainEnchantment(ItemStack itemStack, Enchantment enchantment) {
        if (itemStack == null)
            return 0;
        int enchantLevel = ItemTagger.getEnchantment(itemStack.getItemMeta(), enchantment.getKey());
        if (enchantLevel == 0)
            if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasEnchant(enchantment))
                enchantLevel = itemStack.getItemMeta().getEnchantLevel(enchantment);
        return enchantLevel;
    }

    /**
     * Returns enchantment type that an item usses for the main enchantment. Returns null if there isn't one. Only takes
     * combat items into account.
     *
     * @param material
     * @return
     */
    public static Enchantment getMainCombatEnchantment(Material material) {
        switch (material) {
            case DIAMOND_HOE:
            case GOLDEN_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case WOODEN_HOE:
                if (!ItemSettingsConfig.isUseHoesAsWeapons()) return null;
            case TRIDENT:
            case DIAMOND_SWORD:
            case GOLDEN_SWORD:
            case IRON_SWORD:
            case STONE_SWORD:
            case WOODEN_SWORD:
            case DIAMOND_AXE:
            case GOLDEN_AXE:
            case IRON_AXE:
            case STONE_AXE:
            case WOODEN_AXE:
                return Enchantment.DAMAGE_ALL;
            case BOW:
            case CROSSBOW:
                return Enchantment.ARROW_DAMAGE;
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
            case TURTLE_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case LEATHER_LEGGINGS:
            case CHAINMAIL_BOOTS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
                return Enchantment.PROTECTION_ENVIRONMENTAL;
            default:
                if (!VersionChecker.serverVersionOlderThan(16, 0)) {
                    if (material.equals(Material.NETHERITE_SWORD) ||
                            material.equals(Material.NETHERITE_AXE) ||
                            material.equals(Material.NETHERITE_HOE) && ItemSettingsConfig.isUseHoesAsWeapons())
                        return Enchantment.DAMAGE_ALL;

                    if (material.equals(Material.NETHERITE_HELMET) ||
                            material.equals(Material.NETHERITE_CHESTPLATE) ||
                            material.equals(Material.NETHERITE_LEGGINGS) ||
                            material.equals(Material.NETHERITE_BOOTS))
                        return Enchantment.PROTECTION_ENVIRONMENTAL;
                }
                return null;
        }
    }

}
