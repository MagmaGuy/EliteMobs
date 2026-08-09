package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Harvester (PASSIVE) - Passive damage and Elite Coin bonus.
 * Increases base damage and awards up to 20% more Elite Coins.
 * Tier 2 unlock.
 */
public class HarvesterSkill extends SkillBonus {

    public static final String SKILL_ID = "hoes_harvester";
    private static final double BASE_DAMAGE_BONUS = 0.10; // 10% damage
    private static final double MAX_REWARD_BONUS = 0.20;

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public HarvesterSkill() {
        super(SkillType.HOES, 25, "Harvester",
              "Deal more damage and reap up to 20% more Elite Coins.",
              SkillBonusType.PASSIVE, 2, SKILL_ID);
    }

    public double getDamageBonus(int skillLevel) {
        double approvedCurve = scaled(BASE_DAMAGE_BONUS, 0.001, skillLevel); // 20% at the level 100 soft cap
        if (configFields == null) return approvedCurve;
        double configuredBonus = configFields.calculateValue(skillLevel);
        if (!Double.isFinite(configuredBonus)) return approvedCurve;
        // Existing public configs used an older, over-budget curve. Preserve the ability to nerf
        // the skill locally without allowing stale values to exceed the approved damage curve.
        return Math.max(0D, Math.min(configuredBonus, approvedCurve));
    }

    public double getLootBonus(int skillLevel) {
        return Math.max(0D, Math.min(getDamageBonus(skillLevel), MAX_REWARD_BONUS));
    }

    /**
     * Returns the bounded reward multiplier used by Elite Coin payouts. This method is also the
     * safe integration point for any future chance-based reward: multiplying a 1% chance by 1.2
     * yields 1.2%, never a flat +20 percentage points.
     */
    public static double getLootMultiplier(Player player) {
        if (player == null || !activePlayers.contains(player.getUniqueId())) return 1.0;
        SkillBonus skill = com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry.getSkillById(SKILL_ID);
        if (!(skill instanceof HarvesterSkill harvester)) return 1.0;
        int skillLevel = com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry
                .getPlayerSkillLevel(player, SkillType.HOES);
        return 1.0 + harvester.getLootBonus(skillLevel);
    }

    /**
     * Applies Harvester to a concrete Elite Coin amount without ever exceeding the 20% cap.
     * Flooring keeps the payout integral and guarantees the awarded amount cannot round above the
     * advertised multiplier.
     */
    public static int applyEliteCoinBonus(Player player, int baseAmount) {
        if (baseAmount <= 0) return Math.max(0, baseAmount);
        return (int) Math.floor(baseAmount * getLootMultiplier(player));
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
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
                "damagePercent", String.format("%.1f", getDamageBonus(skillLevel) * 100),
                "lootPercent", String.format("%.1f", getLootBonus(skillLevel) * 100)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "damagePercent", String.format("%.1f", getDamageBonus(skillLevel) * 100),
                "lootPercent", String.format("%.1f", getLootBonus(skillLevel) * 100)
        ));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
