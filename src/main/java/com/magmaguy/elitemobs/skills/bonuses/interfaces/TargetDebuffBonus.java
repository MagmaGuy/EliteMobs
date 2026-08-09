package com.magmaguy.elitemobs.skills.bonuses.interfaces;

import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Cross-skill debuff bonus applied against a target that was previously debuffed (by this or any
 * player's earlier hits). Implementations own their marking state; the damage event only asks
 * whether the debuff applies and what it is worth.
 */
public interface TargetDebuffBonus {

    /**
     * Whether this debuff bonus applies to the given target for the given attacker.
     */
    boolean appliesTo(LivingEntity target, Player attacker);

    /**
     * The skill whose level scales this bonus for the attacker.
     */
    SkillType levelSource();

    /**
     * The additive bonus fraction for this attack (e.g. 0.25 for +25% damage).
     */
    double bonusFor(Player attacker, LivingEntity target, int level);

    /**
     * The literal label used in the skill bonus debug log, including the trailing '='.
     */
    String debugLabel();
}
