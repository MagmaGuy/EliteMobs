package com.magmaguy.elitemobs.mobspawning;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpawnRadiusDifficultyIncrementerTest {

    @Test
    void distanceBasedLevelUsesConfiguredBlockIncrements() {
        assertEquals(1, SpawnRadiusDifficultyIncrementer.calculateDistanceBasedLevel(0, 100));
        assertEquals(1, SpawnRadiusDifficultyIncrementer.calculateDistanceBasedLevel(100, 100));
        assertEquals(50, SpawnRadiusDifficultyIncrementer.calculateDistanceBasedLevel(5_000, 100));
        assertEquals(100, SpawnRadiusDifficultyIncrementer.calculateDistanceBasedLevel(10_000, 100));
    }

    @Test
    void invalidIncrementFallsBackToDefault() {
        assertEquals(100, SpawnRadiusDifficultyIncrementer.calculateDistanceBasedLevel(10_000, 0));
        assertEquals(100, SpawnRadiusDifficultyIncrementer.calculateDistanceBasedLevel(10_000, Double.NaN));
    }

}
