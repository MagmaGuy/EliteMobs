package com.magmaguy.elitemobs.config.customtreasurechests;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomTreasureChestConfigFieldsTest {

    @Test
    void missingLegacyRestockTimersDoNotRequestYamlMigration() {
        assertFalse(CustomTreasureChestConfigFields.hasLegacyRestockTimers(null));
        assertFalse(CustomTreasureChestConfigFields.hasLegacyRestockTimers(List.of()));
        assertTrue(CustomTreasureChestConfigFields.hasLegacyRestockTimers(List.of("player:200")));
    }
}
