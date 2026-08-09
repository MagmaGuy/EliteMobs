package com.magmaguy.elitemobs.utils;

import org.bukkit.entity.EnderDragon;

public class EnderDragonPhaseSimplifier {
    private EnderDragonPhaseSimplifier() {
    }

    /**
     * Returns whether the dragon's phase has it flying. Strict considers edge behaviors as true (charging, landing and taking off)
     *
     * @param phase  Phase to evaluate
     * @param strict If true, includes all flying phases, including things such as charging players and returning to the portal
     * @return Returns whether the dragon is flying
     */
    public static boolean isFlying(EnderDragon.Phase phase, boolean strict) {
        switch (phase) {
            case HOVER:
            case CIRCLING:
            case STRAFING:
                return true;
            default:
                if (strict)
                    return phase.equals(EnderDragon.Phase.LAND_ON_PORTAL) ||
                            phase.equals(EnderDragon.Phase.LEAVE_PORTAL) ||
                            phase.equals(EnderDragon.Phase.CHARGE_PLAYER);
                return false;

        }
    }

}

