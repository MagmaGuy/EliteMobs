package com.magmaguy.elitemobs.experimentalcombat.classes;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuiltInClassCatalogTest {

    private final ClassCatalog catalog = BuiltInClassCatalog.catalog();

    @Test
    void shippedManifestContainsTheFiveApprovedRootClasses() {
        assertEquals(Set.of("paladin", "berserker", "ranger", "cleric", "spellcaster"),
                catalog.roots().stream().map(ClassFormDefinition::id).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void everyRegisteredTreeIsACompleteBinaryProgression() {
        for (ClassFormDefinition form : catalog.forms()) {
            assertEquals(form.band().isTerminal() ? 0 : 2, catalog.childrenOf(form.id()).size());
            assertEquals(2, form.foundationSkills().asList().size());
        }
    }

    @Test
    void descendantsResolveRootMechanicsAndTheirOwnActiveKit() {
        ClassLineage lineage = catalog.lineageOf("colossus");

        assertEquals(List.of("berserker", "juggernaut", "dreadnought", "colossus"), lineage.formIds());
        assertEquals(ClassResourceType.FURY, lineage.resourceType());
        assertEquals("Crater Leap", lineage.mobility().displayName());
        assertEquals("Worldbreaker", lineage.signature().displayName());
        assertEquals("Immovable", lineage.utility().displayName());
        assertEquals(4, lineage.passives().size());
    }

    @Test
    void paladinMobilityCopyDescribesTheMountedCharge() {
        AbilityDefinition steed = catalog.lineageOf("paladin").mobility();

        assertEquals("Divine Steed", steed.displayName());
        assertEquals("Ride an armored steed and taunt foes struck.", steed.description());
    }

    @Test
    void spellcasterTreeInheritsBlinkAndManaAndUsesBothMagicSkills() {
        ClassLineage lineage = catalog.lineageOf("spiritbinder");

        assertEquals(List.of("spellcaster", "occultist", "summoner", "spiritbinder"), lineage.formIds());
        assertEquals(ClassResourceType.MANA, lineage.resourceType());
        assertEquals("Blink", lineage.mobility().displayName());
        assertEquals("Guardian Eidolon", lineage.signature().displayName());
        assertEquals("Shared Essence", lineage.utility().displayName());
        assertEquals(4, lineage.passives().size());

        for (ClassFormDefinition form : catalog.forms()) {
            if (!catalog.rootOf(form.id()).id().equals("spellcaster")) continue;
            assertEquals(List.of(SkillType.STAVES, SkillType.WANDS), form.foundationSkills().asList());
        }
    }

    @Test
    void everyResourceHasCodeOwnedRulesIncludingMana() {
        assertEquals(EnumSet.allOf(ClassResourceType.class), BuiltInClassContent.resourceDefinitions().keySet());
        var mana = BuiltInClassContent.resourceDefinitions().get(ClassResourceType.MANA);
        assertEquals(100D, mana.initialAmount());
        assertEquals(100D / 60D, mana.inCombatTickDelta());
        assertEquals(100D / 60D, mana.outOfCombatTickDelta());
    }

    @Test
    void allPlayerFacingClassRowsStaySnappyAndSelfContained() {
        List<String> descriptions = new ArrayList<>();
        List<String> renderedRows = new ArrayList<>();
        for (ClassFormDefinition form : catalog.forms()) {
            if (form.rootKit() != null) {
                AbilityDefinition mobility = form.rootKit().mobility();
                descriptions.add(mobility.description());
                renderedRows.add(abilityRow("Mobility", mobility));
            }
            descriptions.add(form.signature().description());
            descriptions.add(form.utility().description());
            descriptions.add(form.passive().description());
            renderedRows.add(abilityRow("Signature", form.signature()));
            renderedRows.add(abilityRow("Utility", form.utility()));
            renderedRows.add("Bonus " + form.displayName() + " · " + form.passive().description());
        }
        for (ClassResourceType resource : ClassResourceType.values()) {
            descriptions.add(resource.description());
            renderedRows.add(resource.displayName() + " · " + resource.description());
        }

        for (String renderedRow : renderedRows) {
            assertTrue(renderedRow.length() <= 68,
                    () -> "Rendered detail row exceeds the one-line copy budget: " + renderedRow);
        }

        for (String description : descriptions) {
            assertTrue(description.endsWith("."),
                    () -> "Description must be one sentence: " + description);
            assertEquals(1, description.chars().filter(character -> character == '.').count(),
                    () -> "Description must be one sentence: " + description);
            assertTrue(description.indexOf('\n') < 0 && description.indexOf('\r') < 0,
                    () -> "Description must stay on one source line: " + description);
            assertTrue(description.indexOf('—') < 0 && description.indexOf('–') < 0,
                    () -> "Description contains a dash separator: " + description);

            String lower = description.toLowerCase(Locale.ROOT);
            for (String externalReference : List.of(
                    "diablo", "overwatch", "world of warcraft", "dark souls", "d&d")) {
                assertTrue(!lower.contains(externalReference),
                        () -> "Description references another game: " + description);
            }
        }
    }

    private static String abilityRow(String label, AbilityDefinition ability) {
        return label + " " + ability.displayName() + " · " + ability.description();
    }

    @Test
    void inheritedPassivesContinueScalingFromTheirOwnBandOrigins() {
        ClassLineage lineage = catalog.lineageOf("colossus");

        assertEquals(Map.of(
                        "berserker", 91,
                        "juggernaut", 61,
                        "dreadnought", 31,
                        "colossus", 1),
                lineage.passiveContributionLevels(91));
        assertEquals(Map.of(
                        "berserker", 100,
                        "juggernaut", 70,
                        "dreadnought", 40,
                        "colossus", 10),
                lineage.passiveContributionLevels(100));
    }

    @Test
    void localLevelsMapToTheSingleEffectiveClassLevel() {
        assertEquals(1, ClassBand.ROOT.toEffectiveLevel(1));
        assertEquals(31, ClassBand.LEVEL_31.toEffectiveLevel(1));
        assertEquals(61, ClassBand.LEVEL_61.toEffectiveLevel(1));
        assertEquals(91, ClassBand.LEVEL_91.toEffectiveLevel(1));
        assertEquals(11, ClassBand.LEVEL_91.toLocalLevel(101));
        assertTrue(ClassBand.LEVEL_91.containsEffectiveLevel(101));
    }

    @Test
    void aFormsOwnPairDeterminesItsEffectiveSkillCap() {
        ClassFormDefinition sniper = catalog.require("sniper");

        assertEquals(31, sniper.requiredFoundationSkillLevel());
        assertEquals(List.of(SkillType.BOWS, SkillType.CROSSBOWS), sniper.foundationSkills().asList());
        assertEquals(37, sniper.effectiveSkillCap(skill -> switch (skill) {
            case BOWS -> 42;
            case CROSSBOWS -> 37;
            default -> 0;
        }));
        assertEquals(7, sniper.localProgressionCap(skill -> switch (skill) {
            case BOWS -> 42;
            case CROSSBOWS -> 37;
            default -> 0;
        }));
        assertEquals(60, sniper.effectiveProgressionCap(skill -> 100));
        assertEquals(30, sniper.localProgressionCap(skill -> 100));
        assertTrue(sniper.meetsFoundationRequirement(skill -> 31));
        assertEquals(List.of(SkillType.CROSSBOWS), sniper.foundationSkills().limitingSkills(skill -> switch (skill) {
            case BOWS -> 42;
            case CROSSBOWS -> 37;
            default -> 0;
        }));
    }
}
