package com.magmaguy.elitemobs.combatsystem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeaponOffenseCalculatorTest {

    private static final int MATCHED_LEVEL = 25;
    private static final double EPSILON = 0.0001;

    @Test
    void overperformingWeaponFamiliesUseDampedSpeedFactor() {
        assertMatchedDamageRatio("DIAMOND_AXE", 1.0, dampedFactor(1.0));
        assertMatchedDamageRatio("MACE", 0.6, dampedFactor(0.6));
        assertMatchedDamageRatio("TRIDENT", 1.1, dampedFactor(1.1));
    }

    @Test
    void wellPacedWeaponFamiliesKeepLegacySpeedFactor() {
        assertMatchedDamageRatio("DIAMOND_SWORD", 1.6, legacyFactor(1.6));
        assertMatchedDamageRatio("DIAMOND_HOE", 4.0, legacyFactor(4.0));
        assertMatchedDamageRatio("DIAMOND_SPEAR", 1.0 / 1.05, legacyFactor(1.0 / 1.05));
    }

    @Test
    void dampedWeaponFamiliesPreserveMaterialSpeedDifferences() {
        double diamondAxe = WeaponOffenseCalculator.getAttackSpeedFactor("DIAMOND_AXE", 1.0);
        double woodenAxe = WeaponOffenseCalculator.getAttackSpeedFactor("WOODEN_AXE", 0.8);

        assertTrue(woodenAxe > diamondAxe);
    }

    @Test
    void unknownMeleeItemsUseLegacyAttackSpeedFactor() {
        assertEquals(0.4, WeaponOffenseCalculator.getAttackSpeedFactor("BOW", 4.0), EPSILON);
    }

    private static double legacyFactor(double attackSpeed) {
        return WeaponOffenseCalculator.getLegacyAttackSpeedFactor(attackSpeed);
    }

    private static double dampedFactor(double attackSpeed) {
        return Math.pow(legacyFactor(attackSpeed), WeaponOffenseCalculator.BURST_DAMPING_EXPONENT);
    }

    private static void assertMatchedDamageRatio(String weaponType, double attackSpeed, double expectedFactor) {
        double swordDamage = DamageBreakdown.calculateExpectedDamage(
                MATCHED_LEVEL,
                MATCHED_LEVEL,
                MATCHED_LEVEL,
                "DIAMOND_SWORD",
                1.6);
        double weaponDamage = DamageBreakdown.calculateExpectedDamage(
                MATCHED_LEVEL,
                MATCHED_LEVEL,
                MATCHED_LEVEL,
                weaponType,
                attackSpeed);

        assertEquals(expectedFactor, weaponDamage / swordDamage, EPSILON);
    }

}
