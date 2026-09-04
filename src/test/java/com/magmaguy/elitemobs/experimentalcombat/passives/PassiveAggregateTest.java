package com.magmaguy.elitemobs.experimentalcombat.passives;

import com.magmaguy.elitemobs.experimentalcombat.classes.BuiltInClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.experimentalcombat.progression.ActiveLineageSnapshot;
import com.magmaguy.elitemobs.skills.SkillType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassiveAggregateTest {

    private final ClassCatalog catalog = BuiltInClassCatalog.catalog();
    private final FixedPassiveRegistry registry = BuiltInClassContent.passiveRegistry();

    @Test
    void registryMapsEveryShippedFormIncludingFormsWithoutConditionalTraits() {
        assertEquals(75, catalog.forms().size());
        assertEquals(catalog.forms().stream().map(form -> form.id()).collect(java.util.stream.Collectors.toSet()),
                registry.mappedFormIds());
        assertEquals(Set.of(), Set.copyOf(registry.traits("paladin")));
    }

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
    void rangerMovementFlipsAfterARecentHit() {
        PassiveAggregate ranger = aggregate("ranger", 30);
        PassiveConditionContext untouched = PassiveConditionContext.playerOnly(1D, true, false, false);
        PassiveConditionContext hit = PassiveConditionContext.playerOnly(1D, true, true, false);

        assertTrue(ranger.evaluate(untouched).movementSpeedAdjustment()
                > ranger.evaluate(hit).movementSpeedAdjustment());
        assertTrue(ranger.evaluate(hit).incomingDamageMultiplier()
                > ranger.evaluate(untouched).incomingDamageMultiplier());
    }

    @Test
    void specialistTargetTradeoffsUseTheAdvertisedTargetType() {
        PassiveAggregate dragonslayer = aggregate("dragonslayer", 100);
        PassiveConditionContext boss = context(1D, false, false, false, true, .8D,
                true, false, true, false, 24D, false, true, false);
        PassiveConditionContext ordinary = context(1D, false, false, false, true, .8D,
                false, false, true, false, 24D, false, true, false);

        assertTrue(dragonslayer.evaluate(boss).outgoingDamageMultiplier()
                > dragonslayer.evaluate(ordinary).outgoingDamageMultiplier());
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
        PassiveMechanics pathfinder = aggregate("pathfinder", 100)
                .evaluate(PassiveConditionContext.playerOnly(1D, true, false, true))
                .mechanics();
        PassiveMechanics trapper = aggregate("trapper", 100)
                .evaluate(PassiveConditionContext.playerOnly(1D, false, false, false))
                .mechanics();
        PassiveMechanics lifewarden = aggregate("lifewarden", 90)
                .evaluate(PassiveConditionContext.playerOnly(1D, false, false, true))
                .mechanics();
        PassiveMechanics strategistGrouped = aggregate("strategist", 100)
                .evaluate(PassiveConditionContext.playerOnly(1D, false, false, true))
                .mechanics();
        PassiveMechanics strategistSolo = aggregate("strategist", 100)
                .evaluate(PassiveConditionContext.playerOnly(1D, false, false, false))
                .mechanics();
        PassiveMechanics oracle = aggregate("oracle", 90)
                .evaluate(PassiveConditionContext.playerOnly(1D, false, false, true))
                .mechanics();

        assertTrue(guardian.redirectedDamageMultiplier() < 1D);
        assertTrue(aegis.redirectedDamageMultiplier() < guardian.redirectedDamageMultiplier());
        assertTrue(aegis.shieldStrengthMultiplier() > 1D);
        assertTrue(juggernaut.controlResistanceFraction() > 0D);
        assertTrue(dreadnought.controlResistanceFraction() > juggernaut.controlResistanceFraction());
        assertTrue(pathfinder.partyMovementSpeedAdjustment() > 0D);
        assertTrue(trapper.controlDurationMultiplier() > 1D);
        assertTrue(trapper.controlPotencyMultiplier() > 1D);
        assertTrue(lifewarden.periodicDurationMultiplier() > 1D);
        assertTrue(lifewarden.burstHealingMultiplier() < 1D);
        assertTrue(strategistGrouped.abilityCostMultiplier() < 1D);
        assertTrue(strategistSolo.abilityCostMultiplier() > 1D);
        assertTrue(oracle.shieldStrengthMultiplier() > 1D);
    }

    @Test
    void berserkerBranchesApplyTheirAdvertisedTargetAndCommitmentTradeoffs() {
        PassiveConditionContext woundedGroup = context(.8D, true, false, false, true, .3D,
                false, false, false, true, 4D, false, false, false);
        PassiveConditionContext woundedBoss = context(.8D, false, false, false, true, .3D,
                true, false, true, false, 4D, false, false, false);
        PassiveConditionContext healthyBoss = context(.8D, false, false, false, true, .9D,
                true, false, true, false, 4D, false, false, false);
        PassiveConditionContext chainedGroup = withState(woundedGroup, false, true, false);

        PassiveAggregate bloodstorm = aggregate("bloodstorm", 100);
        assertTrue(bloodstorm.evaluate(woundedGroup).mechanics().groupedEnemyHealingMultiplier() > 1D);
        assertTrue(bloodstorm.evaluate(woundedBoss).outgoingDamageMultiplier()
                < bloodstorm.evaluate(woundedGroup).outgoingDamageMultiplier());

        PassiveAggregate headsman = aggregate("headsman", 100);
        assertTrue(headsman.evaluate(woundedBoss).outgoingDamageMultiplier()
                > headsman.evaluate(woundedGroup).outgoingDamageMultiplier());

        PassiveAggregate harvester = aggregate("harvester", 100);
        assertTrue(harvester.evaluate(chainedGroup).outgoingDamageMultiplier()
                > harvester.evaluate(woundedGroup).outgoingDamageMultiplier());
        assertTrue(harvester.evaluate(chainedGroup).outgoingDamageMultiplier()
                > harvester.evaluate(withState(healthyBoss, false, true, false)).outgoingDamageMultiplier());
        assertEquals(harvester.evaluate(woundedBoss).outgoingDamageMultiplier(),
                harvester.evaluate(withState(woundedBoss, false, true, false)).outgoingDamageMultiplier(),
                1.0E-9D);

        PassiveAggregate crusher = aggregate("crusher", 90);
        PassiveConditionContext close = context(.8D, false, false, false, true, .8D,
                false, false, true, false, 4D, false, false, false);
        PassiveConditionContext far = context(.8D, false, false, false, true, .8D,
                false, false, true, false, 12D, false, false, false);
        assertTrue(crusher.evaluate(close).outgoingDamageMultiplier()
                > crusher.evaluate(far).outgoingDamageMultiplier());
        assertTrue(crusher.evaluate(close).incomingDamageMultiplier()
                < crusher.evaluate(far).incomingDamageMultiplier());

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
    void spellPassivesUseDirectClassWandAndStaffDomainsButExcludeSummons() {
        assertSpellDomainDelta("mage", 60, true);
        assertSpellDomainDelta("pyromancer", 100, true);
        assertSpellDomainDelta("summoner", 90, false);
        assertSpellDomainDelta("spiritbinder", 100, false);

        PassiveAggregate cryomancer = aggregate("cryomancer", 100);
        PassiveConditionContext physical = damageContext(false, false, false, false, false, null, false);
        PassiveConditionContext classSpell = damageContext(true, true, false, false, false, null, false);
        PassiveConditionContext wand = damageContext(false, false, false, false, false, SkillType.WANDS, false);
        PassiveConditionContext staff = damageContext(false, false, false, false, false, SkillType.STAVES, false);
        PassiveConditionContext summon = damageContext(true, false, false, false, false, null, false);
        assertTrue(cryomancer.conditionalTraits().stream()
                .filter(trait -> trait.conditions().equals(Set.of(PassiveCondition.SPELL_DAMAGE)))
                .anyMatch(trait -> trait.contribution().outgoingDamage() < 0D));
        assertEquals(cryomancer.evaluate(classSpell).outgoingDamageMultiplier(),
                cryomancer.evaluate(wand).outgoingDamageMultiplier(), 1.0E-9D);
        assertEquals(cryomancer.evaluate(classSpell).outgoingDamageMultiplier(),
                cryomancer.evaluate(staff).outgoingDamageMultiplier(), 1.0E-9D);
        assertEquals(cryomancer.evaluate(physical).outgoingDamageMultiplier(),
                cryomancer.evaluate(summon).outgoingDamageMultiplier(), 1.0E-9D);
    }

    @Test
    void classAbilityDomainsDriveAreaTrapAndBlastSpecialists() {
        PassiveConditionContext direct = damageContext(true, true, false, false, false, null, false);
        PassiveConditionContext area = damageContext(true, true, true, false, false, null, false);
        PassiveConditionContext trap = damageContext(true, true, false, true, false, null, false);
        PassiveConditionContext blast = damageContext(true, true, true, false, true, null, false);

        assertTrue(aggregate("elementalist", 90).evaluate(area).outgoingDamageMultiplier()
                > aggregate("elementalist", 90).evaluate(direct).outgoingDamageMultiplier());
        assertTrue(aggregate("saboteur", 90).evaluate(trap).outgoingDamageMultiplier()
                > aggregate("saboteur", 90).evaluate(direct).outgoingDamageMultiplier());
        assertTrue(aggregate("demolitionist", 100).evaluate(blast).outgoingDamageMultiplier()
                > aggregate("demolitionist", 100).evaluate(direct).outgoingDamageMultiplier());
    }

    @Test
    void positionalAndTriggeredRisksOnlyApplyWhenTheirFactsAreTrue() {
        PassiveConditionContext close = context(1D, false, false, false, true, 1D,
                false, false, true, false, 4D, false, false, false);
        PassiveConditionContext far = context(1D, false, false, false, true, 1D,
                false, false, true, false, 12D, false, false, false);
        assertTrue(aggregate("elementalist", 90).evaluate(close).incomingDamageMultiplier()
                > aggregate("elementalist", 90).evaluate(far).incomingDamageMultiplier());

        PassiveConditionContext intactWard = spellContext(true, false, false);
        PassiveConditionContext brokenWard = spellContext(true, false, true);
        assertTrue(aggregate("lich", 100).evaluate(brokenWard).incomingDamageMultiplier()
                > aggregate("lich", 100).evaluate(intactWard).incomingDamageMultiplier());
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

    private static PassiveConditionContext withState(
            PassiveConditionContext context,
            boolean magicWeaponDamage,
            boolean recentEliteKill,
            boolean wardBroken) {
        return new PassiveConditionContext(
                context.playerHealthFraction(), context.moving(), context.recentlyHit(), context.grouped(),
                context.targetPresent(), context.targetHealthFraction(), context.targetBoss(),
                context.targetControlled(), context.targetIsolated(), context.targetGrouped(),
                context.targetDistance(), context.criticalHit(), context.rangedAttack(),
                context.classAbilityDamage(), magicWeaponDamage, recentEliteKill, wardBroken);
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

    private void assertSpellDomainDelta(String formId, int effectiveLevel, boolean positive) {
        PassiveAggregate aggregate = aggregate(formId, effectiveLevel);
        PassiveConditionContext physical = damageContext(false, false, false, false, false, null, false);
        PassiveConditionContext classSpell = damageContext(true, true, false, false, false, null, false);
        PassiveConditionContext wand = damageContext(false, false, false, false, false, SkillType.WANDS, false);
        PassiveConditionContext staff = damageContext(false, false, false, false, false, SkillType.STAVES, false);
        PassiveConditionContext summon = damageContext(true, false, false, false, false, null, false);
        double baseline = aggregate.evaluate(physical).outgoingDamageMultiplier();

        for (PassiveConditionContext spell : java.util.List.of(classSpell, wand, staff)) {
            double actual = aggregate.evaluate(spell).outgoingDamageMultiplier();
            if (positive) assertTrue(actual > baseline, formId + " should boost direct spells");
            else assertTrue(actual < baseline, formId + " should weaken direct spells");
        }
        assertEquals(baseline, aggregate.evaluate(summon).outgoingDamageMultiplier(),
                1.0E-9D, formId + " should not alter servant damage");
    }

    private static PassiveConditionContext damageContext(
            boolean classAbilityDamage,
            boolean nonSummonClassAbilityDamage,
            boolean areaClassAbilityDamage,
            boolean trapClassAbilityDamage,
            boolean blastClassAbilityDamage,
            SkillType magicWeaponSkill,
            boolean wardBroken) {
        return new PassiveConditionContext(
                1D, false, false, false,
                true, 1D, false, false, true, false,
                8D, false, false, classAbilityDamage,
                nonSummonClassAbilityDamage, areaClassAbilityDamage,
                trapClassAbilityDamage, blastClassAbilityDamage,
                PassiveRuntimePolicy.isSpellDamage(false, magicWeaponSkill),
                false, wardBroken);
    }
}
