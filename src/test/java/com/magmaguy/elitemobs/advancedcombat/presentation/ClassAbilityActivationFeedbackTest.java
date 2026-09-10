package com.magmaguy.elitemobs.advancedcombat.presentation;

import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.advancedcombat.content.BuiltInClassContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassAbilityActivationFeedbackTest {
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
    }

}
