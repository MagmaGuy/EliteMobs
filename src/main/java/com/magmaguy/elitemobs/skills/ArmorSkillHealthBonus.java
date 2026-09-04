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

import java.util.UUID;

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

        if (SkillsConfig.isWorldExcludedFromSkills(player)) {
            removeHealthBonus(player);
            resetPlayerHealthDisplay(player);
            clampHealthToCurrentMaxHealth(player);
            return;
        }

        if (!SkillsConfig.isArmorSkillHealthBonusEnabled()) {
            resetPlayerHealthDisplay(player);
            // Optional hard reset for servers that want to fully remove old extra-heart values.
            if (SkillsConfig.isForceDefaultHealthWhenArmorSkillHealthBonusDisabled()) {
                removeHealthBonus(player);
                forceVanillaMaxHealth(player);
            }
            return;
        }

        // During startup and /em reload, online-player hydration is asynchronous. The old modifier
        // is still the only authoritative value until PlayerDataLoadedEvent supplies the real XP.
        if (!PlayerData.isDataLoaded(player.getUniqueId())) return;

        // Get the player's armor skill level
        long armorXP = PlayerData.getSkillXP(player.getUniqueId(), SkillType.ARMOR);
        int armorLevel = SkillXPCalculator.levelFromTotalXP(armorXP);

        // No bonus at level 1 (base level)
        // Calculate bonus: +1 heart (2 HP) per level above 1.
        double bonusHealth = Math.max(0, armorLevel - 1) * 2.0;
        replaceHealthBonus(player, bonusHealth);

        updatePlayerHealthDisplay(player);
    }

    private static void updatePlayerHealthDisplay(Player player) {
        if (SkillsConfig.isScalePlayerHealthDisplayToVanilla()) {
            HealthDisplayCoordinator.acquire(player, HealthDisplayCoordinator.Owner.ARMOR_SKILL);
            return;
        }

        resetPlayerHealthDisplay(player);
    }

    /**
     * Clears the client-side health scaling applied by EliteMobs, if EliteMobs applied it.
     */
    public static void resetPlayerHealthDisplay(Player player) {
        if (player == null) return;
        HealthDisplayCoordinator.release(player, HealthDisplayCoordinator.Owner.ARMOR_SKILL);
    }

    private static void clampHealthToCurrentMaxHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) return;
        double currentMaxHealth = maxHealth.getValue();
        if (player.getHealth() > currentMaxHealth) {
            player.setHealth(currentMaxHealth);
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
        replaceHealthBonus(player, 0D);
    }

    private static void replaceHealthBonus(Player player, double bonusHealth) {
        if (player == null) return;
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) return;

        NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, MODIFIER_KEY_STRING);
        AttributeModifier existing = maxHealth.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(key))
                .findFirst()
                .orElse(null);
        boolean shouldExist = bonusHealth > 0D;
        if (!shouldExist && existing == null) return;
        if (shouldExist && existing != null && Math.abs(existing.getAmount() - bonusHealth) < 1.0E-9) return;

        HealthPercentagePreserver.during(player, maxHealth, () -> {
            if (existing != null) maxHealth.removeModifier(existing);
            if (shouldExist)
                maxHealth.addModifier(new AttributeModifier(
                        key,
                        bonusHealth,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.ANY));
        });
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
        if (SkillsConfig.isWorldExcludedFromSkills(player)) return 0;

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
     * Gets the max health value used by combat calculations for this player.
     * World exclusions disable armor skill health in the same way as disabling the mechanic.
     */
    public static double getConfiguredMaxHealthForPlayer(Player player, int armorLevel) {
        if (SkillsConfig.isWorldExcludedFromSkills(player)) return VANILLA_MAX_HEALTH;
        return getConfiguredMaxHealthForArmorLevel(armorLevel);
    }

    public static int getBonusHearts(Player player) {
        return (int) (getBonusHealth(player) / 2.0);
    }

}
