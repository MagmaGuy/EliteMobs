package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Immolate (CONDITIONAL) - Fireballs deal bonus damage to badly wounded enemies.
 * Tier 3 unlock.
 * <p>
 * Power budget: the standard conditional band, the same execute window as Finishing Flourish -
 * a 1.75x hit at level 50 below 30% health (E = 0.267 * 0.75 = 0.20).
 */
public class ImmolateSkill extends MagicWeaponSkill implements ConditionalSkill {

    public static final String SKILL_ID = "staves_immolate";
    private static final double HEALTH_THRESHOLD = 0.30;

    public ImmolateSkill() {
        super(SkillType.STAVES, 3, "Immolate", "Fireballs deal bonus damage to badly wounded enemies.",
                SkillBonusType.CONDITIONAL, SKILL_ID);
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.STAFF_FIREBALL);
        if (event == null) return false;
        EliteEntity elite = event.getEliteMobEntity();
        return elite.getMaxHealth() > 0 && elite.getHealth() / elite.getMaxHealth() < HEALTH_THRESHOLD;
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        return scaled(0.45, 0.006, skillLevel); // 75% at level 50
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "bonus", String.format("%.0f", getConditionalBonus(skillLevel) * 100),
                "threshold", String.format("%.0f", HEALTH_THRESHOLD * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "bonus", String.format("%.0f", getConditionalBonus(skillLevel) * 100),
                "threshold", String.format("%.0f", HEALTH_THRESHOLD * 100)));
    }
}
