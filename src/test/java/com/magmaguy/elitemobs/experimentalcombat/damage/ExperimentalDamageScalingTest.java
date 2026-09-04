package com.magmaguy.elitemobs.experimentalcombat.damage;

import com.magmaguy.elitemobs.combatsystem.LevelScaling;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExperimentalDamageScalingTest {
    private static final double ABILITY_MULTIPLIER = 1.8D;

    @ParameterizedTest
    @ValueSource(ints = {1, 30, 60, 90})
    void matchedLevelAbilityMaintainsTheSameEliteHealthRatio(int level) {
        double eliteHealth = LevelScaling.calculateMobHealth(level, 0D);
        double rankedMultiplier = ABILITY_MULTIPLIER * ExperimentalDamageScaling.classRankScale(level);
        double damage = ExperimentalDamageScaling.combine(
                LevelScaling.calculateBaseDamageToElite(level),
                LevelScaling.calculateOffensiveSkillAdjustment(level, level),
                1D,
                rankedMultiplier,
                1D,
                1D,
                1D);

        assertEquals(rankedMultiplier / LevelScaling.TARGET_HITS_TO_KILL_MOB,
                damage / eliteHealth, 1.0E-9D);
    }

    @ParameterizedTest
    @ValueSource(ints = {30, 60, 90})
    void tenLevelDeficitUsesTheCanonicalOffensivePenalty(int targetLevel) {
        int classLevel = Math.max(1, targetLevel - 10);
        double expectedAdjustment = LevelScaling.calculateOffensiveSkillAdjustment(classLevel, targetLevel);
        double damage = ExperimentalDamageScaling.combine(
                LevelScaling.calculateBaseDamageToElite(targetLevel),
                expectedAdjustment,
                1D,
                ABILITY_MULTIPLIER,
                1D,
                1D,
                1D);

        double matchedDamage = ExperimentalDamageScaling.combine(
                LevelScaling.calculateBaseDamageToElite(targetLevel),
                1D,
                1D,
                ABILITY_MULTIPLIER,
                1D,
                1D,
                1D);
        assertEquals(expectedAdjustment, damage / matchedDamage, 1.0E-9D);
    }

    @org.junit.jupiter.api.Test
    void zeroProtectionOrCombatMultiplierMeansZeroDamage() {
        assertEquals(0D, ExperimentalDamageScaling.combine(10D, 1D, 1D, 1D, 1D, 0D, 1D));
        assertEquals(0D, ExperimentalDamageScaling.combine(10D, 1D, 1D, 1D, 1D, 1D, 0D));
    }

    @org.junit.jupiter.api.Test
    void rankedAbilityIsAuthoredMultiplierTimesRankScaleAgainstTheNeutralBaseline() {
        double baseline = 12.5D;
        int level = 61;

        assertEquals(
                baseline * ABILITY_MULTIPLIER * (1D + .0025D * level),
                ExperimentalDamageScaling.scaleClassAbilityBaseline(
                        baseline, level, ABILITY_MULTIPLIER),
                1.0E-9D);
    }
}
