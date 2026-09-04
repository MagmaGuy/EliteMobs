package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrenzyScalingPolicyTest {

    @Test
    void bloodragerFrenzyDurationRisesAcrossItsEffectiveLevelBand() {
        AbilityTuning frenzy = FixedAbilityRegistry.berserkerDefinitions()
                .get("bloodrager.signature")
                .tuning();

        assertEquals(108, FrenzyScalingPolicy.durationTicks(frenzy, 31));
        assertEquals(115, FrenzyScalingPolicy.durationTicks(frenzy, 60));
    }

    @Test
    void damageAndSpeedRiseContinuouslyWithMissingHealth() {
        FrenzyScalingPolicy.Scaling healthy = FrenzyScalingPolicy.scale(
                100D, 100D, .24D, .40D);
        FrenzyScalingPolicy.Scaling halfHealth = FrenzyScalingPolicy.scale(
                50D, 100D, .24D, .40D);
        FrenzyScalingPolicy.Scaling quarterHealth = FrenzyScalingPolicy.scale(
                25D, 100D, .24D, .40D);

        assertEquals(1D, healthy.damageMultiplier(), 1.0E-9D);
        assertEquals(0D, healthy.movementSpeedAdjustment(), 1.0E-9D);
        assertEquals(1.12D, halfHealth.damageMultiplier(), 1.0E-9D);
        assertEquals(.20D, halfHealth.movementSpeedAdjustment(), 1.0E-9D);
        assertEquals(1.18D, quarterHealth.damageMultiplier(), 1.0E-9D);
        assertEquals(.30D, quarterHealth.movementSpeedAdjustment(), 1.0E-9D);
        assertTrue(quarterHealth.damageMultiplier() > halfHealth.damageMultiplier());
        assertTrue(quarterHealth.movementSpeedAdjustment() > halfHealth.movementSpeedAdjustment());
    }
}
