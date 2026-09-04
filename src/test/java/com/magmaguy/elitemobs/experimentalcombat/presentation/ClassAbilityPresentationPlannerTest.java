package com.magmaguy.elitemobs.experimentalcombat.presentation;

import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityEffect;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityFamily;
import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilityDefinition;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassAbilityPresentationPlannerTest {

    @Test
    void everyBuiltInAbilityHasTheCuesItsMechanicsNeed() {
        var catalog = BuiltInClassContent.catalog();
        var registry = BuiltInClassContent.abilityRegistry();

        for (var form : catalog.forms()) {
            ClassLineage lineage = catalog.lineageOf(form.id());
            for (AbilitySlot slot : AbilitySlot.values()) {
                AbilityDefinition ability = ability(lineage, slot);
                FixedAbilitySpec spec = registry.require(ability.id());
                AbilityPresentationPlan plan = ClassAbilityPresentationPlanner.plan(lineage, spec);

                assertNotNull(plan.theme(), ability.id());
                assertTrue(plan.cues().contains(AbilityPresentationCue.CAST), ability.id());
                if (isMobility(spec.family()))
                    assertTrue(plan.cues().contains(AbilityPresentationCue.MOBILITY), ability.id());
                if (spec.effects().contains(AbilityEffect.DAMAGE))
                    assertTrue(plan.cues().contains(AbilityPresentationCue.IMPACT), ability.id());
                if (spec.effects().contains(AbilityEffect.HEAL)
                        || spec.effects().contains(AbilityEffect.LIFESTEAL))
                    assertTrue(plan.cues().contains(AbilityPresentationCue.HEAL), ability.id());
                if (spec.effects().stream().anyMatch(ClassAbilityPresentationPlannerTest::isBuff))
                    assertTrue(plan.cues().contains(AbilityPresentationCue.BUFF), ability.id());
                if (spec.effects().stream().anyMatch(ClassAbilityPresentationPlannerTest::isControl))
                    assertTrue(plan.cues().contains(AbilityPresentationCue.CONTROL), ability.id());

                assertTrue(plan.budget().particleLimit() <= 180, ability.id());
                assertTrue(plan.budget().soundLimit() <= 8, ability.id());
                assertTrue(plan.budget().targetBurstLimit() <= 8, ability.id());
                assertTrue(plan.budget().traceSampleLimit() <= 18, ability.id());
            }
        }
    }

    @Test
    void distinctiveBranchesSelectReusableFantasyThemes() {
        assertTheme("paladin", AbilityPresentationTheme.VALOR);
        assertTheme("berserker", AbilityPresentationTheme.FURY);
        assertTheme("demolitionist", AbilityPresentationTheme.ENGINEERING);
        assertTheme("tempest_archer", AbilityPresentationTheme.STORM);
        assertTheme("grovekeeper", AbilityPresentationTheme.NATURE);
        assertTheme("spiritcaller", AbilityPresentationTheme.SPIRIT);
        assertTheme("pyromancer", AbilityPresentationTheme.FLAME);
        assertTheme("cryomancer", AbilityPresentationTheme.FROST);
        assertTheme("necromancer", AbilityPresentationTheme.SHADOW);
    }

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

    private static void assertTheme(String formId, AbilityPresentationTheme expected) {
        assertEquals(expected, plan(formId, AbilitySlot.SIGNATURE).theme(), formId);
    }

    private static AbilityDefinition ability(ClassLineage lineage, AbilitySlot slot) {
        return switch (slot) {
            case MOBILITY -> lineage.mobility();
            case SIGNATURE -> lineage.signature();
            case UTILITY -> lineage.utility();
        };
    }

    private static boolean isMobility(AbilityFamily family) {
        return switch (family) {
            case MOUNTED_CHARGE, BALLISTIC_LEAP, SAFE_DASH, SAFE_BLINK, ALLY_FLIGHT -> true;
            default -> false;
        };
    }

    private static boolean isBuff(AbilityEffect effect) {
        return switch (effect) {
            case SHIELD, CLEANSE, SELF_PROTECT, ALLY_PROTECT, SPEED, STRENGTH -> true;
            default -> false;
        };
    }

    private static boolean isControl(AbilityEffect effect) {
        return switch (effect) {
            case KNOCKBACK, PULL, LAUNCH, SLOW, WEAKEN, GLOW, PARTY_DAMAGE_MARK,
                    TAUNT, INTERRUPT, FEAR -> true;
            default -> false;
        };
    }
}
