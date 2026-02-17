package com.magmaguy.elitemobs.skills;

import lombok.Getter;
import org.bukkit.Material;

/**
 * Represents the different skill types that players can level up independently.
 * Each skill corresponds to a weapon type or armor, with armor leveling at 1/3 the rate.
 */
public enum SkillType {
    ARMOR("Armor", 1.0 / 3.0),
    SWORDS("Swords", 1.0),
    AXES("Axes", 1.0),
    BOWS("Bows", 1.0),
    CROSSBOWS("Crossbows", 1.0),
    TRIDENTS("Tridents", 1.0),
    HOES("Hoes", 1.0),
    MACES("Maces", 1.0),
    SPEARS("Spears", 1.0);

    @Getter
    private final String displayName;
    @Getter
    private final double xpMultiplier;

    SkillType(String displayName, double xpMultiplier) {
        this.displayName = displayName;
        this.xpMultiplier = xpMultiplier;
    }

    /**
     * Determines the weapon skill type from a material.
     * Returns null if the material doesn't correspond to a weapon skill (e.g., armor, blocks, etc.)
     *
     * @param material The material to check
     * @return The corresponding SkillType, or null if not a weapon
     */
    public static SkillType fromMaterial(Material material) {
        if (material == null) return null;

        String name = material.name();

        // Check for swords
        if (name.endsWith("_SWORD")) {
            return SWORDS;
        }

        // Check for axes
        if (name.endsWith("_AXE")) {
            return AXES;
        }

        // Check for hoes (scythes)
        if (name.endsWith("_HOE")) {
            return HOES;
        }

        // Check for bows
        if (material == Material.BOW) {
            return BOWS;
        }

        // Check for crossbows
        if (material == Material.CROSSBOW) {
            return CROSSBOWS;
        }

        // Check for tridents
        if (material == Material.TRIDENT) {
            return TRIDENTS;
        }

        // Check for maces (1.21+)
        try {
            if (material == Material.MACE) {
                return MACES;
            }
        } catch (NoSuchFieldError e) {
            // MACE doesn't exist in this version
        }

        // Check for spears (1.21.11+)
        if (name.endsWith("_SPEAR")) {
            return SPEARS;
        }

        // Not a recognized weapon type
        return null;
    }

    /**
     * Determines the skill type from a material, including armor pieces.
     * This is used for gear restrictions.
     *
     * @param material The material to check
     * @return The corresponding SkillType, or null if not a recognized elite item type
     */
    public static SkillType fromMaterialIncludingArmor(Material material) {
        if (material == null) return null;

        // First check weapons
        SkillType weaponType = fromMaterial(material);
        if (weaponType != null) return weaponType;

        String name = material.name();

        // Check for armor pieces
        if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
            name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")) {
            return ARMOR;
        }

        // Check for elytra (counts as armor)
        if (material == Material.ELYTRA) {
            return ARMOR;
        }

        // Check for turtle helmet
        if (material == Material.TURTLE_HELMET) {
            return ARMOR;
        }

        return null;
    }

    /**
     * Gets all weapon skill types (excludes ARMOR)
     *
     * @return Array of weapon skill types
     */
    public static SkillType[] getWeaponSkills() {
        return new SkillType[]{SWORDS, AXES, BOWS, CROSSBOWS, TRIDENTS, HOES, MACES, SPEARS};
    }

    /**
     * Checks if this skill type is a weapon skill (not armor)
     *
     * @return true if this is a weapon skill
     */
    public boolean isWeaponSkill() {
        return this != ARMOR;
    }

    /**
     * Gets the database column name for this skill type
     *
     * @return The column name (e.g., "SkillXP_Swords")
     */
    public String getColumnName() {
        return "SkillXP_" + this.name();
    }
}
