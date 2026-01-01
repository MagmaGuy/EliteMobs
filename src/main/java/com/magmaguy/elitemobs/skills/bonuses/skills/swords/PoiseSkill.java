package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Poise (PASSIVE) - Reduces knockback taken when using swords.
 * Always active when selected.
 * Tier 1 unlock.
 */
public class PoiseSkill extends SkillBonus {

    public static final String SKILL_ID = "swords_poise";
    private static final double BASE_KNOCKBACK_REDUCTION = 0.20; // 20% reduction

    private static final Set<UUID> activePlayers = new HashSet<>();

    public PoiseSkill() {
        super(SkillType.SWORDS, 10, "Poise",
              "Reduces knockback taken while wielding a sword.",
              SkillBonusType.PASSIVE, 1, SKILL_ID);
    }

    /**
     * Gets the knockback reduction percentage for a skill level.
     */
    public static double getKnockbackReduction(int skillLevel) {
        // Base 20% + 0.3% per level, max 50%
        return Math.min(0.50, BASE_KNOCKBACK_REDUCTION + (skillLevel * 0.003));
    }

    /**
     * Checks if a player has this skill active.
     */
    public static boolean hasActiveSkill(UUID playerUUID) {
        return activePlayers.contains(playerUUID);
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
        double reduction = getKnockbackReduction(skillLevel) * 100;
        return List.of(
                "&7Knockback Reduction: &f" + String.format("%.1f", reduction) + "%",
                "&7Always active when selected"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getKnockbackReduction(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("-%.1f%% Knockback", getKnockbackReduction(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
