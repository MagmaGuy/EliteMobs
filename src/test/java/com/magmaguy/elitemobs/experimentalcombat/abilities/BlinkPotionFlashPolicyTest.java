package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;
import org.bukkit.potion.PotionEffect;

import static com.magmaguy.elitemobs.experimentalcombat.abilities.BlinkPotionFlashPolicy.CleanupAction.NOOP;
import static com.magmaguy.elitemobs.experimentalcombat.abilities.BlinkPotionFlashPolicy.CleanupAction.REMOVE_FLASH;
import static com.magmaguy.elitemobs.experimentalcombat.abilities.BlinkPotionFlashPolicy.CleanupAction.RESTORE_PREVIOUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlinkPotionFlashPolicyTest {
    private static final BlinkPotionFlashPolicy.EffectState FLASH =
            new BlinkPotionFlashPolicy.EffectState(2, 1, false, false, false);

    @Test
    void flashesOnlyAfterSuccessfulNonZeroMovement() {
        assertTrue(BlinkPotionFlashPolicy.shouldFlash(true, 36D));
        assertFalse(BlinkPotionFlashPolicy.shouldFlash(false, 36D));
        assertFalse(BlinkPotionFlashPolicy.shouldFlash(true, 0D));
        assertFalse(BlinkPotionFlashPolicy.shouldFlash(true, Double.NaN));
    }

    @Test
    void restoresThePreexistingEffectAfterTheOwnedFlash() {
        BlinkPotionFlashPolicy.EffectState previous =
                new BlinkPotionFlashPolicy.EffectState(1, 200, true, true, true);
        assertEquals(RESTORE_PREVIOUS, BlinkPotionFlashPolicy.cleanup(
                4L, 4L, false, true, FLASH, FLASH, previous));
    }

    @Test
    void removesOnlyAnOwnedFlashWhenThereWasNoPreviousEffect() {
        assertEquals(REMOVE_FLASH, BlinkPotionFlashPolicy.cleanup(
                4L, 4L, false, true, FLASH, FLASH, null));
        assertEquals(NOOP, BlinkPotionFlashPolicy.cleanup(
                4L, 4L, false, true, FLASH, null, null));
    }

    @Test
    void staleCleanupAndExternalChangesNeverOverwriteNewerState() {
        BlinkPotionFlashPolicy.EffectState external =
                new BlinkPotionFlashPolicy.EffectState(4, 400, false, true, true);
        assertEquals(NOOP, BlinkPotionFlashPolicy.cleanup(
                3L, 4L, false, true, FLASH, FLASH, null));
        assertEquals(NOOP, BlinkPotionFlashPolicy.cleanup(
                4L, 4L, true, true, FLASH, FLASH, null));
        assertEquals(NOOP, BlinkPotionFlashPolicy.cleanup(
                4L, 4L, false, true, FLASH, external, null));
    }

    @Test
    void strongerExistingEffectThatWasNeverReplacedNeedsNoCleanup() {
        assertEquals(NOOP, BlinkPotionFlashPolicy.cleanup(
                4L, 4L, false, false, FLASH, FLASH, FLASH));
    }

    @Test
    void anEffectThatExpiresDuringTheFlashIsNotResurrected() {
        assertEquals(0, BlinkPotionFlashPolicy.restoredDuration(1, false, 1));
        assertEquals(0, BlinkPotionFlashPolicy.restoredDuration(1, false, 5));
    }

    @Test
    void repeatedFlashUsesElapsedTimeFromTheOriginalBaseline() {
        long originalBaselineTick = 100L;
        long secondFlashCleanupTick = 111L;
        assertEquals(189, BlinkPotionFlashPolicy.restoredDuration(
                200, false, secondFlashCleanupTick - originalBaselineTick));
        assertEquals(PotionEffect.INFINITE_DURATION,
                BlinkPotionFlashPolicy.restoredDuration(
                        PotionEffect.INFINITE_DURATION, true, 500L));
    }
}
