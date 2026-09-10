package com.magmaguy.elitemobs.advancedcombat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancedCombatSuggestionStateTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void dismissalPersistsInLocalYamlWithoutAPlayerDatabaseField() throws Exception {
        Path file = temporaryDirectory.resolve(".state/advanced-combat-suggestion.yml");
        UUID dismissed = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        AdvancedCombatSuggestionState first = new AdvancedCombatSuggestionState(file);
        assertFalse(first.isDismissed(dismissed));
        first.dismiss(dismissed);

        assertTrue(Files.isRegularFile(file));
        AdvancedCombatSuggestionState reloaded = new AdvancedCombatSuggestionState(file);
        assertTrue(reloaded.isDismissed(dismissed));
        assertFalse(reloaded.isDismissed(other));
    }
}
