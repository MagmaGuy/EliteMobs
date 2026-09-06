package com.magmaguy.elitemobs.experimentalcombat.passives;

import com.magmaguy.elitemobs.experimentalcombat.classes.BuiltInClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.experimentalcombat.progression.ActiveLineageSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PassiveAggregateTest {

    private final ClassCatalog catalog = BuiltInClassCatalog.catalog();
    private final FixedPassiveRegistry registry = BuiltInClassContent.passiveRegistry();

    @Test
    void bloodragerRiskScalesWithMissingHealthAndInheritedContributionLevel() {
        PassiveConditionContext healthy = context(.9D, false, false, false, false, 1D,
                false, false, false, false, 4D, false, false, false);
        PassiveConditionContext critical = context(.2D, false, false, false, false, 1D,
                false, false, false, false, 4D, false, false, false);

        PassiveAggregate level31 = aggregate("bloodrager", 31);
        PassiveAggregate level60 = aggregate("bloodrager", 60);

        assertTrue(level31.evaluate(critical).outgoingDamageMultiplier()
                > level31.evaluate(healthy).outgoingDamageMultiplier());
        assertTrue(level31.evaluate(critical).incomingDamageMultiplier()
                > level31.evaluate(healthy).incomingDamageMultiplier());
        assertTrue(level60.evaluate(critical).outgoingDamageMultiplier()
                > level31.evaluate(critical).outgoingDamageMultiplier());
    }

    @Test
    void reaverReducesHealingReceivedRatherThanHealingDone() {
        PassiveAggregate reaver = aggregate("reaver", 90);
        PassiveAggregate.Evaluation evaluation = reaver.evaluate(
                PassiveConditionContext.playerOnly(.4D, false, false, false));

        assertTrue(evaluation.healingReceivedMultiplier() < 1D);
        assertTrue(evaluation.healingDoneMultiplier() > 1D);
    }

    @Test
    void typedMechanicsScaleAndInheritAcrossTheirClassLineages() {
        PassiveMechanics guardian = aggregate("guardian", 60)
                .evaluate(PassiveConditionContext.playerOnly(1D, false, false, true))
                .mechanics();
        PassiveMechanics aegis = aggregate("aegis", 90)
                .evaluate(PassiveConditionContext.playerOnly(1D, false, false, true))
                .mechanics();
        PassiveMechanics juggernaut = aggregate("juggernaut", 60)
                .evaluate(PassiveConditionContext.playerOnly(1D, false, false, false))
                .mechanics();
        PassiveMechanics dreadnought = aggregate("dreadnought", 90)
                .evaluate(PassiveConditionContext.playerOnly(1D, false, false, false))
                .mechanics();
        PassiveMechanics lifewarden = aggregate("lifewarden", 90)
                .evaluate(PassiveConditionContext.playerOnly(1D, false, false, true))
                .mechanics();
        PassiveMechanics oracle = aggregate("oracle", 90)
                .evaluate(PassiveConditionContext.playerOnly(1D, false, false, true))
                .mechanics();

        assertTrue(guardian.redirectedDamageMultiplier() < 1D);
        assertTrue(aegis.redirectedDamageMultiplier() < guardian.redirectedDamageMultiplier());
        assertTrue(aegis.shieldStrengthMultiplier() > 1D);
        assertTrue(juggernaut.controlResistanceFraction() > 0D);
        assertTrue(dreadnought.controlResistanceFraction() > juggernaut.controlResistanceFraction());
        assertTrue(lifewarden.periodicDurationMultiplier() > 1D);
        assertTrue(lifewarden.burstHealingMultiplier() < 1D);
        assertTrue(oracle.shieldStrengthMultiplier() > 1D);
    }

    @Test
    void berserkerBranchesApplyTheirAdvertisedTargetAndCommitmentTradeoffs() {
        PassiveConditionContext woundedGroup = context(.8D, true, false, false, true, .3D,
                false, false, false, true, 4D, false, false, false);
        PassiveConditionContext woundedBoss = context(.8D, false, false, false, true, .3D,
                true, false, true, false, 4D, false, false, false);

        PassiveAggregate bloodstorm = aggregate("bloodstorm", 100);
        assertTrue(bloodstorm.evaluate(woundedGroup).mechanics().groupedEnemyHealingMultiplier() > 1D);
        assertTrue(bloodstorm.evaluate(woundedBoss).outgoingDamageMultiplier()
                < bloodstorm.evaluate(woundedGroup).outgoingDamageMultiplier());

        PassiveAggregate headsman = aggregate("headsman", 100);
        assertTrue(headsman.evaluate(woundedBoss).outgoingDamageMultiplier()
                > headsman.evaluate(woundedGroup).outgoingDamageMultiplier());

        assertTrue(registry.require("siegebreaker").atContributionLevel(100).outgoingDamage() < 0D);
        PassiveAggregate titanbane = aggregate("titanbane", 100);
        PassiveConditionContext groupedBoss = context(.8D, false, false, false, true, .8D,
                true, false, false, true, 4D, false, false, false);
        assertTrue(titanbane.evaluate(woundedBoss).outgoingDamageMultiplier()
                > titanbane.evaluate(groupedBoss).outgoingDamageMultiplier());
    }

    @Test
    void groupHealersAndLifewardenUseHealingMechanicsInsteadOfDefenseSubstitutes() {
        PassiveConditionContext grouped = PassiveConditionContext.playerOnly(1D, false, false, true);
        PassiveConditionContext solo = PassiveConditionContext.playerOnly(1D, false, false, false);

        assertTrue(aggregate("hierophant", 90).evaluate(grouped).healingDoneMultiplier()
                > aggregate("hierophant", 90).evaluate(solo).healingDoneMultiplier());
        assertTrue(aggregate("shepherd", 100).evaluate(grouped).healingDoneMultiplier()
                > aggregate("shepherd", 100).evaluate(solo).healingDoneMultiplier());

        PassiveConditionContext standing = PassiveConditionContext.playerOnly(1D, false, false, false);
        PassiveConditionContext moving = PassiveConditionContext.playerOnly(1D, true, false, false);
        assertTrue(aggregate("grovekeeper", 100).evaluate(standing).healingDoneMultiplier()
                > aggregate("grovekeeper", 100).evaluate(moving).healingDoneMultiplier());
    }

    @Test
    void terminalPassiveBenefitsContinuePastTheXpSoftCap() {
        PassiveConditionContext spell = spellContext(true, false, false);
        assertTrue(aggregate("pyromancer", 101).evaluate(spell).outgoingDamageMultiplier()
                > aggregate("pyromancer", 100).evaluate(spell).outgoingDamageMultiplier());
        assertTrue(registry.require("dragonslayer").atContributionLevel(101).outgoingDamage()
                > registry.require("dragonslayer").atContributionLevel(100).outgoingDamage());
        assertTrue(aggregate("colossus", 101).evaluate(spell).mechanics().controlResistanceFraction()
                > aggregate("colossus", 100).evaluate(spell).mechanics().controlResistanceFraction());
    }

    private PassiveAggregate aggregate(String formId, int effectiveLevel) {
        ClassLineage lineage = catalog.lineageOf(formId);
        ActiveLineageSnapshot snapshot = new ActiveLineageSnapshot(
                formId,
                lineage.activeForm().band().toLocalLevel(effectiveLevel),
                effectiveLevel,
                lineage.formIds(),
                lineage.passiveContributionLevels(effectiveLevel));
        return PassiveAggregate.resolve(lineage, snapshot, registry);
    }

    private static PassiveConditionContext context(
            double playerHealth,
            boolean moving,
            boolean recentlyHit,
            boolean grouped,
            boolean targetPresent,
            double targetHealth,
            boolean targetBoss,
            boolean targetControlled,
            boolean targetIsolated,
            boolean targetGrouped,
            double targetDistance,
            boolean critical,
            boolean ranged,
            boolean classDamage) {
        return new PassiveConditionContext(
                playerHealth, moving, recentlyHit, grouped,
                targetPresent, targetHealth, targetBoss, targetControlled,
                targetIsolated, targetGrouped, targetDistance, critical, ranged, classDamage);
    }

    private static PassiveConditionContext spellContext(
            boolean classAbilityDamage,
            boolean magicWeaponDamage,
            boolean wardBroken) {
        return new PassiveConditionContext(
                1D, false, false, false,
                true, 1D, false, false, true, false,
                8D, false, false, classAbilityDamage,
                magicWeaponDamage, false, wardBroken);
    }

}
