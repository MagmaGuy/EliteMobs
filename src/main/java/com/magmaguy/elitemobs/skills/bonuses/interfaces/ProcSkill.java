package com.magmaguy.elitemobs.skills.bonuses.interfaces;

import org.bukkit.entity.Player;

/**
 * Interface for PROC-type skills that trigger based on chance.
 * <p>
 * Proc skills have a percentage chance to activate on specific events
 * (e.g., on attack hit, on damage received, on kill).
 */
public interface ProcSkill {

    /**
     * Gets the proc chance at the given skill level.
     *
     * @param skillLevel The player's skill level
     * @return The chance to proc (0.0 to 1.0, where 1.0 = 100%)
     */
    double getProcChance(int skillLevel);

    /**
     * Called when the proc successfully triggers.
     *
     * @param player The player who triggered the proc
     * @param context The context object for this proc (varies by skill)
     */
    void onProc(Player player, Object context);

    /**
     * Minimum time in milliseconds that must pass between successive procs of
     * this skill for the same player. Returns 0 for no internal cooldown
     * (the default — proc is gated only by {@link #getProcChance(int)}).
     * <p>
     * Override for procs that deal large burst damage or have heavy side
     * effects, to prevent autoclickers from spamming them faster than a
     * legitimate attack cadence would allow.
     *
     * @return Internal proc cooldown in milliseconds; 0 disables the gate.
     */
    default long getInternalCooldownMillis() {
        return 0L;
    }
}
