package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityCommitEffectsTest {
    @Test
    void resourceBurstIsNetPositiveAfterAuthoredCostAndContinuesPastOneHundred() {
        FixedAbilitySpec bloodRoar = BuiltInClassContent.abilityRegistry()
                .require("bloodrager.utility");

        AbilityCommitEffects at100 = AbilityCommitEffects.resolve(bloodRoar, 100);
        AbilityCommitEffects at101 = AbilityCommitEffects.resolve(bloodRoar, 101);

        assertTrue(at100.resourceGrant() > bloodRoar.resourceCost());
        assertEquals(.05D, at101.resourceGrant() - at100.resourceGrant(), 1.0E-9D);
    }

    @Test
    void quickloadOwnsARealScalingResourceBurstInsteadOfADeadCooldownRefund() {
        FixedAbilitySpec quickload = BuiltInClassContent.abilityRegistry()
                .require("artillerist.utility");

        AbilityCommitEffects at91 = AbilityCommitEffects.resolve(
                quickload,
                91);
        AbilityCommitEffects at100 = AbilityCommitEffects.resolve(
                quickload,
                100);
        assertTrue(at91.resourceGrant() > quickload.resourceCost());
        assertTrue(at100.resourceGrant() > at91.resourceGrant());

        String description = BuiltInClassContent.catalog().lineageOf("artillerist")
                .utility().description();
        assertTrue(description.contains("Focus"));
        assertFalse(description.toLowerCase().contains("hasten"));
    }
}
