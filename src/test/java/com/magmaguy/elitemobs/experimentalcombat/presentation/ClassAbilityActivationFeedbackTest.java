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
    void damageMarkSummaryUsesTheRuntimeLevelScaling() {
        FixedAbilitySpec spec = BuiltInClassContent.abilityRegistry().require("ranger.utility");

        String summary = ClassAbilityActivationFeedback.summary(spec, 100);

        assertTrue(summary.contains("+13% marked damage for 7.5s"));
        assertFalse(summary.contains("+10% marked damage for 6s"));
    }

    @Test
    void protectionSummaryUsesTheRuntimeLevelScaling() {
        FixedAbilitySpec spec = BuiltInClassContent.abilityRegistry().require("paladin.signature");

        String summary = ClassAbilityActivationFeedback.summary(spec, 100);

        assertTrue(summary.contains("25% damage reduction for 3.75s"));
        assertFalse(summary.contains("20% damage reduction for 3s"));
    }

    @Test
    void demonologistSummaryPrioritizesItsSpellPowerTradeoff() {
        FixedAbilitySpec spec = BuiltInClassContent.abilityRegistry().require("demonologist.utility");

        String summary = ClassAbilityActivationFeedback.summary(spec, 100);

        assertTrue(summary.contains("+28% spell damage for 5.65s"));
        assertTrue(summary.contains("+28% damage taken for 5.65s"));
        assertTrue(summary.split(", ").length <= 2);
        assertEqualsOneLine(summary);
    }

    @Test
    void rootSummaryNamesItsRuntimeDuration() {
        FixedAbilitySpec spec = BuiltInClassContent.abilityRegistry().require("titanbane.utility");

        String summary = ClassAbilityActivationFeedback.summary(spec, 100);

        assertTrue(summary.contains("root + disrupt + Slowness III for 5.65s"));
        assertTrue(summary.split(", ").length <= 2);
    }

    @Test
    void burnSummaryNamesItsRuntimeDuration() {
        FixedAbilitySpec spec = BuiltInClassContent.abilityRegistry().require("pyromancer.signature");

        String summary = ClassAbilityActivationFeedback.summary(spec, 100);

        assertTrue(summary.contains("burn for 6.25s"));
        assertTrue(summary.split(", ").length <= 2);
    }

    @Test
    void displacementSummariesNameTheirRuntimeStrength() {
        String knockback = ClassAbilityActivationFeedback.summary(
                BuiltInClassContent.abilityRegistry().require("paladin.utility"), 100);
        String pull = ClassAbilityActivationFeedback.summary(
                BuiltInClassContent.abilityRegistry().require("harvester.utility"), 100);
        String launch = ClassAbilityActivationFeedback.summary(
                BuiltInClassContent.abilityRegistry().require("crusher.utility"), 100);

        assertTrue(knockback.contains("knockback 0.88 blocks"));
        assertTrue(pull.contains("pull 0.88 blocks"));
        assertTrue(launch.contains("launch 0.88 blocks"));
        assertTrue(knockback.split(", ").length <= 2);
        assertTrue(pull.split(", ").length <= 2);
        assertTrue(launch.split(", ").length <= 2);
    }

    @Test
    void lifestealDurationIsOnlyShownForARealScaledWindow() {
        String window = ClassAbilityActivationFeedback.summary(
                BuiltInClassContent.abilityRegistry().require("reaver.signature"), 100);
        String immediate = ClassAbilityActivationFeedback.summary(
                BuiltInClassContent.abilityRegistry().require("bloodstorm.signature"), 100);

        assertTrue(window.contains("lifesteal for 3.75s"));
        assertTrue(immediate.contains("lifesteal"));
        assertFalse(immediate.contains("lifesteal for"));
    }

    @Test
    void timedEffectsScaleWhileScheduledUptimeStaysTruthfulToRuntime() {
        String shield = ClassAbilityActivationFeedback.summary(
                BuiltInClassContent.abilityRegistry().require("spellcaster.utility"), 100);
        String fear = ClassAbilityActivationFeedback.summary(
                BuiltInClassContent.abilityRegistry().require("tyrant.signature"), 100);
        String slow = ClassAbilityActivationFeedback.summary(
                BuiltInClassContent.abilityRegistry().require("trapper.utility"), 100);
        String summon = ClassAbilityActivationFeedback.summary(
                BuiltInClassContent.abilityRegistry().require("necromancer.signature"), 100);

        assertTrue(shield.contains("+15% shield for 12.5s"));
        assertTrue(shield.contains("25% damage reduction for 12.5s"));
        assertTrue(fear.contains("fear + taunt + enemy damage -19% for 5.65s"));
        assertTrue(slow.contains("Slowness III + reveal for 6.9s"));
        assertTrue(summon.contains("summon for 40s"));
        assertFalse(summon.contains("summon for 50s"));
    }

    @Test
    void interruptSummaryNamesItsScaledSuppressionWindow() {
        FixedAbilitySpec spec = BuiltInClassContent.abilityRegistry().require("hierophant.utility");

        String summary = ClassAbilityActivationFeedback.summary(spec, 100);

        assertTrue(summary.contains("disrupt for 2.2s"));
    }

    @Test
    void slowAndWeakenAreComposedWithoutDroppingEitherEffect() {
        FixedAbilitySpec spec = BuiltInClassContent.abilityRegistry().require("plaguebringer.utility");

        String summary = ClassAbilityActivationFeedback.summary(spec, 100);

        assertTrue(summary.contains("Slowness III + enemy damage -23% for 7.5s"));
        assertTrue(summary.split(", ").length <= 2);
        assertEqualsOneLine(summary);
    }

    @Test
    void fearTauntAndWeakenShareOneTerseControlOutcome() {
        FixedAbilitySpec spec = BuiltInClassContent.abilityRegistry().require("tyrant.signature");

        String summary = ClassAbilityActivationFeedback.summary(spec, 100);

        assertTrue(summary.contains("fear + taunt + enemy damage -19% for 5.65s"));
        assertTrue(summary.split(", ").length <= 2);
        assertEqualsOneLine(summary);
    }

    @Test
    void correctedSemanticsAreNamedWithoutClaimingFlatOrRepeatedAreaDamage() {
        ClassLineage bloodrager = BuiltInClassContent.catalog().lineageOf("bloodrager");
        String frenzy = plain(ClassAbilityActivationFeedback.message(
                bloodrager,
                AbilitySlot.SIGNATURE,
                BuiltInClassContent.abilityRegistry().require("bloodrager.signature"),
                31));
        assertTrue(frenzy.contains("up to +24% damage"));
        assertTrue(frenzy.contains("+43% speed as HP falls for 5.4s"));

        ClassLineage dreadnought = BuiltInClassContent.catalog().lineageOf("dreadnought");
        String seismic = plain(ClassAbilityActivationFeedback.message(
                dreadnought,
                AbilitySlot.SIGNATURE,
                BuiltInClassContent.abilityRegistry().require("dreadnought.signature"),
                61));
        assertTrue(seismic.contains("expanding hit"));
        assertFalse(seismic.contains("3 hits"));

        ClassLineage raincaller = BuiltInClassContent.catalog().lineageOf("raincaller");
        String storm = plain(ClassAbilityActivationFeedback.message(
                raincaller,
                AbilitySlot.SIGNATURE,
                BuiltInClassContent.abilityRegistry().require("raincaller.signature"),
                91));
        assertTrue(storm.contains("24 arrows"));
        assertTrue(storm.contains("over 4s"));
    }

    @Test
    void weakeningSummaryShowsTheLevelScaledPercentageInsteadOfVanillaWeakness() {
        ClassLineage lineage = BuiltInClassContent.catalog().lineageOf("justicar");
        FixedAbilitySpec spec = BuiltInClassContent.abilityRegistry().require("justicar.utility");

        String summary = ClassAbilityActivationFeedback.summary(spec, 60);

        assertTrue(summary.contains("-23% enemy damage for 4.6s"));
        assertFalse(summary.contains("Weakness"));
    }

    @Test
    void everyActivationStaysNamedThemedAndOnOneLine() {
        for (var form : BuiltInClassContent.catalog().forms()) {
            ClassLineage lineage = BuiltInClassContent.catalog().lineageOf(form.id());
            for (AbilitySlot slot : AbilitySlot.values()) {
                String abilityId = switch (slot) {
                    case MOBILITY -> lineage.mobility().id();
                    case SIGNATURE -> lineage.signature().id();
                    case UTILITY -> lineage.utility().id();
                };
                FixedAbilitySpec spec = BuiltInClassContent.abilityRegistry().require(abilityId);
                String message = ClassAbilityActivationFeedback.message(lineage, slot, spec, 100);

                assertTrue(message.contains(abilityName(lineage, slot)), abilityId);
                assertTrue(message.startsWith("<g:"
                        + ClassPresentationTheme.colors(lineage.resourceType()) + ">"), abilityId);
                assertEqualsOneLine(message);
                assertFalse(message.toLowerCase().contains("cooldown"), abilityId);
                if (slot != AbilitySlot.MOBILITY) {
                    for (int level : new int[]{1, 30, 60, 90, 100}) {
                        String summary = ClassAbilityActivationFeedback.summary(spec, level);
                        assertTrue(summary.split(", ").length <= 2, abilityId + " at " + level);
                        assertFalse(summary.equals("activate"), abilityId + " at " + level);
                        assertEqualsOneLine(summary);
                    }
                }
            }
        }
    }

    private static String abilityName(ClassLineage lineage, AbilitySlot slot) {
        return switch (slot) {
            case MOBILITY -> lineage.mobility().displayName();
            case SIGNATURE -> lineage.signature().displayName();
            case UTILITY -> lineage.utility().displayName();
        };
    }

    private static void assertEqualsOneLine(String message) {
        assertTrue(!message.contains("\n") && !message.contains("\r"));
    }

    private static String plain(String message) {
        return message.replaceAll("<[^>]+>", "").replaceAll("&[0-9a-fk-or]", "");
    }
}
