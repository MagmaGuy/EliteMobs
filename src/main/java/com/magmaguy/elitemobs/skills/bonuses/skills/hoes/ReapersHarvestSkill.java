package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Reaper's Harvest (CONDITIONAL) - Massive damage bonus against low health enemies.
 * Condition: Target below 25% health.
 * Tier 1 unlock.
 */
public class ReapersHarvestSkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "hoes_reapers_harvest";
    private static final double HEALTH_THRESHOLD = 0.25; // 25% health
    private static final double BASE_BONUS = 1.0; // 100% extra damage

    private static final Set<UUID> activePlayers = new HashSet<>();

    public ReapersHarvestSkill() {
        super(SkillType.HOES, 10, "Reaper's Harvest",
              "Deal massive bonus damage to enemies below 25% health.",
              SkillBonusType.CONDITIONAL, 1, SKILL_ID);
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return false;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return false;

        LivingEntity target = eliteEntity.getLivingEntity();
        return target.getHealth() / target.getMaxHealth() < HEALTH_THRESHOLD;
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        // Base 100% + 2% per level
        return BASE_BONUS + (skillLevel * 0.02);
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
        return List.of(
                "&7Bonus Damage: &f+" + String.format("%.0f", bonusPercent) + "%",
                "&7Condition: Target below 25% HP",
                "&7Execute weakened foes"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.0f%% Execute Damage", getConditionalBonus(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
