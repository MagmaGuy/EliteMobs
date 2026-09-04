package com.magmaguy.elitemobs.experimentalcombat.input;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.magmaguy.elitemobs.experimentalcombat.input.ClassControlMode.FAction.OPEN_ABILITIES;
import static com.magmaguy.elitemobs.experimentalcombat.input.ClassControlMode.FAction.PASS_THROUGH;
import static com.magmaguy.elitemobs.experimentalcombat.input.ClassControlMode.FAction.TOGGLED_OFF;
import static com.magmaguy.elitemobs.experimentalcombat.input.ClassControlMode.FAction.TOGGLED_ON;
import static com.magmaguy.elitemobs.experimentalcombat.input.ClassControlMode.FAction.TOGGLE_BLOCKED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassControlModeTest {

    private final UUID playerId = UUID.randomUUID();
    private final ClassControlMode controls = new ClassControlMode();

    @Test
    void singleSneakHeldFStaysAVanillaHandSwap() {
        assertEquals(PASS_THROUGH, controls.pressF(playerId, 100, true, false, true, true));
        assertFalse(controls.enabled(playerId, false, true));
    }

    @Test
    void sneakHeldDoubleFOutsideTogglesTheAbilityLayerOnAndOff() {
        assertEquals(PASS_THROUGH, controls.pressF(playerId, 100, true, false, true, true));
        assertEquals(TOGGLED_ON, controls.pressF(playerId, 105, true, false, true, true));
        assertTrue(controls.enabled(playerId, false, true));

        assertEquals(PASS_THROUGH, controls.pressF(playerId, 200, true, false, true, true));
        assertEquals(TOGGLED_OFF, controls.pressF(playerId, 206, true, false, true, true));
        assertFalse(controls.enabled(playerId, false, true));
    }

    @Test
    void secondTapBeyondTheQuickSuccessionWindowDoesNotToggle() {
        long lateTick = 300 + ClassControlMode.TOGGLE_TAP_WINDOW_TICKS + 1;

        assertEquals(PASS_THROUGH, controls.pressF(playerId, 300, true, false, true, true));
        assertEquals(PASS_THROUGH, controls.pressF(playerId, lateTick, true, false, true, true));
        assertFalse(controls.enabled(playerId, false, true));

        // The late tap re-arms the window, so a quick follow-up still completes the gesture.
        assertEquals(TOGGLED_ON, controls.pressF(playerId, lateTick + 3, true, false, true, true));
    }

    @Test
    void plainFBetweenTapsResetsTheDoubleTap() {
        assertEquals(PASS_THROUGH, controls.pressF(playerId, 400, true, false, true, true));
        assertEquals(PASS_THROUGH, controls.pressF(playerId, 402, false, false, true, true));
        assertEquals(PASS_THROUGH, controls.pressF(playerId, 404, true, false, true, true));
        assertFalse(controls.enabled(playerId, false, true));
    }

    @Test
    void plainFOutsideOpensAbilitiesOnlyWhileTheLayerIsEnabled() {
        assertEquals(PASS_THROUGH, controls.pressF(playerId, 500, false, false, true, true));

        controls.pressF(playerId, 510, true, false, true, true);
        assertEquals(TOGGLED_ON, controls.pressF(playerId, 512, true, false, true, true));

        assertEquals(OPEN_ABILITIES, controls.pressF(playerId, 520, false, false, true, true));
    }

    @Test
    void adminGateBlocksTheOutsideToggle() {
        assertEquals(PASS_THROUGH, controls.pressF(playerId, 600, true, false, false, true));
        assertEquals(TOGGLE_BLOCKED, controls.pressF(playerId, 602, true, false, false, true));
        assertFalse(controls.enabled(playerId, false, false));
    }

    @Test
    void disablingTheAdminGateRevokesAnExistingOutsideMode() {
        controls.pressF(playerId, 700, true, false, true, true);
        assertEquals(TOGGLED_ON, controls.pressF(playerId, 702, true, false, true, true));
        assertTrue(controls.enabled(playerId, false, true));

        assertFalse(controls.enabled(playerId, false, false));
        assertFalse(controls.enabled(playerId, false, true));
    }

    @Test
    void eligibleCombatContentIsAlwaysOnAndNeverToggles() {
        assertTrue(controls.enabled(playerId, true, false));

        assertEquals(OPEN_ABILITIES, controls.pressF(playerId, 800, true, true, false, true));
        assertEquals(OPEN_ABILITIES, controls.pressF(playerId, 802, true, true, false, true));
        assertTrue(controls.enabled(playerId, true, false));
    }

    @Test
    void playersWithoutFLayerSupportKeepVanillaHandSwapping() {
        assertEquals(PASS_THROUGH, controls.pressF(playerId, 900, true, false, true, false));
        assertEquals(PASS_THROUGH, controls.pressF(playerId, 902, true, false, true, false));
        assertFalse(controls.enabled(playerId, false, true));
    }

    @Test
    void clearRemovesTheSessionToggleAndAnyPendingTap() {
        controls.pressF(playerId, 1000, true, false, true, true);
        controls.clear(playerId);

        // The pending tap is gone, so this press arms a fresh window instead of completing one.
        assertEquals(PASS_THROUGH, controls.pressF(playerId, 1002, true, false, true, true));
        assertFalse(controls.enabled(playerId, false, true));
    }
}
