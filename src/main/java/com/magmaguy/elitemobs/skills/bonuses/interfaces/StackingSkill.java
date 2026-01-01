package com.magmaguy.elitemobs.skills.bonuses.interfaces;

import org.bukkit.entity.Player;

/**
 * Interface for STACKING-type skills that build up over actions.
 * <p>
 * Stacking skills accumulate stacks through specific actions and reset
 * under certain conditions (e.g., consecutive hits, time stationary).
 */
public interface StackingSkill {

    /**
     * Gets the maximum number of stacks this skill can accumulate.
     *
     * @return The maximum stack count
     */
    int getMaxStacks();

    /**
     * Gets the current number of stacks for a player.
     *
     * @param player The player to check
     * @return The current stack count (0 to getMaxStacks())
     */
    int getCurrentStacks(Player player);

    /**
     * Adds a stack for the player (up to max).
     *
     * @param player The player to add a stack to
     */
    void addStack(Player player);

    /**
     * Resets all stacks for a player.
     *
     * @param player The player to reset
     */
    void resetStacks(Player player);

    /**
     * Gets the bonus value per stack at the given skill level.
     *
     * @param skillLevel The player's skill level
     * @return The bonus per stack
     */
    double getBonusPerStack(int skillLevel);

    /**
     * Gets the total bonus based on current stacks.
     *
     * @param player     The player to calculate for
     * @param skillLevel The player's skill level
     * @return The total stacked bonus
     */
    default double getTotalStackBonus(Player player, int skillLevel) {
        return getCurrentStacks(player) * getBonusPerStack(skillLevel);
    }
}
