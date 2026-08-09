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

    /**
     * Attempts to activate the skill, reporting whether it actually fired.
     * <p>
     * A skill with its own gating condition (for example "target below 30% health") returns false
     * when that condition fails. The caller then consumes neither the cooldown nor the damage
     * bonus, so the condition genuinely governs the skill. Without this, a caller can only invoke
     * {@link #onActivate} and has no way to learn whether anything happened — which previously let
     * gated skills apply their damage bonus on every hit, bypassing their own condition entirely.
     * <p>
     * Skills that always fire when off cooldown need not override this.
     *
     * @param player The player activating the skill
     * @param event  The triggering event (can be null)
     * @return true if the skill actually fired
     */
    default boolean tryActivate(Player player, Object event) {
        onActivate(player, event);
        return true;
    }

    /**
     * Whether generic player-to-elite hit handling should trigger this cooldown skill.
     * Defensive cooldowns that activate from incoming damage should return false.
     *
     * @return true if outgoing hits can activate this skill
     */
    default boolean triggersOnOffensiveHit() {
        return true;
    }
}
