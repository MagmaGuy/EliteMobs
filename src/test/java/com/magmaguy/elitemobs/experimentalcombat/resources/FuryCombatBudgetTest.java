package com.magmaguy.elitemobs.experimentalcombat.resources;

import com.magmaguy.elitemobs.experimentalcombat.classes.ClassResourceType;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FuryCombatBudgetTest {

    @Test
    void representativeSustainedCombatFillsFuryInOneMinute() {
        ClassResourceDefinition fury = BuiltInClassContent.resourceDefinitions().get(ClassResourceType.FURY);

        assertEquals(100D, FuryCombatBudget.expectedGainPerMinute(fury), 1.0E-9D);
        assertEquals(FuryCombatBudget.TARGET_FILL_SECONDS,
                FuryCombatBudget.expectedFillSeconds(fury), 1.0E-9D);
    }

    @Test
    void furyStillComesFromBothDealingAndReceivingDamage() {
        ClassResourceDefinition fury = BuiltInClassContent.resourceDefinitions().get(ClassResourceType.FURY);

        assertEquals(FuryCombatBudget.DEALT_GAIN_PER_HEALTH_EQUIVALENT,
                fury.damageDealtHealthEquivalentGain());
        assertEquals(FuryCombatBudget.RECEIVED_GAIN_PER_HEALTH_EQUIVALENT,
                fury.damageReceivedHealthEquivalentGain());
        assertEquals(-15D, fury.outOfCombatTickDelta());
    }
}
