package com.magmaguy.elitemobs.advancedcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DamageReductionAttributionTest {

    @Test
    void reportsOnlyTheReductionThatSurvivesLaterMultipliers() {
        DamageReductionAttribution attribution = new DamageReductionAttribution(
                UUID.randomUUID(), UUID.randomUUID(), "justicar.utility",
                40D, 10D);

        assertEquals(5D, attribution.survivingPreventedDamage(20D, false), 1.0E-9D);
        assertEquals(20D, attribution.survivingPreventedDamage(80D, false), 1.0E-9D);
    }

    @Test
    void cancelledOrErasedDamageDoesNotProduceContributionEvidence() {
        DamageReductionAttribution attribution = new DamageReductionAttribution(
                UUID.randomUUID(), UUID.randomUUID(), "justicar.utility",
                40D, 10D);

        assertEquals(0D, attribution.survivingPreventedDamage(20D, true), 1.0E-9D);
        assertEquals(0D, attribution.survivingPreventedDamage(0D, false), 1.0E-9D);
    }
}
