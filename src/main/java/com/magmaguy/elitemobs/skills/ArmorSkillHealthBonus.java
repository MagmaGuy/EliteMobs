package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Applies bonus max health to players based on their Armor skill level.
 * Players receive +1 heart (2 HP) per Armor skill level.
 */
public class ArmorSkillHealthBonus {

    // Consistent UUID for the attribute modifier so we can find and remove it
    private static final UUID MODIFIER_UUID = UUID.fromString("e1b2c3d4-a5f6-4890-abcd-ef1234567890");
    private static final String MODIFIER_NAME = "elitemobs.armor_skill_health";

    private ArmorSkillHealthBonus() {
        // Static utility class
    }

    /**
     * Applies the armor skill health bonus to a player.
     * Should be called on player join after PlayerData is loaded.
     *
     * @param player The player to apply the bonus to
     */
    public static void applyHealthBonus(Player player) {
        if (player == null || !player.isOnline()) return;

        // First remove any existing modifier
        removeHealthBonus(player);

        // Get the player's armor skill level
        long armorXP = PlayerData.getSkillXP(player.getUniqueId(), SkillType.ARMOR);
        int armorLevel = SkillXPCalculator.levelFromTotalXP(armorXP);

        // No bonus at level 1 (base level)
        if (armorLevel <= 1) return;

        // Calculate bonus: +1 heart (2 HP) per level above 1
        double bonusHealth = (armorLevel - 1) * 2.0;

        // Apply the attribute modifier
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            AttributeModifier modifier = new AttributeModifier(
                    MODIFIER_UUID,
                    MODIFIER_NAME,
                    bonusHealth,
                    AttributeModifier.Operation.ADD_NUMBER
            );
            maxHealth.addModifier(modifier);
        }
    }

    /**
     * Removes the armor skill health bonus from a player.
     * Should be called on player quit.
     *
     * @param player The player to remove the bonus from
     */
    public static void removeHealthBonus(Player player) {
        if (player == null) return;

        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            // Remove modifier by UUID
            for (AttributeModifier modifier : maxHealth.getModifiers()) {
                if (modifier.getUniqueId().equals(MODIFIER_UUID)) {
                    maxHealth.removeModifier(modifier);
                    break;
                }
            }
        }
    }

    /**
     * Updates the health bonus for a player.
     * Call this when armor skill XP changes.
     *
     * @param player The player to update
     */
    public static void updateHealthBonus(Player player) {
        applyHealthBonus(player);
    }

    /**
     * Gets the current bonus health from armor skill for a player.
     *
     * @param player The player to check
     * @return The bonus health amount (in HP, not hearts)
     */
    public static double getBonusHealth(Player player) {
        if (player == null) return 0;

        long armorXP = PlayerData.getSkillXP(player.getUniqueId(), SkillType.ARMOR);
        int armorLevel = SkillXPCalculator.levelFromTotalXP(armorXP);

        if (armorLevel <= 1) return 0;
        return (armorLevel - 1) * 2.0;
    }

    /**
     * Gets the bonus health in hearts (for display purposes).
     *
     * @param player The player to check
     * @return The bonus health in hearts
     */
    public static int getBonusHearts(Player player) {
        return (int) (getBonusHealth(player) / 2.0);
    }
}
