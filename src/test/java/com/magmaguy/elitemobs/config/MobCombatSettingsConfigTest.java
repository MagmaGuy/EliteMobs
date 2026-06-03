package com.magmaguy.elitemobs.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobCombatSettingsConfigTest {

    @Test
    void emptyDistanceBasedWorldListAllowsAllWorlds() {
        assertTrue(MobCombatSettingsConfig.isWorldAllowedForDistanceBasedNaturalEliteLevels("world", List.of()));
    }

    @Test
    void distanceBasedWorldListMatchesConfiguredWorldOnly() {
        assertTrue(MobCombatSettingsConfig.isWorldAllowedForDistanceBasedNaturalEliteLevels("world_nether", List.of("world", "WORLD_NETHER")));
        assertFalse(MobCombatSettingsConfig.isWorldAllowedForDistanceBasedNaturalEliteLevels("world_the_end", List.of("world", "world_nether")));
    }

    @Test
    void configuredWorldNamesAreTrimmedAndNullSafe() {
        assertTrue(MobCombatSettingsConfig.isWorldAllowedForDistanceBasedNaturalEliteLevels("world", List.of("  world  ")));
        assertFalse(MobCombatSettingsConfig.isWorldAllowedForDistanceBasedNaturalEliteLevels(null, List.of("world")));
    }

}
