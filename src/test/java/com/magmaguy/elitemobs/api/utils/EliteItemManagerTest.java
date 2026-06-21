package com.magmaguy.elitemobs.api.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EliteItemManagerTest {

    private static final double EPSILON = 0.0001;

    @Test
    void damageEnchantmentsScaleMonotonicallyPastVanillaMax() {
        double sharpnessFour = EliteItemManager.calculateDamageEnchantmentBonus(4);
        double sharpnessFive = EliteItemManager.calculateDamageEnchantmentBonus(5);
        double sharpnessSix = EliteItemManager.calculateDamageEnchantmentBonus(6);

        assertEquals(0.10, sharpnessFour, EPSILON);
        assertEquals(0.125, sharpnessFive, EPSILON);
        assertEquals(0.15, sharpnessSix, EPSILON);
        assertTrue(sharpnessFour < sharpnessFive);
        assertTrue(sharpnessFive < sharpnessSix);
    }

    @Test
    void missingDamageEnchantmentsAddNoPercentBonus() {
        assertEquals(0, EliteItemManager.calculateDamageEnchantmentBonus(0), EPSILON);
    }

}
