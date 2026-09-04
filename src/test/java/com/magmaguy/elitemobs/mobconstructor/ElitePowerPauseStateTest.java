package com.magmaguy.elitemobs.mobconstructor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElitePowerPauseStateTest {
    @Test
    void clearingInterruptCannotResumeAServicePausedBinding() {
        ElitePowerPauseState state = new ElitePowerPauseState();

        state.set(ElitePowerPauseReason.MIND_SERVICE, true);
        state.set(ElitePowerPauseReason.INTERRUPT, true);
        state.set(ElitePowerPauseReason.INTERRUPT, false);

        assertTrue(state.isPaused());
        assertTrue(state.has(ElitePowerPauseReason.MIND_SERVICE));
        assertFalse(state.has(ElitePowerPauseReason.INTERRUPT));

        state.set(ElitePowerPauseReason.MIND_SERVICE, false);
        assertFalse(state.isPaused());
    }
}
