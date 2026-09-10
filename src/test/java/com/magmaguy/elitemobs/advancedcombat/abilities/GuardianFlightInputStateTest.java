package com.magmaguy.elitemobs.advancedcombat.abilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuardianFlightInputStateTest {

    @Test
    void jumpAndSneakCancelOnlyOnTheirRisingEdges() {
        GuardianFlightInputState state = new GuardianFlightInputState(false, false);

        GuardianFlightInputState.Transition jump = state.update(true, false);
        assertEquals(GuardianFlightInputState.Intent.FORWARD_CANCEL, jump.intent());
        assertEquals(GuardianFlightInputState.Intent.NONE, jump.next().update(true, false).intent());

        GuardianFlightInputState released = jump.next().update(false, false).next();
        GuardianFlightInputState.Transition sneak = released.update(false, true);
        assertEquals(GuardianFlightInputState.Intent.UPWARD_CANCEL, sneak.intent());
        assertEquals(GuardianFlightInputState.Intent.NONE, sneak.next().update(false, true).intent());
    }

    @Test
    void simultaneousPressUsesTheExplicitJumpCancel() {
        GuardianFlightInputState.Transition transition =
                new GuardianFlightInputState(false, false).update(true, true);
        assertEquals(GuardianFlightInputState.Intent.FORWARD_CANCEL, transition.intent());
    }
}
