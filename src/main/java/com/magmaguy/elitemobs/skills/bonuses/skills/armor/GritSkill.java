package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Tier 3 ARMOR skill - Grit
 * Provides scaling damage reduction when health is low
 */
public class GritSkill extends SkillBonus implements ConditionalSkill {

    private static final HashSet<UUID> activePlayers = new HashSet<>();
    private static final double HEALTH_THRESHOLD = 0.50; // 50% health

    public GritSkill() {
        super(
            SkillType.ARMOR,
            50,
            "Grit",
            "Gain increased damage reduction when below 50% health",
            SkillBonusType.CONDITIONAL,
            3,
            "grit"
        );
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        // Conditional bonus is checked on damage event
    }

    @Override
    public void removeBonus(Player player) {
        // No persistent bonus to remove
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        List<String> lore = new ArrayList<>();
        lore.add("Gain increased damage reduction when below 50% health");
        lore.add("Lower health = more damage reduction");
        lore.add(String.format("Max Reduction: %.1f%%", getMaxReduction(skillLevel) * 100));
        lore.add("Requires: Health below 50%");
        return lore;
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getScaledValue(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("Up to %.1f%% damage reduction", getMaxReduction(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }

    // ConditionalSkill interface methods

    @Override
    public boolean conditionMet(Player player, Object context) {
        if (!isActive(player)) {
            return false;
        }
        return player.getHealth() / player.getMaxHealth() < HEALTH_THRESHOLD;
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        // This will be scaled based on current health in modifyIncomingDamage
        return getScaledValue(skillLevel);
    }

    /**
     * Gets the maximum damage reduction at 0% health.
     *
     * @param skillLevel The player's skill level
     * @return The maximum reduction percentage
     */
    private double getMaxReduction(int skillLevel) {
        // Up to 50% damage reduction at 0% health
        return 0.5 * getScaledValue(skillLevel);
    }

    /**
     * Calculates damage reduction based on player's current health.
     * Called from damage event handler.
     *
     * @param player The player taking damage
     * @param originalDamage The original damage amount
     * @param context The damage event context
     * @return The modified damage amount
     */
    public double modifyIncomingDamage(Player player, double originalDamage, Object context) {
        if (!conditionMet(player, context)) {
            return originalDamage;
        }

        int skillLevel = getPlayerSkillLevel(player);

        // Calculate reduction based on how low health is
        // At 50% health: 0% reduction
        // At 0% health: full reduction (up to 50%)
        double healthPercent = player.getHealth() / player.getMaxHealth();
        double lowHealthMultiplier = (HEALTH_THRESHOLD - healthPercent) / HEALTH_THRESHOLD; // 0 at 50%, 1 at 0%

        double reduction = getMaxReduction(skillLevel) * lowHealthMultiplier;
        return originalDamage * (1 - reduction);
    }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
