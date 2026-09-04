package com.magmaguy.elitemobs.experimentalcombat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExperimentalCombatSuggestionStateTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void dismissalPersistsInLocalYamlWithoutAPlayerDatabaseField() throws Exception {
        Path file = temporaryDirectory.resolve(".state/experimental-combat-suggestion.yml");
        UUID dismissed = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        ExperimentalCombatSuggestionState first = new ExperimentalCombatSuggestionState(file);
        assertFalse(first.isDismissed(dismissed));
        first.dismiss(dismissed);

        assertTrue(Files.isRegularFile(file));
        ExperimentalCombatSuggestionState reloaded = new ExperimentalCombatSuggestionState(file);
        assertTrue(reloaded.isDismissed(dismissed));
        assertFalse(reloaded.isDismissed(other));
    }
}
