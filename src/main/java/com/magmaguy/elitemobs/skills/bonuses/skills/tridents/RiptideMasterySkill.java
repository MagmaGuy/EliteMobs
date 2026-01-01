package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

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
 * Riptide Mastery (CONDITIONAL) - Bonus damage when in water or rain.
 * Extra bonus if both conditions are met.
 * Tier 3 unlock.
 */
public class RiptideMasterySkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "tridents_riptide_mastery";
    private static final double BASE_BONUS = 0.5;

    // Track which players have this skill active
    private static final Set<UUID> activePlayers = new HashSet<>();

    public RiptideMasterySkill() {
        super(SkillType.TRIDENTS, 50, "Riptide Mastery",
              "Bonus damage when in water or rain. Extra bonus if both conditions are met.",
              SkillBonusType.CONDITIONAL, 3, SKILL_ID);
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        // Active when in water or rain
        return player.isInWater() || player.getWorld().hasStorm();
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        return calculateBonus(skillLevel);
    }

    /**
     * Calculates the conditional bonus with extra multiplier if both conditions met.
     */
    private double calculateBonus(int skillLevel) {
        if (configFields != null) {
            return BASE_BONUS + configFields.calculateValue(skillLevel);
        }
        return BASE_BONUS + (skillLevel * 0.01);
    }

    /**
     * Gets the total bonus for a player, including the double bonus if both conditions are met.
     */
    public double getTotalBonus(Player player, int skillLevel) {
        if (!conditionMet(player, null)) return 0.0;

        double bonus = getConditionalBonus(skillLevel);

        // Extra bonus if both in water and raining
        if (player.isInWater() && player.getWorld().hasStorm()) {
            bonus *= 1.5;
        }

        return bonus;
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
        double bonus = getConditionalBonus(skillLevel) * 100;
        double doubleBonus = bonus * 1.5;
        return List.of(
                "&7Active: &fIn water or rain",
                "&7Damage Bonus: &f+" + String.format("%.1f", bonus) + "%",
                "&7Both conditions: &f+" + String.format("%.1f", doubleBonus) + "%"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.1f%% (Water/Rain)", getConditionalBonus(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
