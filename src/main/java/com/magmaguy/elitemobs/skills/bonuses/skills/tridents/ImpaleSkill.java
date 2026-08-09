package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Impale (PROC) - Trident attacks have a chance to deal massive bonus damage.
 * Tier 1 unlock.
 * <p>
 * The class used to claim the bonus ignored armor. Nothing implemented that, and nothing could:
 * elite armor is purely cosmetic, and every vanilla damage modifier other than BASE is zeroed
 * before the formula runs (see {@code EliteMobDamagedByPlayerEvent.onEliteMobAttacked}), so
 * player-to-elite damage never passes through armor in the first place.
 */
public class ImpaleSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "tridents_impale";
    private static final double BASE_PROC_CHANCE = 0.15; // 15% base chance
    private static final double BASE_DAMAGE_MULTIPLIER = 1.29;

    // Track which players have this skill active
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public ImpaleSkill() {
        super(SkillType.TRIDENTS, 10, "Impale",
              "Trident attacks have a chance to deal massive bonus damage.",
              SkillBonusType.PROC, 1, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        if (configFields != null) return configFields.calculateProcChance(skillLevel);
        // Base chance + 0.15% per level, capped at 25%
        return scaled(BASE_PROC_CHANCE, 0.0015, 0.25, skillLevel);
    }

    @Override
    public void onProc(Player player, Object context) {
        // No side effect: the whole skill is the damage multiplier, which processOffensiveSkill
        // applies from getBonusValue on the same hit that rolled this proc.
    }

    private double calculateDamageMultiplier(int skillLevel) {
        if (configFields != null) return BASE_DAMAGE_MULTIPLIER + configFields.calculateValue(skillLevel);
        // Power budget: 22.5% proc rate at level 50 earns a 1.89x hit
        // (E = 0.225 * 0.89 = 0.20). Hardcoded rather than read from config so every server
        // runs the same numbers while the rebalance is being validated.
        return scaled(BASE_DAMAGE_MULTIPLIER, 0.012, skillLevel);
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
        double procChance = getProcChance(skillLevel) * 100;
        double multiplier = calculateDamageMultiplier(skillLevel);
        return applyLoreTemplates(Map.of(
                "procChance", String.format("%.1f", procChance),
                "multiplier", String.format("%.1f", multiplier)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        // Return the bonus portion only (e.g., 0.5 for a 1.5x multiplier).
        // processOffensiveSkill adds 1.0 + this, so total = the damage multiplier.
        return calculateDamageMultiplier(skillLevel) - 1.0;
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "multiplier", String.format("%.1f", calculateDamageMultiplier(skillLevel)),
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100)
        ));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
