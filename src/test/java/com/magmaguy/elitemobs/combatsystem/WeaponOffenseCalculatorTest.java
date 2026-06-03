package com.magmaguy.elitemobs.combatsystem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeaponOffenseCalculatorTest {

    @Test
    void slowMaceAttackSpeedProducesHigherPerHitDamage() {
        double swordDamage = DamageBreakdown.calculateExpectedDamage(25, 25, 25, 1.6);
        double maceDamage = DamageBreakdown.calculateExpectedDamage(25, 25, 25, 0.6);

        assertEquals(LevelScaling.REFERENCE_ATTACK_SPEED / 0.6, maceDamage / swordDamage, 0.0001);
    }

    @Test
    void axeAttackSpeedProducesExpectedPerHitDamage() {
        double swordDamage = DamageBreakdown.calculateExpectedDamage(25, 25, 25, 1.6);
        double axeDamage = DamageBreakdown.calculateExpectedDamage(25, 25, 25, 1.0);

        assertEquals(LevelScaling.REFERENCE_ATTACK_SPEED / 1.0, axeDamage / swordDamage, 0.0001);
    }

}
