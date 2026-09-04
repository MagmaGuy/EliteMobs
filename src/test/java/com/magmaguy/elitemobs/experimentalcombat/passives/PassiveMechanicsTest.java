package com.magmaguy.elitemobs.experimentalcombat.passives;

import com.magmaguy.elitemobs.skills.SkillType;
import org.junit.jupiter.api.Test;
import org.bukkit.potion.PotionEffect;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassiveMechanicsTest {

    @Test
    void neutralMechanicsDoNotAlterAbilitiesOrMovementControl() {
        PassiveMechanics mechanics = PassiveMechanics.NEUTRAL;

        assertEquals(1D, mechanics.burstHealingMultiplier());
        assertEquals(1D, mechanics.periodicHealingMultiplier());
        assertEquals(1D, mechanics.groupedEnemyHealingMultiplier());
        assertEquals(1D, mechanics.periodicDurationMultiplier());
        assertEquals(1D, mechanics.shieldStrengthMultiplier());
        assertEquals(1D, mechanics.redirectedDamageMultiplier());
        assertEquals(1D, mechanics.controlDurationMultiplier());
        assertEquals(1D, mechanics.controlPotencyMultiplier());
        assertEquals(1D, mechanics.abilityCostMultiplier());
        assertEquals(0D, mechanics.controlResistanceFraction());
        assertEquals(0D, mechanics.partyMovementSpeedAdjustment());
    }

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

    @Test
    void spellIdentityIncludesOnlyClassAbilitiesAndTheTwoMagicWeapons() {
        assertTrue(PassiveRuntimePolicy.isSpellDamage(true, null));
        assertTrue(PassiveRuntimePolicy.isSpellDamage(false, SkillType.WANDS));
        assertTrue(PassiveRuntimePolicy.isSpellDamage(false, SkillType.STAVES));
        assertFalse(PassiveRuntimePolicy.isSpellDamage(false, SkillType.SWORDS));
        assertFalse(PassiveRuntimePolicy.isSpellDamage(false, null));
    }

    @Test
    void partyMovementUsesTheStrongestNearbyAuraWithoutStackingOrSoloLeakage() {
        PassiveMechanics weaker = new PassiveMechanics(
                1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D, 0D, .04D);
        PassiveMechanics stronger = new PassiveMechanics(
                1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D, 0D, .07D);

        assertEquals(.07D,
                PassiveRuntimePolicy.partyMovementAdjustment(List.of(weaker, stronger), true),
                1.0E-9D);
        assertEquals(0D,
                PassiveRuntimePolicy.partyMovementAdjustment(List.of(stronger), false),
                1.0E-9D);
    }
}
