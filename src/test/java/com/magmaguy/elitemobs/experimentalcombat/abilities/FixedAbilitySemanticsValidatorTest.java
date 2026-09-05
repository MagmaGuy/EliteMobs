package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FixedAbilitySemanticsValidatorTest {
    @Test
    void rejectsEveryNonzeroTuningFieldWithoutAnExecutableConsumer() {
        FixedAbilitySpec invalid = new FixedAbilitySpec(
                "invalid.utility", AbilitySlot.UTILITY, AbilityFamily.INSTANT,
                AbilityTarget.SELF, Set.of(AbilityEffect.CLEANSE),
                new AbilityTuning(1D, .1D, .1D, .5D, 1.2D,
                        0D, 0D, 40, 1, 1),
                AbilityExecutionTraits.STANDARD, 20D);

        assertEquals(5, FixedAbilitySemanticsValidator.problems(invalid).size());
    }

    @Test
    void weakeningPotencyMustHaveExactlyOneExecutableConsumer() {
        FixedAbilitySpec missingPotency = new FixedAbilitySpec(
                "missing.utility", AbilitySlot.UTILITY, AbilityFamily.INSTANT,
                AbilityTarget.AIMED_ENEMY, Set.of(AbilityEffect.WEAKEN),
                AbilityTuning.combat(0D, 12D, 0D, 80),
                AbilityExecutionTraits.STANDARD, 20D);
        FixedAbilitySpec deadPotency = new FixedAbilitySpec(
                "dead.utility", AbilitySlot.UTILITY, AbilityFamily.INSTANT,
                AbilityTarget.SELF, Set.of(AbilityEffect.CLEANSE),
                AbilityTuning.combat(0D, 0D, 0D, 80).withWeakenMultiplier(.8D),
                AbilityExecutionTraits.STANDARD, 20D);

        assertEquals(List.of("WEAKEN must author a weakenMultiplier below 1"),
                FixedAbilitySemanticsValidator.problems(missingPotency));
        assertEquals(List.of("weakenMultiplier has no WEAKEN effect"),
                FixedAbilitySemanticsValidator.problems(deadPotency));
    }

    @Test
    void healEchoRequiresAReachableSameFormHeal() {
        FixedAbilitySpec echo = ability(
                "echo.signature",
                0D,
                Set.of(AbilityEffect.HEAL),
                AbilityExecutionTraits.mechanics(AbilityMechanic.HEAL_ECHO));
        FixedAbilitySpec dryUtility = ability(
                "echo.utility",
                0D,
                Set.of(AbilityEffect.SHIELD),
                AbilityExecutionTraits.STANDARD);
        FixedAbilitySpec healingUtility = ability(
                "echo.utility",
                .04D,
                Set.of(AbilityEffect.HEAL),
                AbilityExecutionTraits.STANDARD);

        assertThrows(IllegalStateException.class, () ->
                FixedAbilitySemanticsValidator.validate(List.of(echo, dryUtility)));
        assertDoesNotThrow(() ->
                FixedAbilitySemanticsValidator.validate(List.of(echo, healingUtility)));
    }

    private static FixedAbilitySpec ability(
            String id,
            double healingFraction,
            Set<AbilityEffect> effects,
            AbilityExecutionTraits traits) {
        return new FixedAbilitySpec(
                id,
                id.endsWith(".signature") ? AbilitySlot.SIGNATURE : AbilitySlot.UTILITY,
                AbilityFamily.INSTANT,
                AbilityTarget.SELF,
                effects,
                AbilityTuning.support(healingFraction, 0D, 0D, 0D, 40),
                traits,
                20D);
    }
}
