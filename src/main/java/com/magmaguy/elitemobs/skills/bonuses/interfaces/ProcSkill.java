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
}
