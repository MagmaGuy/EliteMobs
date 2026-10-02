package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Arcing Bolt (PROC) - Missiles can arc to another nearby enemy.
 * Tier 2 unlock.
 * <p>
 * Power budget: the standard proc band, 25% at level 50 for an arc worth 80% of the hit
 * (E = 0.25 * 0.80 = 0.20). It needs a second enemy within 5 blocks of the target.
 */
public class ArcingBoltSkill extends MagicWeaponSkill implements ProcSkill {

    public static final String SKILL_ID = "wands_arcing_bolt";
    private static final double ARC_RANGE = 5.0;

    public ArcingBoltSkill() {
        super(SkillType.WANDS, 2, "Arcing Bolt", "Missiles can arc to another nearby enemy.",
                SkillBonusType.PROC, SKILL_ID);
    }

    @Override
    public boolean canProc(Player player, Object context) {
        return strike(context, MagicStrike.WAND_MISSILE) != null;
    }

    @Override
    public double getProcChance(int skillLevel) {
        return scaled(0.15, 0.002, 0.45, skillLevel); // 25% at level 50
    }

    private double arcFraction(int skillLevel) {
        return scaled(0.55, 0.005, skillLevel); // 80% at level 50
    }

    @Override
    public void onProc(Player player, Object context) {
        EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.WAND_MISSILE);
        if (event == null || event.getEliteMobEntity().getLivingEntity() == null) return;
        LivingEntity target = event.getEliteMobEntity().getLivingEntity();
        List<LivingEntity> nearby = enemiesNear(player, target.getLocation(), ARC_RANGE, target);
        if (nearby.isEmpty()) return;
        LivingEntity arcTarget = nearby.get(0);
        beam(chest(target), chest(arcTarget), Particle.ELECTRIC_SPARK);
        sideDamage(player, arcTarget, event.getDamage() * arcFraction(skillLevel(player)));
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100),
                "arcPercent", String.format("%.0f", arcFraction(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return arcFraction(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("arcPercent", String.format("%.0f", arcFraction(skillLevel) * 100)));
    }

    @Override
    public boolean affectsDamage() {
        return false; // The arc is a separate hit; the triggering missile is unchanged.
    }
}
