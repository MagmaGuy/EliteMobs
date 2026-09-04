package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityEnemyPolicyTest {

    @Test
    void woundedTargetPromiseRejectsTargetsAtOrAboveHalfHealth() {
        Set<AbilityMechanic> mechanic = Set.of(AbilityMechanic.WOUNDED_TARGETS_ONLY);

        assertTrue(AbilityEnemyPolicy.permits(mechanic,
                new AbilityEnemyPolicy.EnemyFacts(.49D, false, false)));
        assertFalse(AbilityEnemyPolicy.permits(mechanic,
                new AbilityEnemyPolicy.EnemyFacts(.50D, true, true)));
    }

    @Test
    void largeOrBossPromiseAcceptsEitherFactButNotAnOrdinaryElite() {
        Set<AbilityMechanic> mechanic = Set.of(AbilityMechanic.LARGE_OR_BOSS_ONLY);

        assertTrue(AbilityEnemyPolicy.permits(mechanic,
                new AbilityEnemyPolicy.EnemyFacts(1D, true, false)));
        assertTrue(AbilityEnemyPolicy.permits(mechanic,
                new AbilityEnemyPolicy.EnemyFacts(1D, false, true)));
        assertFalse(AbilityEnemyPolicy.permits(mechanic,
                new AbilityEnemyPolicy.EnemyFacts(1D, false, false)));
    }

    @Test
    void bossOnlyPromiseDoesNotTreatSizeAsBossStatus() {
        Set<AbilityMechanic> mechanic = Set.of(AbilityMechanic.BOSS_ONLY);

        assertTrue(AbilityEnemyPolicy.permits(mechanic,
                new AbilityEnemyPolicy.EnemyFacts(1D, true, false)));
        assertFalse(AbilityEnemyPolicy.permits(mechanic,
                new AbilityEnemyPolicy.EnemyFacts(1D, false, true)));
    }

    @Test
    void strictTargetPromisesFailClosedInsteadOfFallingBackToNearbyEnemies() {
        assertTrue(AbilityEnemyPolicy.requiresQualifiedSelection(
                Set.of(AbilityMechanic.RECENT_ATTACKERS)));
        assertTrue(AbilityEnemyPolicy.requiresQualifiedSelection(
                Set.of(AbilityMechanic.TAUNTED_TARGETS_ONLY)));
        assertFalse(AbilityEnemyPolicy.requiresQualifiedSelection(Set.of()));
    }
}
