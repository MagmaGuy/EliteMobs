package com.magmaguy.elitemobs.experimentalcombat.minions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinionBehaviorPolicyTest {

    @Test
    void combatAlwaysPreemptsFollowingAndWandering() {
        assertEquals(MinionBehaviorPolicy.Intent.COMBAT,
                MinionBehaviorPolicy.choose(true, 20D, true));
    }

    @Test
    void followsOnlyWhenOwnerIsOutsideTheSettledRadius() {
        assertEquals(MinionBehaviorPolicy.Intent.FOLLOW,
                MinionBehaviorPolicy.choose(false, 8D, true));
        assertEquals(MinionBehaviorPolicy.Intent.WANDER,
                MinionBehaviorPolicy.choose(false, 2D, true));
    }

    @Test
    void localWanderRequiresAnExplicitSettledWindow() {
        assertEquals(MinionBehaviorPolicy.Intent.IDLE,
                MinionBehaviorPolicy.choose(false, 2D, false));
    }
}
