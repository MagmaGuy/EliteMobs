package com.magmaguy.elitemobs.experimentalcombat.resources;

import com.magmaguy.elitemobs.experimentalcombat.classes.ClassResourceType;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassiveResourceBudgetTest {

    @Test
    void emptyManaBarTakesOneMinuteToRefillInCombat() {
        ClassResourceDefinition mana = definition(ClassResourceType.MANA);

        assertEquals(60D,
                PassiveResourceBudget.secondsToAfford(mana, 0D, mana.maximum(), true), 1.0E-9D);
        assertEquals(60D,
                PassiveResourceBudget.secondsToAfford(mana, 0D, mana.maximum(), false), 1.0E-9D);
    }

    @Test
    void resolvePassivelyRecoversAtOneThirdManaRate() {
        ClassResourceDefinition resolve = definition(ClassResourceType.RESOLVE);
        ClassResourceDefinition mana = definition(ClassResourceType.MANA);

        assertEquals(mana.inCombatTickDelta() / 3D, resolve.inCombatTickDelta(), 1.0E-9D);
        assertEquals(mana.outOfCombatTickDelta() / 3D, resolve.outOfCombatTickDelta(), 1.0E-9D);
        assertEquals(180D, PassiveResourceBudget.secondsToFill(resolve, true), 1.0E-9D);
        assertEquals(180D, PassiveResourceBudget.secondsToFill(resolve, false), 1.0E-9D);
    }

    @Test
    void passiveResolveFundsPaladinMobilityWithoutRemovingDefensiveIncome() {
        ClassResourceDefinition resolve = definition(ClassResourceType.RESOLVE);
        double mobilityCost = BuiltInClassContent.abilityRegistry()
                .require("paladin.mobility").resourceCost();

        assertEquals(55D, mobilityCost);
        assertEquals(99D, PassiveResourceBudget.secondsToAfford(resolve, 0D, mobilityCost, true), 1.0E-9D);
        assertEquals(99D, PassiveResourceBudget.secondsToAfford(resolve, 0D, mobilityCost, false), 1.0E-9D);
        assertTrue(resolve.damageReceivedHealthEquivalentGain() > 0D);
        assertTrue(resolve.preventedDamageHealthEquivalentGain() > 0D);
        assertTrue(resolve.tauntGainPerEnemy() > 0D);
    }

    private static ClassResourceDefinition definition(ClassResourceType type) {
        return BuiltInClassContent.resourceDefinitions().get(type);
    }
}
