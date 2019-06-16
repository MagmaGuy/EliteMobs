package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.mobconstructor.CombatSystem;
import org.bukkit.Material;

public class MaterialTier {

    public static double getMaterialTier(Material material) {
        switch (material) {
            case DIAMOND_AXE:
            case DIAMOND_HORSE_ARMOR:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_HELMET:
            case DIAMOND_HOE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_PICKAXE:
            case DIAMOND_SHOVEL:
            case DIAMOND_SWORD:
            case DIAMOND_BOOTS:
                return CombatSystem.DIAMOND_TIER_LEVEL;
            case IRON_AXE:
            case IRON_HORSE_ARMOR:
            case IRON_BOOTS:
            case IRON_CHESTPLATE:
            case IRON_HELMET:
            case IRON_HOE:
            case IRON_LEGGINGS:
            case IRON_PICKAXE:
            case IRON_SHOVEL:
            case IRON_SWORD:
                return CombatSystem.IRON_TIER_LEVEL;
            case CHAINMAIL_BOOTS:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_LEGGINGS:
            case STONE_SWORD:
            case STONE_HOE:
            case STONE_SHOVEL:
            case STONE_PICKAXE:
            case STONE_AXE:
                return CombatSystem.STONE_CHAIN_TIER_LEVEL;
            default:
                //gold ,wood and leather all have a base tier of 0, all other items do as well
                return 0;
        }
    }

}
