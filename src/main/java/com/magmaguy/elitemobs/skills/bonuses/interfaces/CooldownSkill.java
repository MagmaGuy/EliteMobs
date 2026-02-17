package com.magmaguy.elitemobs.skills.bonuses.interfaces;

import org.bukkit.entity.Player;

/**
 * Interface for COOLDOWN-type skills that have internal cooldowns.
 * <p>
 * Cooldown skills can trigger their effect but must wait for a cooldown
 * period before they can trigger again (e.g., survive fatal damage once per minute).
 */
public interface CooldownSkill {

    /**
     * Gets the cooldown duration in seconds.
     *
     * @param skillLevel The player's skill level (cooldown may decrease with level)
     * @return The cooldown duration in seconds
     */
    long getCooldownSeconds(int skillLevel);

    /**
     * Checks if a player is currently on cooldown for this skill.
     *
     * @param player The player to check
     * @return true if the skill is on cooldown
     */
    boolean isOnCooldown(Player player);

    /**
     * Starts the cooldown for a player.
     *
     * @param player     The player to put on cooldown
     * @param skillLevel The player's skill level
     */
    void startCooldown(Player player, int skillLevel);

    /**
     * Gets the remaining cooldown time for a player.
     *
     * @param player The player to check
     * @return Remaining cooldown in seconds, or 0 if not on cooldown
     */
    long getRemainingCooldown(Player player);

    /**
     * Manually ends the cooldown for a player.
     *
     * @param player The player to remove from cooldown
     */
    void endCooldown(Player player);

    /**
     * Called when the cooldown skill activates.
     * This is the main effect method for cooldown skills.
     *
     * @param player The player activating the skill
     * @param event  The triggering event (can be null)
     */
    default void onActivate(Player player, Object event) {
        // Default no-op - skills override this or handle activation in their own event handlers
    }
}
