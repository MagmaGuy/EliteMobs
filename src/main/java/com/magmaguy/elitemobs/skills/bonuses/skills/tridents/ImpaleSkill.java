package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
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
 * Impale (PROC) - Trident attacks have a chance to deal massive bonus damage that ignores armor.
 * Tier 1 unlock.
 */
public class ImpaleSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "tridents_impale";
    private static final double BASE_PROC_CHANCE = 0.15; // 15% base chance
    private static final double BASE_DAMAGE_MULTIPLIER = 1.0;

    // Track which players have this skill active
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public ImpaleSkill() {
        super(SkillType.TRIDENTS, 10, "Impale",
              "Trident attacks have a chance to deal massive bonus damage that ignores armor.",
              SkillBonusType.PROC, 1, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        // Base chance + 0.15% per level, capped at 25%
        return Math.min(0.25, BASE_PROC_CHANCE + (skillLevel * 0.0015));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

    }

    private double calculateDamageMultiplier(int skillLevel) {
        if (configFields != null) {
            return BASE_DAMAGE_MULTIPLIER + configFields.calculateValue(skillLevel);
        }
        return BASE_DAMAGE_MULTIPLIER + (skillLevel * 0.02);
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
        return calculateDamageMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "multiplier", String.format("%.1f", calculateDamageMultiplier(skillLevel))
        ));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
