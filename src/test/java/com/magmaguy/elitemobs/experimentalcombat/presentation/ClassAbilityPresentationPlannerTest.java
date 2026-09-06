package com.magmaguy.elitemobs.experimentalcombat.presentation;

import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilityDefinition;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassAbilityPresentationPlannerTest {

    @Test
    void blinkTraceSamplesTheWholeRouteWithoutUnboundedParticleWork() {
        AbilityPresentationPlan blink = plan("spellcaster", AbilitySlot.MOBILITY);

        List<Double> sixBlockTrace = blink.traceProgress(6D);
        assertEquals(12, sixBlockTrace.size());
        assertTrue(sixBlockTrace.getFirst() > 0D);
        assertTrue(sixBlockTrace.getLast() < 1D);
        for (int index = 1; index < sixBlockTrace.size(); index++)
            assertTrue(sixBlockTrace.get(index) > sixBlockTrace.get(index - 1));

        assertEquals(18, blink.traceProgress(100D).size());
        assertTrue(blink.traceProgress(0D).isEmpty());
    }

    @Test
    void oneCastCannotSpendPastItsParticleSoundOrTargetBurstBudget() {
        AbilityPresentationPlan plan = plan("pyromancer", AbilitySlot.SIGNATURE);
        AbilityPresentationBudgetLedger ledger = plan.newBudgetLedger();
        int particles = 0;
        int sounds = 0;
        int targetBursts = 0;

        for (int index = 0; index < 30; index++) {
            AbilityPresentationBudgetLedger.Grant grant =
                    ledger.claim(AbilityPresentationCue.IMPACT, 20, 1);
            particles += grant.particles();
            sounds += grant.sounds();
            if (grant.granted()) targetBursts++;
        }

        assertEquals(plan.budget().particleLimit(), particles);
        assertEquals(plan.budget().soundLimit(), sounds);
        assertEquals(plan.budget().targetBurstLimit(), targetBursts);
        assertEquals(AbilityPresentationBudgetLedger.Grant.NONE,
                ledger.claim(AbilityPresentationCue.HEAL, 10, 1));
        assertFalse(ledger.claim(AbilityPresentationCue.MOBILITY, 10, 1).granted());
    }

    private static AbilityPresentationPlan plan(String formId, AbilitySlot slot) {
        ClassLineage lineage = BuiltInClassContent.catalog().lineageOf(formId);
        FixedAbilitySpec spec = BuiltInClassContent.abilityRegistry().require(ability(lineage, slot).id());
        return ClassAbilityPresentationPlanner.plan(lineage, spec);
    }

    private static AbilityDefinition ability(ClassLineage lineage, AbilitySlot slot) {
        return switch (slot) {
            case MOBILITY -> lineage.mobility();
            case SIGNATURE -> lineage.signature();
            case UTILITY -> lineage.utility();
        };
    }

}
