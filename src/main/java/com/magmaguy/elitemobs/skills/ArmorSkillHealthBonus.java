package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

/**
 * Applies bonus max health to players based on their Armor skill level.
 * Players receive +1 heart (2 HP) per Armor skill level.
 */
public class ArmorSkillHealthBonus {

    private static final String MODIFIER_KEY_STRING = "armor_skill_health";
    private static final double VANILLA_MAX_HEALTH = 20.0;

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

        if (!SkillsConfig.isArmorSkillHealthBonusEnabled()) {
            // Optional hard reset for servers that want to fully remove old extra-heart values.
            if (SkillsConfig.isForceDefaultHealthWhenArmorSkillHealthBonusDisabled()) {
                removeHealthBonus(player);
                forceVanillaMaxHealth(player);
            }
            return;
        }

        // First remove any existing modifier to avoid duplicates/stale values
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
            NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, MODIFIER_KEY_STRING);
            maxHealth.addModifier(new AttributeModifier(key, bonusHealth, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
        }
    }

    private static void forceVanillaMaxHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) return;
        maxHealth.setBaseValue(VANILLA_MAX_HEALTH);
        if (player.getHealth() > VANILLA_MAX_HEALTH) {
            player.setHealth(VANILLA_MAX_HEALTH);
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
            for (AttributeModifier modifier : maxHealth.getModifiers()) {
                if (modifier.getKey().equals(new NamespacedKey(MetadataHandler.PLUGIN, MODIFIER_KEY_STRING))) {
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
        if (!SkillsConfig.isArmorSkillHealthBonusEnabled()) return 0;

        long armorXP = PlayerData.getSkillXP(player.getUniqueId(), SkillType.ARMOR);
        int armorLevel = SkillXPCalculator.levelFromTotalXP(armorXP);

        if (armorLevel <= 1) return 0;
        return (armorLevel - 1) * 2.0;
    }

    /**
     * Gets the max health value used by armor skill progression for combat calculations.
     * Returns vanilla max health when the mechanic is disabled.
     */
    public static double getConfiguredMaxHealthForArmorLevel(int armorLevel) {
        if (!SkillsConfig.isArmorSkillHealthBonusEnabled()) return VANILLA_MAX_HEALTH;
        return VANILLA_MAX_HEALTH + Math.max(0, armorLevel - 1) * 2.0;
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
