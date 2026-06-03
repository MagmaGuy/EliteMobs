package com.magmaguy.elitemobs.combatsystem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelScalingTest {

    @Test
    void mobHealthCalculationIsFiniteAtExtremeLevels() {
        assertTrue(Double.isFinite(LevelScaling.calculateMobHealth(Integer.MAX_VALUE, 20)));
        assertTrue(LevelScaling.calculateMobHealth(Integer.MAX_VALUE, 20) <= LevelScaling.getMinecraftMaxHealth());
    }

}
