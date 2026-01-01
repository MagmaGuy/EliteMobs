package com.magmaguy.elitemobs.skills.bonuses.interfaces;

import org.bukkit.entity.Player;

/**
 * Interface for TOGGLE-type skills that players can manually activate/deactivate.
 * <p>
 * Toggle skills have persistent effects while enabled, often with both
 * benefits and drawbacks (e.g., deal more damage but take more damage).
 */
public interface ToggleSkill {

    /**
     * Toggles the skill on or off for a player.
     *
     * @param player The player to toggle the skill for
     */
    void toggle(Player player);

    /**
     * Checks if the skill is currently toggled on for a player.
     *
     * @param player The player to check
     * @return true if the skill is currently active
     */
    boolean isToggled(Player player);

    /**
     * Enables the skill for a player.
     *
     * @param player The player to enable the skill for
     */
    void enable(Player player);

    /**
     * Disables the skill for a player.
     *
     * @param player The player to disable the skill for
     */
    void disable(Player player);

    /**
     * Gets the positive bonus value when toggled on.
     *
     * @param skillLevel The player's skill level
     * @return The positive bonus value
     */
    double getPositiveBonus(int skillLevel);

    /**
     * Gets the negative effect value when toggled on.
     * (e.g., increased damage taken)
     *
     * @param skillLevel The player's skill level
     * @return The negative effect value (penalty decreases with level typically)
     */
    double getNegativeEffect(int skillLevel);
}
