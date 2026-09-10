package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.advancedcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.advancedcombat.passives.PassiveAggregate;
import com.magmaguy.elitemobs.advancedcombat.passives.PassiveConditionContext;
import com.magmaguy.elitemobs.advancedcombat.passives.PassiveMechanics;
import com.magmaguy.elitemobs.advancedcombat.progression.ActiveLineageSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EliteControlEffectPlanTest {

    @Test
    void trapperControlPowerChangesTheSlowDeliveredAtTheBandBoundary() {
        EliteControlEffectPlan entry = trapperSlowAt(91);
        EliteControlEffectPlan cap = trapperSlowAt(100);

        assertEquals(.51166D, entry.movementSpeedMultiplier(), 1.0E-9D);
        assertEquals(.5068D, cap.movementSpeedMultiplier(), 1.0E-9D);
        assertEquals(-.0697090909090909D,
                entry.additionalMovementSpeedAdjustment(), 1.0E-9D);
        assertEquals(-.0785454545454545D,
                cap.additionalMovementSpeedAdjustment(), 1.0E-9D);
        assertTrue(cap.movementSpeedMultiplier() < entry.movementSpeedMultiplier(),
                "Trapper level 100 must slow an Elite more than Trapper level 91");
    }

    private static EliteControlEffectPlan trapperSlowAt(int effectiveLevel) {
        ClassCatalog catalog = BuiltInClassContent.catalog();
        ClassLineage lineage = catalog.lineageOf("trapper");
        ActiveLineageSnapshot snapshot = new ActiveLineageSnapshot(
                "trapper",
                lineage.activeForm().band().toLocalLevel(effectiveLevel),
                effectiveLevel,
                lineage.formIds(),
                lineage.passiveContributionLevels(effectiveLevel));
        PassiveMechanics mechanics = PassiveAggregate.resolve(
                        lineage, snapshot, BuiltInClassContent.passiveRegistry())
                .evaluate(PassiveConditionContext.playerOnly(1D, false, false, false))
                .mechanics();
        AbilityMechanicModifiers modifiers = new AbilityMechanicModifiers(
                mechanics.burstHealingMultiplier(),
                mechanics.periodicHealingMultiplier(),
                mechanics.groupedEnemyHealingMultiplier(),
                mechanics.periodicDurationMultiplier(),
                mechanics.shieldStrengthMultiplier(),
                mechanics.redirectedDamageMultiplier(),
                mechanics.controlDurationMultiplier(),
                mechanics.controlPotencyMultiplier());
        FixedAbilitySpec utility = BuiltInClassContent.abilityRegistry().require("trapper.utility");
        int levelDuration = ActiveAbilityLevelScaling.durationTicks(
                utility.tuning().durationTicks(), effectiveLevel);
        return EliteControlEffectPlan.slow(2, levelDuration, modifiers);
    }
}
