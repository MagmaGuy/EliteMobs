package com.magmaguy.elitemobs.advancedcombat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancedCombatEnemyPolicyTest {

    @Test
    void onlyAuthorizedHostileElitesPassTheSharedTargetPolicy() {
        AdvancedCombatEnemyPolicy.TargetFacts valid =
                new AdvancedCombatEnemyPolicy.TargetFacts(
                        true, true, false, false, false, true, true);

        assertTrue(AdvancedCombatEnemyPolicy.permits(valid));
        assertFalse(AdvancedCombatEnemyPolicy.permits(withNpc(valid)));
        assertFalse(AdvancedCombatEnemyPolicy.permits(withFriendly(valid)));
        assertFalse(AdvancedCombatEnemyPolicy.permits(withClassMinion(valid)));
        assertFalse(AdvancedCombatEnemyPolicy.permits(withValidElite(valid, false)));
        assertFalse(AdvancedCombatEnemyPolicy.permits(withInstanceAuthorization(valid, false)));
        assertFalse(AdvancedCombatEnemyPolicy.permits(withCasterReady(valid, false)));
        assertFalse(AdvancedCombatEnemyPolicy.permits(withCandidateReady(valid, false)));
    }

    private static AdvancedCombatEnemyPolicy.TargetFacts withNpc(
            AdvancedCombatEnemyPolicy.TargetFacts state) {
        return new AdvancedCombatEnemyPolicy.TargetFacts(
                state.casterReady(), state.candidateReady(), true, state.friendly(),
                state.classMinion(), state.validElite(), state.instanceAuthorized());
    }

    private static AdvancedCombatEnemyPolicy.TargetFacts withFriendly(
            AdvancedCombatEnemyPolicy.TargetFacts state) {
        return new AdvancedCombatEnemyPolicy.TargetFacts(
                state.casterReady(), state.candidateReady(), state.npc(), true,
                state.classMinion(), state.validElite(), state.instanceAuthorized());
    }

    private static AdvancedCombatEnemyPolicy.TargetFacts withClassMinion(
            AdvancedCombatEnemyPolicy.TargetFacts state) {
        return new AdvancedCombatEnemyPolicy.TargetFacts(
                state.casterReady(), state.candidateReady(), state.npc(), state.friendly(),
                true, state.validElite(), state.instanceAuthorized());
    }

    private static AdvancedCombatEnemyPolicy.TargetFacts withValidElite(
            AdvancedCombatEnemyPolicy.TargetFacts state,
            boolean validElite) {
        return new AdvancedCombatEnemyPolicy.TargetFacts(
                state.casterReady(), state.candidateReady(), state.npc(), state.friendly(),
                state.classMinion(), validElite, state.instanceAuthorized());
    }

    private static AdvancedCombatEnemyPolicy.TargetFacts withInstanceAuthorization(
            AdvancedCombatEnemyPolicy.TargetFacts state,
            boolean instanceAuthorized) {
        return new AdvancedCombatEnemyPolicy.TargetFacts(
                state.casterReady(), state.candidateReady(), state.npc(), state.friendly(),
                state.classMinion(), state.validElite(), instanceAuthorized);
    }

    private static AdvancedCombatEnemyPolicy.TargetFacts withCasterReady(
            AdvancedCombatEnemyPolicy.TargetFacts state,
            boolean casterReady) {
        return new AdvancedCombatEnemyPolicy.TargetFacts(
                casterReady, state.candidateReady(), state.npc(), state.friendly(),
                state.classMinion(), state.validElite(), state.instanceAuthorized());
    }

    private static AdvancedCombatEnemyPolicy.TargetFacts withCandidateReady(
            AdvancedCombatEnemyPolicy.TargetFacts state,
            boolean candidateReady) {
        return new AdvancedCombatEnemyPolicy.TargetFacts(
                state.casterReady(), candidateReady, state.npc(), state.friendly(),
                state.classMinion(), state.validElite(), state.instanceAuthorized());
    }
}
