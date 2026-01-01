package com.magmaguy.elitemobs.skills.bonuses.skills.axes;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Executioner (CONDITIONAL) - Bonus damage to enemies below 40% HP.
 * Tier 2 unlock.
 */
public class ExecutionerSkill extends SkillBonus implements ConditionalSkill {

    public static final String SKILL_ID = "axes_executioner";
    private static final double BASE_DAMAGE_BONUS = 0.40;
    private static final double HEALTH_THRESHOLD = 0.40;

    private static final Set<UUID> activePlayers = new HashSet<>();

    public ExecutionerSkill() {
        super(SkillType.AXES, 25, "Executioner",
              "Deal bonus damage to enemies below 40% health.",
              SkillBonusType.CONDITIONAL, 2, SKILL_ID);
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return false;
        EliteEntity target = event.getEliteMobEntity();
        if (target == null || target.getLivingEntity() == null) return false;
        LivingEntity entity = target.getLivingEntity();
        return entity.getHealth() / entity.getMaxHealth() <= HEALTH_THRESHOLD;
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        return BASE_DAMAGE_BONUS + (skillLevel * 0.008);
    }

    public static double applyExecuteBonus(Player player, EliteMobDamagedByPlayerEvent event, double damage) {
        if (!activePlayers.contains(player.getUniqueId())) return damage;
        EliteEntity target = event.getEliteMobEntity();
        if (target == null || target.getLivingEntity() == null) return damage;
        LivingEntity entity = target.getLivingEntity();
        if (entity.getHealth() / entity.getMaxHealth() > HEALTH_THRESHOLD) return damage;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.AXES);
        double bonus = BASE_DAMAGE_BONUS + (skillLevel * 0.008);
        return damage * (1 + bonus);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void removeBonus(Player player) { activePlayers.remove(player.getUniqueId()); }
    @Override
    public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void onDeactivate(Player player) { activePlayers.remove(player.getUniqueId()); }
    @Override
    public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return List.of(
                "&7Execute Bonus: &f+" + String.format("%.0f", getConditionalBonus(skillLevel) * 100) + "%",
                "&7Threshold: &fBelow 40% HP"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) { return getConditionalBonus(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) { return String.format("+%.0f%% Execute Damage", getConditionalBonus(skillLevel) * 100); }
    @Override
    public void shutdown() { activePlayers.clear(); }
}
