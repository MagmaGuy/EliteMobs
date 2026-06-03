package com.magmaguy.elitemobs.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillsConfigTest {

    @Test
    void emptySkillWorldExclusionListAllowsAllWorlds() {
        assertFalse(SkillsConfig.worldIsExcludedFromSkills("world", List.of()));
    }

    @Test
    void skillWorldExclusionMatchesConfiguredWorldOnly() {
        assertTrue(SkillsConfig.worldIsExcludedFromSkills("event_world", List.of("world", "EVENT_WORLD")));
        assertFalse(SkillsConfig.worldIsExcludedFromSkills("survival", List.of("event_world")));
    }

    @Test
    void configuredWorldNamesAreTrimmedAndNullSafe() {
        assertTrue(SkillsConfig.worldIsExcludedFromSkills("event_world", List.of("  event_world  ")));
        assertFalse(SkillsConfig.worldIsExcludedFromSkills(null, List.of("event_world")));
    }

}
