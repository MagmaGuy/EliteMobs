package com.magmaguy.elitemobs.experimentalcombat.presentation;

import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassAbilityActivationFeedbackTest {
    @Test
    void mobilityOnlyShowsNameAndResourceSpend() {
        ClassLineage lineage = BuiltInClassContent.catalog().lineageOf("berserker");
        FixedAbilitySpec spec = BuiltInClassContent.abilityRegistry().require("berserker.mobility");

        String message = ClassAbilityActivationFeedback.message(lineage, AbilitySlot.MOBILITY, spec, 1);
        String plain = plain(message);

        assertTrue(plain.contains("Crater Leap!"));
        assertTrue(plain.contains("-60 Fury"));
        assertFalse(message.contains(lineage.mobility().description()));
        assertFalse(message.toLowerCase().contains("leap, then"));
    }

    @Test
    void signatureAndUtilityUseTerseNumericRuntimeSummaries() {
        ClassLineage lineage = BuiltInClassContent.catalog().lineageOf("berserker");
        FixedAbilitySpec signature = BuiltInClassContent.abilityRegistry().require("berserker.signature");

        String message = ClassAbilityActivationFeedback.message(
                lineage, AbilitySlot.SIGNATURE, signature, 1);
        String plain = plain(message);

        assertTrue(plain.contains("Rampage!"));
        assertTrue(plain.contains("+18% damage for 5s"));
        assertTrue(plain.contains("-35 Fury"));
        assertFalse(message.contains(lineage.signature().description()));
        assertEqualsOneLine(message);
    }

    @Test
    void timedStrengthSummaryUsesTheRuntimeLevelScaling() {
        FixedAbilitySpec spec = BuiltInClassContent.abilityRegistry().require("berserker.signature");

        String summary = ClassAbilityActivationFeedback.summary(spec, 100);

        assertTrue(summary.contains("+23% damage for 6.25s"));
        assertFalse(summary.contains("+18% damage for 5s"));
    }

    @Test
    void slowAndWeakenAreComposedWithoutDroppingEitherEffect() {
        FixedAbilitySpec spec = BuiltInClassContent.abilityRegistry().require("plaguebringer.utility");

        String summary = ClassAbilityActivationFeedback.summary(spec, 100);

        assertTrue(summary.contains("Slowness III + enemy damage -23% for 7.5s"));
        assertTrue(summary.split(", ").length <= 2);
        assertEqualsOneLine(summary);
    }

    private static void assertEqualsOneLine(String message) {
        assertTrue(!message.contains("\n") && !message.contains("\r"));
    }

    private static String plain(String message) {
        return message.replaceAll("<[^>]+>", "").replaceAll("&[0-9a-fk-or]", "");
    }
}
