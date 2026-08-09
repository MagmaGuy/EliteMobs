package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tier 3 ARMOR skill - Grit
 * Provides scaling damage reduction when health is low
 */
public class GritSkill extends SkillBonus implements ConditionalSkill {

    /**
     * Peak damage reduction, reached at 0% health, on the shared defensive power budget.
     * <p>
     * Sustained power is {@code E = uptime * reduction} and the budget is 0.20. Grit only pays out
     * below {@link #HEALTH_THRESHOLD} (roughly half of a real fight) and ramps linearly from nothing
     * at the threshold to this peak at 0 health, which halves it again: effective uptime 0.25, so
     * the fair peak is {@code 0.20 / 0.25 = 0.80}. That lands exactly on
     * {@link SkillBonus#MAX_DEFENSIVE_REDUCTION}, which is the most a single skill is allowed to take
     * off a hit — Grit is the one armor skill the per-skill clamp binds by design. Hardcoded on
     * purpose: balance values no longer come from config, only presentation does.
     */
    private static final double MAX_REDUCTION = 0.80;

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final double HEALTH_THRESHOLD = 0.50; // 50% health

    public GritSkill() {
        super(
            SkillType.ARMOR,
            50,
            "Grit",
            "Gain increased damage reduction when below 50% health",
            SkillBonusType.CONDITIONAL,
            3,
            "armor_grit"
        );
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
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
        return applyLoreTemplates(Map.of(
                "maxReduction", String.format("%.1f", getMaxReduction(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "maxReduction", String.format("%.1f", getMaxReduction(skillLevel) * 100)));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }

    // ConditionalSkill interface methods

    @Override
    public boolean conditionMet(Player player, Object context) {
        double damage = context instanceof Number ? ((Number) context).doubleValue() : 0;
        return (player.getHealth() - damage) / player.getMaxHealth() < HEALTH_THRESHOLD;
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        // Peak value; scaled down by current health in modifyIncomingDamage
        return getMaxReduction(skillLevel);
    }

    /**
     * Gets the maximum damage reduction at 0% health.
     *
     * @param skillLevel The player's skill level
     * @return The maximum reduction percentage
     */
    private double getMaxReduction(int skillLevel) {
        double configuredReduction = configFields == null
                ? MAX_REDUCTION
                : 0.5 * configFields.calculateValue(skillLevel);
        return clampDefensiveReduction(configuredReduction);
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
        if (!conditionMet(player, originalDamage)) {
            return originalDamage;
        }

        int skillLevel = getPlayerSkillLevel(player);

        // Calculate reduction based on how low health would be after damage
        // At 50% health: 0% reduction
        // At 0% health: full reduction (up to 50%)
        double postDamageHealth = Math.max(0, player.getHealth() - originalDamage);
        double healthPercent = postDamageHealth / player.getMaxHealth();
        double lowHealthMultiplier = (HEALTH_THRESHOLD - healthPercent) / HEALTH_THRESHOLD; // 0 at 50%, 1 at 0%

        // Clamped again after health scaling so the applied reduction can never invert the damage
        double reduction = clampDefensiveReduction(getMaxReduction(skillLevel) * lowHealthMultiplier);
        return originalDamage * (1 - reduction);
    }

    @Override
    public TestStrategy getTestStrategy() { return TestStrategy.CONDITION_SETUP; }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
