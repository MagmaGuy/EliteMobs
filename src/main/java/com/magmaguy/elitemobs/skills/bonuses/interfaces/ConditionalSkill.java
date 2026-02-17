package com.magmaguy.elitemobs.skills.bonuses.interfaces;

import org.bukkit.entity.Player;

/**
 * Interface for CONDITIONAL-type skills that activate when specific conditions are met.
 * <p>
 * Conditional skills check for certain game states before applying their effects
 * (e.g., target HP below 30%, player stationary, 1v1 combat situation).
 */
public interface ConditionalSkill {

    /**
     * Checks if the skill's condition is currently met.
     *
     * @param player  The player using the skill
     * @param context The context object for evaluation (varies by skill)
     * @return true if the condition is met and the skill should apply
     */
    boolean conditionMet(Player player, Object context);

    /**
     * Gets the bonus value when the condition is met.
     *
     * @param skillLevel The player's skill level
     * @return The bonus value (interpretation depends on skill type)
     */
    double getConditionalBonus(int skillLevel);
}
