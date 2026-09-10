package com.magmaguy.elitemobs.advancedcombat.classes;

import com.magmaguy.elitemobs.skills.SkillType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuiltInClassCatalogTest {

    private final ClassCatalog catalog = BuiltInClassCatalog.catalog();

    @Test
    void descendantsResolveRootMechanicsAndTheirOwnActiveKit() {
        ClassLineage lineage = catalog.lineageOf("colossus");

        assertEquals(List.of("berserker", "juggernaut", "dreadnought", "colossus"), lineage.formIds());
        assertEquals(ClassResourceType.FURY, lineage.resourceType());
        assertEquals("berserker.mobility", lineage.mobility().id());
        assertEquals("colossus.signature", lineage.signature().id());
        assertEquals("colossus.utility", lineage.utility().id());
        assertEquals(4, lineage.passives().size());
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
