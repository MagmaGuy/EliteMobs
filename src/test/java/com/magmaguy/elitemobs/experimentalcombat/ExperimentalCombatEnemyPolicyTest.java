package com.magmaguy.elitemobs.experimentalcombat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExperimentalCombatEnemyPolicyTest {

    @Test
    void onlyAuthorizedHostileElitesPassTheSharedTargetPolicy() {
        ExperimentalCombatEnemyPolicy.TargetFacts valid =
                new ExperimentalCombatEnemyPolicy.TargetFacts(
                        true, true, false, false, false, true, true);

        assertTrue(ExperimentalCombatEnemyPolicy.permits(valid));
        assertFalse(ExperimentalCombatEnemyPolicy.permits(withNpc(valid)));
        assertFalse(ExperimentalCombatEnemyPolicy.permits(withFriendly(valid)));
        assertFalse(ExperimentalCombatEnemyPolicy.permits(withClassMinion(valid)));
        assertFalse(ExperimentalCombatEnemyPolicy.permits(withValidElite(valid, false)));
        assertFalse(ExperimentalCombatEnemyPolicy.permits(withInstanceAuthorization(valid, false)));
        assertFalse(ExperimentalCombatEnemyPolicy.permits(withCasterReady(valid, false)));
        assertFalse(ExperimentalCombatEnemyPolicy.permits(withCandidateReady(valid, false)));
    }

    private static ExperimentalCombatEnemyPolicy.TargetFacts withNpc(
            ExperimentalCombatEnemyPolicy.TargetFacts state) {
        return new ExperimentalCombatEnemyPolicy.TargetFacts(
                state.casterReady(), state.candidateReady(), true, state.friendly(),
                state.classMinion(), state.validElite(), state.instanceAuthorized());
    }

    private static ExperimentalCombatEnemyPolicy.TargetFacts withFriendly(
            ExperimentalCombatEnemyPolicy.TargetFacts state) {
        return new ExperimentalCombatEnemyPolicy.TargetFacts(
                state.casterReady(), state.candidateReady(), state.npc(), true,
                state.classMinion(), state.validElite(), state.instanceAuthorized());
    }

    private static ExperimentalCombatEnemyPolicy.TargetFacts withClassMinion(
            ExperimentalCombatEnemyPolicy.TargetFacts state) {
        return new ExperimentalCombatEnemyPolicy.TargetFacts(
                state.casterReady(), state.candidateReady(), state.npc(), state.friendly(),
                true, state.validElite(), state.instanceAuthorized());
    }

    private static ExperimentalCombatEnemyPolicy.TargetFacts withValidElite(
            ExperimentalCombatEnemyPolicy.TargetFacts state,
            boolean validElite) {
        return new ExperimentalCombatEnemyPolicy.TargetFacts(
                state.casterReady(), state.candidateReady(), state.npc(), state.friendly(),
                state.classMinion(), validElite, state.instanceAuthorized());
    }

    private static ExperimentalCombatEnemyPolicy.TargetFacts withInstanceAuthorization(
            ExperimentalCombatEnemyPolicy.TargetFacts state,
            boolean instanceAuthorized) {
        return new ExperimentalCombatEnemyPolicy.TargetFacts(
                state.casterReady(), state.candidateReady(), state.npc(), state.friendly(),
                state.classMinion(), state.validElite(), instanceAuthorized);
    }

    private static ExperimentalCombatEnemyPolicy.TargetFacts withCasterReady(
            ExperimentalCombatEnemyPolicy.TargetFacts state,
            boolean casterReady) {
        return new ExperimentalCombatEnemyPolicy.TargetFacts(
                casterReady, state.candidateReady(), state.npc(), state.friendly(),
                state.classMinion(), state.validElite(), state.instanceAuthorized());
    }

    private static ExperimentalCombatEnemyPolicy.TargetFacts withCandidateReady(
            ExperimentalCombatEnemyPolicy.TargetFacts state,
            boolean candidateReady) {
        return new ExperimentalCombatEnemyPolicy.TargetFacts(
                state.casterReady(), candidateReady, state.npc(), state.friendly(),
                state.classMinion(), state.validElite(), state.instanceAuthorized());
    }
}
