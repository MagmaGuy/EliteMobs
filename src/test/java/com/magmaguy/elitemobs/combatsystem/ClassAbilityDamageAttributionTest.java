package com.magmaguy.elitemobs.combatsystem;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassAbilityDamageAttributionTest {
    private static final double TOLERANCE = 1.0E-9D;

    @Test
    void reportsTheExactDamageAddedByStackedAbilityMultipliers() {
        ClassAbilityDamageAttribution attribution = new ClassAbilityDamageAttribution();

        double damage = attribution.applyMultiplier("berserker.signature", 10D, 1.5D);
        damage = attribution.applyMultiplier("ranger.utility", damage, 1.2D);

        assertEquals(18D, damage, TOLERANCE);
        assertEquals(8D, attribution.bonusDamage(damage), TOLERANCE);
        assertEquals(Set.of("berserker.signature", "ranger.utility"), attribution.sources());
    }

    @Test
    void laterDamageScalingKeepsTheDisplayedContributionReconciledWithTheTotal() {
        ClassAbilityDamageAttribution attribution = new ClassAbilityDamageAttribution();
        double buffed = attribution.applyMultiplier("berserker.signature", 10D, 1.5D);

        double finalDamage = buffed * 1.2D;
        attribution.observeReplacement(buffed, finalDamage);

        assertEquals(18D, finalDamage, TOLERANCE);
        assertEquals(6D, attribution.bonusDamage(finalDamage), TOLERANCE);
    }

    @Test
    void ordinaryDamageHasNoClassAbilityContribution() {
        ClassAbilityDamageAttribution attribution = new ClassAbilityDamageAttribution();

        attribution.observeReplacement(10D, 20D);

        assertEquals(0D, attribution.bonusDamage(20D), TOLERANCE);
        assertEquals(Set.of(), attribution.sources());
    }
}
