package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Riptide Mastery (CONDITIONAL) - Bonus damage when in water or rain.
 * Extra bonus if both conditions are met.
 * Tier 3 unlock.
 */
public class RiptideMasterySkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "tridents_riptide_mastery";
    // Budgeted against how often the condition ACTUALLY holds. "In water OR storming" is trivially
    // satisfiable - stand in a pond - so realistic uptime is ~60%. Splitting that into the plain
    // case and the 1.5x water-AND-storm case (in water ~45%, storming ~25%, so both ~11%) gives
    // E = 0.49*b + 0.11*1.5b = 0.655*b; b = 0.31 at level 50 lands E = 0.20. The base was 0.18
    // (1.33x) while the 1.5x branch was dead code that never reached a damage calculation; making
    // it live would have pushed the skill over budget at its old value.
    private static final double BASE_BONUS = 0.16;

    // Track which players have this skill active
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

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

    @Override
    public double getConditionalBonus(Player player, int skillLevel) {
        return getTotalBonus(player, skillLevel);
    }

    /**
     * Base conditional bonus, before the water-AND-storm uplift in
     * {@link #getTotalBonus(Player, int)}.
     */
    private double calculateBonus(int skillLevel) {
        if (configFields != null) return BASE_BONUS + configFields.calculateValue(skillLevel);
        return scaled(BASE_BONUS, 0.003, skillLevel); // 16% base + 0.3% per level -> 1.31x at level 50
    }

    /**
     * Gets the total bonus for a player, including the extra bonus if both conditions are met.
     * <p>
     * Called from the CONDITIONAL branch of
     * {@link com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent#processOffensiveSkill}, which
     * has the player in hand; the interface's level-only {@code getConditionalBonus} cannot see
     * whether the player is standing in water during a storm and so never applied the 1.5x.
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
        return applyLoreTemplates(Map.of(
                "damageBonus", String.format("%.1f", bonus),
                "doubleBonus", String.format("%.1f", doubleBonus)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "damageBonus", String.format("%.1f", getConditionalBonus(skillLevel) * 100)
        ));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
