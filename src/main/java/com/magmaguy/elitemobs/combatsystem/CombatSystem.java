package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.Material;

public class CombatSystem {
    public static final double PER_LEVEL_POWER_INCREASE = 0.5;
    /**
     * This determines how many hits it takes to kill a boss.
     * Unfortunately, since it's flat, at a scale, all bosses tend to end up having the same amount of life.
     * It accomplishes this by adding this flat amount of health to bosses.
     * <p>
     * Minus one because bosses start with 7 hp in the normalized combat system. They always have some baseline hp.
     */
    public static final double TARGET_HITS_TO_KILL_MINUS_ONE = 3D; //affects max health assignment on EliteMobEntity.java
    //this sets the tier of various materials
    public static final int NETHERITE_TIER_LEVEL = 8;
    public static final int DIAMOND_TIER_LEVEL = 7;
    public static final int IRON_TIER_LEVEL = 6;
    public static final int STONE_CHAIN_TIER_LEVEL = 5;
    public static final int GOLD_WOOD_LEATHER_TIER_LEVEL = 3;

    //DPS increase per tier
    public static final double DPS_PER_LEVEL = 1.6;


    private CombatSystem() {
    }

    public static int getMaterialTier(Material material) {
        switch (material) {
            case DIAMOND_BOOTS:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_HELMET:
            case DIAMOND_LEGGINGS:
                return DIAMOND_TIER_LEVEL;
            case IRON_BOOTS:
            case IRON_CHESTPLATE:
            case IRON_HELMET:
            case IRON_LEGGINGS:
                return IRON_TIER_LEVEL;
            case CHAINMAIL_BOOTS:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_LEGGINGS:
                return STONE_CHAIN_TIER_LEVEL;
            case GOLDEN_BOOTS:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_HELMET:
            case GOLDEN_LEGGINGS:
            case LEATHER_BOOTS:
            case LEATHER_CHESTPLATE:
            case LEATHER_HELMET:
            case LEATHER_LEGGINGS:
            case TURTLE_HELMET:
                return GOLD_WOOD_LEATHER_TIER_LEVEL;
            default:
                if (!VersionChecker.serverVersionOlderThan(16, 0) &&
                        (material.equals(Material.NETHERITE_HELMET) ||
                                material.equals(Material.NETHERITE_CHESTPLATE) ||
                                material.equals(Material.NETHERITE_LEGGINGS) ||
                                material.equals(Material.NETHERITE_BOOTS)))
                    return CombatSystem.NETHERITE_TIER_LEVEL;
        }
        return 0;
    }

}
