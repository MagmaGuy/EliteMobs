package com.magmaguy.elitemobs.advancedcombat.passives;

import org.junit.jupiter.api.Test;
import org.bukkit.potion.PotionEffect;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PassiveMechanicsTest {

    @Test
    void mechanicsComposeMultipliersAndAddBoundedFractions() {
        PassiveMechanics first = new PassiveMechanics(
                .9D, 1.1D, 1.08D, 1.2D, 1.15D, .85D, 1.25D, 1.1D,
                .9D, .25D, .04D);
        PassiveMechanics second = new PassiveMechanics(
                .8D, 1.05D, 1.1D, 1.1D, 1.2D, .9D, 1.2D, 1.15D,
                1.1D, .35D, .03D);

        PassiveMechanics combined = first.combine(second);

        assertEquals(.72D, combined.burstHealingMultiplier(), 1.0E-9D);
        assertEquals(1.155D, combined.periodicHealingMultiplier(), 1.0E-9D);
        assertEquals(1.188D, combined.groupedEnemyHealingMultiplier(), 1.0E-9D);
        assertEquals(1.32D, combined.periodicDurationMultiplier(), 1.0E-9D);
        assertEquals(1.38D, combined.shieldStrengthMultiplier(), 1.0E-9D);
        assertEquals(.765D, combined.redirectedDamageMultiplier(), 1.0E-9D);
        assertEquals(1.5D, combined.controlDurationMultiplier(), 1.0E-9D);
        assertEquals(1.265D, combined.controlPotencyMultiplier(), 1.0E-9D);
        assertEquals(.99D, combined.abilityCostMultiplier(), 1.0E-9D);
        assertEquals(.5125D, combined.controlResistanceFraction(), 1.0E-9D);
        assertEquals(.07D, combined.partyMovementSpeedAdjustment(), 1.0E-9D);
    }

    @Test
    void controlResistanceShortensControlAndKnockbackByTheSameRemainingFraction() {
        PassiveMechanics mechanics = new PassiveMechanics(
                1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D, .35D, 0D);

        assertEquals(65, PassiveRuntimePolicy.controlDurationTicks(100, mechanics));
        assertEquals(.65D, PassiveRuntimePolicy.knockbackMultiplier(mechanics), 1.0E-9D);
        assertEquals(PotionEffect.INFINITE_DURATION,
                PassiveRuntimePolicy.controlDurationTicks(PotionEffect.INFINITE_DURATION, mechanics));
    }

}
