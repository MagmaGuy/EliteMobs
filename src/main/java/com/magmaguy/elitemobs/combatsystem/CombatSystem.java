package com.magmaguy.elitemobs.combatsystem;

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

    // ========================================
    // Skill-Based Combat System (50/50 split)
    // ========================================

    /**
     * The ratio of damage/defense that comes from player skill level.
     * With 0.5, half the effective power comes from skill, half from gear.
     */
    public static final double SKILL_CONTRIBUTION_RATIO = 0.5;

    /**
     * The ratio of damage/defense that comes from item level.
     * With 0.5, half the effective power comes from gear, half from skill.
     */
    public static final double ITEM_CONTRIBUTION_RATIO = 0.5;

    /**
     * Maximum level for the skill system.
     * Beyond this level, soft caps apply and progression becomes exponentially harder.
     */
    public static final int MAX_SKILL_LEVEL = 100;

    /**
     * Calculates the damage contribution from player skill level.
     * This is half of the total effective damage in a 50/50 split system.
     *
     * @param skillLevel The player's skill level for the weapon type
     * @return The damage contribution from skill
     */
    public static double getSkillDamageContribution(int skillLevel) {
        return skillLevel * SKILL_CONTRIBUTION_RATIO;
    }

    /**
     * Calculates the damage contribution from item level.
     * This is half of the total effective damage in a 50/50 split system.
     *
     * @param itemLevel The item's level
     * @return The damage contribution from the item
     */
    public static double getItemDamageContribution(int itemLevel) {
        return itemLevel * ITEM_CONTRIBUTION_RATIO;
    }

    /**
     * Calculates total effective damage combining skill and item contributions.
     *
     * @param skillLevel The player's skill level for the weapon type
     * @param itemLevel The weapon's level
     * @return The total effective damage
     */
    public static double getTotalEffectiveDamage(int skillLevel, int itemLevel) {
        return getSkillDamageContribution(skillLevel) + getItemDamageContribution(itemLevel);
    }

    /**
     * Calculates the defense contribution from player armor skill level.
     * This is half of the total effective defense in a 50/50 split system.
     *
     * @param armorSkillLevel The player's armor skill level
     * @return The defense contribution from skill
     */
    public static double getSkillDefenseContribution(int armorSkillLevel) {
        return armorSkillLevel * SKILL_CONTRIBUTION_RATIO;
    }

    /**
     * Calculates the defense contribution from armor item levels.
     * This is half of the total effective defense in a 50/50 split system.
     *
     * @param totalArmorLevel The combined level of all armor pieces
     * @return The defense contribution from armor
     */
    public static double getArmorDefenseContribution(int totalArmorLevel) {
        return totalArmorLevel * ITEM_CONTRIBUTION_RATIO;
    }

    /**
     * Calculates total effective defense combining skill and armor contributions.
     *
     * @param armorSkillLevel The player's armor skill level
     * @param totalArmorLevel The combined level of all armor pieces
     * @return The total effective defense
     */
    public static double getTotalEffectiveDefense(int armorSkillLevel, int totalArmorLevel) {
        return getSkillDefenseContribution(armorSkillLevel) + getArmorDefenseContribution(totalArmorLevel);
    }


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
                if (material.equals(Material.NETHERITE_HELMET) ||
                        material.equals(Material.NETHERITE_CHESTPLATE) ||
                        material.equals(Material.NETHERITE_LEGGINGS) ||
                        material.equals(Material.NETHERITE_BOOTS))
                    return CombatSystem.NETHERITE_TIER_LEVEL;
        }
        return 0;
    }

}
