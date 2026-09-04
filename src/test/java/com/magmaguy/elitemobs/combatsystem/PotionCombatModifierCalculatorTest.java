package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ResourceLock("MobCombatSettingsConfig.potionDamageMultipliers")
class PotionCombatModifierCalculatorTest {
    private double originalResistance;
    private double originalStrength;
    private double originalWeakness;

    @BeforeEach
    void setKnownMultipliers() throws ReflectiveOperationException {
        originalResistance = setMultiplier("resistanceDamageMultiplier", .10D);
        originalStrength = setMultiplier("strengthDamageMultiplier", .20D);
        originalWeakness = setMultiplier("weaknessDamageMultiplier", .20D);
    }

    @AfterEach
    void restoreMultipliers() throws ReflectiveOperationException {
        setMultiplier("resistanceDamageMultiplier", originalResistance);
        setMultiplier("strengthDamageMultiplier", originalStrength);
        setMultiplier("weaknessDamageMultiplier", originalWeakness);
    }

    @Test
    void sourceWeaknessAndTargetResistanceBothReduceDamage() {
        assertEquals(
                .72D,
                PotionCombatModifierCalculator.getCombinedDamageMultiplier(
                        0,
                        1,
                        1),
                1.0E-9D);
    }

    @Test
    void neutralTargetStillHonorsSourceWeakness() {
        assertEquals(
                .80D,
                PotionCombatModifierCalculator.getCombinedDamageMultiplier(
                        0,
                        1,
                        0),
                1.0E-9D);
    }

    @Test
    void weaknessReductionIsProportionalAcrossDamageAndAmplifierLevels() {
        double weaknessOne = PotionCombatModifierCalculator
                .getCombinedDamageMultiplier(
                        0,
                        1,
                        0);
        double weaknessTwo = PotionCombatModifierCalculator
                .getCombinedDamageMultiplier(
                        0,
                        2,
                        0);

        assertEquals(8D, 10D * weaknessOne, 1.0E-9D);
        assertEquals(80D, 100D * weaknessOne, 1.0E-9D);
        assertEquals(6D, 10D * weaknessTwo, 1.0E-9D);
        assertEquals(60D, 100D * weaknessTwo, 1.0E-9D);
    }

    private static double setMultiplier(String name, double value)
            throws ReflectiveOperationException {
        Field field = MobCombatSettingsConfig.class.getDeclaredField(name);
        field.setAccessible(true);
        double previous = field.getDouble(null);
        field.setDouble(null, value);
        return previous;
    }
}
