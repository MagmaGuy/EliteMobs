package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Reap What You Sow (CONDITIONAL) - Risk/reward skill that grants bonus damage at low health.
 * The lower your health, the higher the damage bonus.
 * Tier 3 unlock.
 */
public class ReapWhatYouSowSkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "hoes_reap_what_you_sow";
    private static final double HEALTH_THRESHOLD = 0.50; // 50% health
    private static final double BASE_BONUS = 0.50; // 50% bonus at threshold

    private static final Set<UUID> activePlayers = new HashSet<>();

    public ReapWhatYouSowSkill() {
        super(SkillType.HOES, 50, "Reap What You Sow",
              "Deal more damage when you're at low health - risk for reward.",
              SkillBonusType.CONDITIONAL, 3, SKILL_ID);
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        return player.getHealth() / player.getMaxHealth() < HEALTH_THRESHOLD;
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        // Base 50% + 1% per level
        return BASE_BONUS + (skillLevel * 0.01);
    }

    /**
     * Calculates the scaled bonus based on player's current health.
     * Lower health = higher bonus.
     */
    public static double calculateRiskBonus(Player player, int skillLevel) {
        double healthPercent = player.getHealth() / player.getMaxHealth();

        if (healthPercent >= HEALTH_THRESHOLD) return 0.0;

        // Get base bonus
        SkillBonus skill = com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry.getSkillById(SKILL_ID);
        double baseBonus = 0.5;
        if (skill instanceof ReapWhatYouSowSkill reapSkill) {
            baseBonus = reapSkill.getConditionalBonus(skillLevel);
        }

        // Scale bonus: more bonus the lower the health
        // At 50% HP: 0% extra, At 0% HP: full bonus * 2
        double riskMultiplier = 1 + (HEALTH_THRESHOLD - healthPercent) * 2;
        return baseBonus * riskMultiplier;
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
        double bonusPercent = getConditionalBonus(skillLevel) * 100;
        double maxBonus = bonusPercent * 2; // Max at 0% HP
        return List.of(
                "&7Base Bonus: &f+" + String.format("%.0f", bonusPercent) + "%",
                "&7Max Bonus: &f+" + String.format("%.0f", maxBonus) + "% (at 0% HP)",
                "&7Condition: Below 50% health",
                "&7Risk equals reward"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.0f%% at Low HP", getConditionalBonus(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
