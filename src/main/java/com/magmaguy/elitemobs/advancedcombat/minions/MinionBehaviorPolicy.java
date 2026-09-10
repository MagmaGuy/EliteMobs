package com.magmaguy.elitemobs.advancedcombat.minions;

/** Pure arbitration shared by the native behaviors and unit tests. */
public final class MinionBehaviorPolicy {
    static final double SETTLED_RADIUS = 4D;

    private MinionBehaviorPolicy() {
    }

    public static Intent choose(boolean combatTargetAvailable, double ownerDistance, boolean wanderReady) {
        if (combatTargetAvailable) return Intent.COMBAT;
        if (Double.isFinite(ownerDistance) && ownerDistance > SETTLED_RADIUS) return Intent.FOLLOW;
        if (wanderReady) return Intent.WANDER;
        return Intent.IDLE;
    }

    public enum Intent {
        COMBAT,
        FOLLOW,
        WANDER,
        IDLE
    }
}
