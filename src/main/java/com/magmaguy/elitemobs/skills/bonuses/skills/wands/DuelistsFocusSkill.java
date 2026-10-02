package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Duelist's Focus (CONDITIONAL) - Missiles hit harder while you face your target alone.
 * Tier 2 unlock.
 * <p>
 * Active while no other enemy is within 12 blocks of you. Power budget: priced at 50% uptime
 * rather than the 26.7% conditional band, because boss rooms are often one-on-one, so +40% at
 * level 50 gives E = 0.50 * 0.40 = 0.20.
 */
public class DuelistsFocusSkill extends MagicWeaponSkill implements ConditionalSkill {

    public static final String SKILL_ID = "wands_duelists_focus";
    private static final double DUEL_RANGE = 12.0;

    public DuelistsFocusSkill() {
        super(SkillType.WANDS, 2, "Duelist's Focus", "Missiles hit harder when you face one enemy alone.",
                SkillBonusType.CONDITIONAL, SKILL_ID);
    }

    @Override
    public boolean conditionMet(Player player, Object context) {
        EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.WAND_MISSILE);
        if (event == null || event.getEliteMobEntity().getLivingEntity() == null) return false;
        return enemiesNear(player, player.getLocation(), DUEL_RANGE, event.getEliteMobEntity().getLivingEntity()).isEmpty();
    }

    @Override
    public double getConditionalBonus(int skillLevel) {
        return scaled(0.25, 0.003, skillLevel); // 40% at level 50
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "bonus", String.format("%.0f", getConditionalBonus(skillLevel) * 100),
                "range", String.format("%.0f", DUEL_RANGE)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getConditionalBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("bonus", String.format("%.0f", getConditionalBonus(skillLevel) * 100)));
    }
}
