package com.magmaguy.elitemobs.experimentalcombat.analysis;

import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityLevelScaling;
import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilityRegistry;
import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActiveAbilityBalanceAnalyzerTest {

    private final ClassCatalog catalog = BuiltInClassContent.catalog();
    private final FixedAbilityRegistry registry = BuiltInClassContent.abilityRegistry();

    @Test
    void everyActiveAbilityIsIncludedWithLevelAndResourceMetrics() {
        ActiveAbilityBalanceReport report = ActiveAbilityBalanceAnalyzer.analyze(catalog, registry);

        assertEquals(registry.registeredIds().size(), report.rows().size());
        assertTrue(report.rows().stream().allMatch(row -> row.evaluationLevel() >= 1));
        assertTrue(report.rows().stream().allMatch(row -> row.normalizedEffect() >= row.baseEffect()));
        assertTrue(report.rows().stream().allMatch(row -> row.resourceCost() > 0D));
        assertTrue(report.rows().stream().allMatch(row -> row.resourceEfficiency() >= 0D));
        assertEquals(AbilityLevelScaling.multiplier(100), 1.25D);
    }

    @Test
    void mobilityUsesResourceAsItsOnlyRepeatGate() {
        List<FixedAbilitySpec> mobility = registry.registeredIds().stream()
                .map(registry::require)
                .filter(spec -> spec.slot() == AbilitySlot.MOBILITY)
                .toList();

        assertEquals(catalog.roots().size(), mobility.size());
        assertEquals(50D, mobility.stream().mapToDouble(FixedAbilitySpec::resourceCost).average().orElseThrow());
        assertTrue(mobility.stream().allMatch(spec -> spec.resourceCost() >= 40D && spec.resourceCost() <= 60D));
    }

    @Test
    void everySlotUsesSignificanceBasedCostBands() {
        for (AbilitySlot slot : AbilitySlot.values()) {
            List<Double> costs = registry.registeredIds().stream()
                    .map(registry::require)
                    .filter(spec -> spec.slot() == slot)
                    .map(FixedAbilitySpec::resourceCost)
                    .distinct()
                    .toList();
            assertTrue(costs.size() >= 3, slot + " should not use one flat resource price");
        }
    }

    @Test
    void thresholdFlagsBothSidesOfThePeerMedian() {
        assertEquals(ActiveAbilityBalanceReport.Outlier.LOW,
                ActiveAbilityBalanceAnalyzer.detectOutlier(6.6D, 10D));
        assertEquals(ActiveAbilityBalanceReport.Outlier.NONE,
                ActiveAbilityBalanceAnalyzer.detectOutlier(10D, 10D));
        assertEquals(ActiveAbilityBalanceReport.Outlier.HIGH,
                ActiveAbilityBalanceAnalyzer.detectOutlier(15.1D, 10D));
    }

    @Test
    void combatReportExportsGraphAndMachineReadableRows() {
        ActiveAbilityBalanceReport report = ActiveAbilityBalanceAnalyzer.analyze(catalog, registry);

        assertFalse(report.graphLines().isEmpty());
        assertEquals(registry.registeredIds().size() + 1L, report.csv().lines().count());
        assertTrue(report.csv().contains("normalized_effect,efficiency,outlier"));
    }

    @Test
    void projectileBombardmentHasAFixedDeliveryBudgetAcrossTargetDensities() {
        ActiveAbilityBalanceReport report = ActiveAbilityBalanceAnalyzer.analyze(catalog, registry);
        ActiveAbilityBalanceReport.Row raincaller = report.rows().stream()
                .filter(row -> row.abilityId().equals("raincaller.signature"))
                .findFirst()
                .orElseThrow();

        assertEquals(List.of(1, 3, 8), raincaller.scenarios().stream()
                .map(ActiveAbilityBalanceReport.TargetScenario::targetCount)
                .toList());

        // 0.55 damage x 6 pulses x 4 arrows x the level-100 1.25 multiplier.
        assertEquals(16.5D, raincaller.scenario(ActiveAbilityBalanceReport.TargetDensity.DUEL)
                .castEffect(), 1.0E-9D);
        assertEquals(16.5D, raincaller.scenario(ActiveAbilityBalanceReport.TargetDensity.GROUP)
                .castEffect(), 1.0E-9D);
        assertEquals(16.5D, raincaller.scenario(ActiveAbilityBalanceReport.TargetDensity.PACK)
                .castEffect(), 1.0E-9D);
    }

    @Test
    void abilitiesExposeTheirActualBandStartAndEndScaling() {
        ActiveAbilityBalanceReport report = ActiveAbilityBalanceAnalyzer.analyze(catalog, registry);
        ActiveAbilityBalanceReport.Row bloodrager = report.rows().stream()
                .filter(row -> row.abilityId().equals("bloodrager.signature"))
                .findFirst()
                .orElseThrow();
        ActiveAbilityBalanceReport.Row raincaller = report.rows().stream()
                .filter(row -> row.abilityId().equals("raincaller.signature"))
                .findFirst()
                .orElseThrow();

        assertEquals(31, bloodrager.startLevel());
        assertEquals(60, bloodrager.evaluationLevel());
        assertTrue(bloodrager.normalizedEffect() > bloodrager.startEffect());
        assertEquals(91, raincaller.startLevel());
        assertEquals(100, raincaller.evaluationLevel());
        assertTrue(raincaller.normalizedEffect() > raincaller.startEffect());
    }

    @Test
    void everySignatureAndUtilityModelsItsRuntimeLevelProgression() {
        ActiveAbilityBalanceReport report = ActiveAbilityBalanceAnalyzer.analyze(catalog, registry);

        List<ActiveAbilityBalanceReport.Row> flat = report.rows().stream()
                .filter(row -> row.slot() != AbilitySlot.MOBILITY)
                .filter(row -> row.evaluationLevel() > row.startLevel())
                .filter(row -> row.normalizedEffect() <= row.startEffect())
                .toList();

        assertEquals(List.of(), flat,
                () -> "The balance graph flattened runtime-scaled abilities: "
                        + flat.stream().map(ActiveAbilityBalanceReport.Row::abilityId).toList());
    }

    @Test
    void resourceOpportunityModelsTheOneMinuteFullBarBaseline() {
        ActiveAbilityBalanceReport report = ActiveAbilityBalanceAnalyzer.analyze(catalog, registry);
        ActiveAbilityBalanceReport.Row blink = report.rows().stream()
                .filter(row -> row.abilityId().equals("spellcaster.mobility"))
                .findFirst()
                .orElseThrow();

        assertEquals(30D, blink.cadence().resourceRefillSeconds(), 1.0E-9D);
        assertEquals(2D, blink.cadence().sustainableCastsPerMinute(), 1.0E-9D);
        assertEquals(blink.scenario(ActiveAbilityBalanceReport.TargetDensity.DUEL).castEffect() * 2D,
                blink.scenario(ActiveAbilityBalanceReport.TargetDensity.DUEL)
                        .sustainableEffectPerMinute(), 1.0E-9D);
    }

    @Test
    void summonRowsExposePhysicalUptimeCadenceHealthAndCap() {
        ActiveAbilityBalanceReport report = ActiveAbilityBalanceAnalyzer.analyze(catalog, registry);
        ActiveAbilityBalanceReport.Row necromancer = report.rows().stream()
                .filter(row -> row.abilityId().equals("necromancer.signature"))
                .findFirst()
                .orElseThrow();
        ActiveAbilityBalanceReport.SummonMetrics summon = necromancer.summon().orElseThrow();

        assertEquals(1, summon.summonCount());
        assertEquals(3, summon.ownerCap());
        assertEquals(800, summon.uptimeTicks());
        assertEquals(10, summon.attacksPerMinion());
        assertEquals(80, summon.attackPeriodTicks());
        assertTrue(summon.maxHealth() > 0D);
        assertTrue(summon.scaledDamagePerHit() > 0D);
        assertTrue(necromancer.normalizedEffect()
                        >= summon.summonCount()
                        * summon.attacksPerMinion()
                        * summon.scaledDamagePerHit(),
                "A summon must be scored by its projected lifetime impact, not one hit");
    }

    @Test
    void graphAndCsvExposeDensityCadenceAndSummonDiagnostics() {
        ActiveAbilityBalanceReport report = ActiveAbilityBalanceAnalyzer.analyze(catalog, registry);
        String graph = String.join("\n", report.graphLines());
        String csv = report.csv();

        assertTrue(graph.contains("D/G/P"));
        assertTrue(graph.contains("casts/min"));
        assertTrue(graph.contains("summon"));
        assertTrue(csv.contains("start_level,end_level"));
        assertTrue(csv.contains("duel_effect,group_effect,pack_effect"));
        assertTrue(csv.contains("casts_per_minute,summon_uptime_ticks"));
        assertTrue(csv.contains("duel_outlier,group_outlier,pack_outlier"));
    }

    @Test
    void semanticMechanicsContributeTheirMeasuredPayloadInsteadOfFreeLabels() {
        ActiveAbilityBalanceReport report = ActiveAbilityBalanceAnalyzer.analyze(catalog, registry);
        ActiveAbilityBalanceReport.Row deadeye = row(report, "deadeye.signature");
        ActiveAbilityBalanceReport.Row trapper = row(report, "trapper.signature");
        ActiveAbilityBalanceReport.Row demolitionist = row(report, "demolitionist.signature");

        // The guaranteed critical is an authored 50% damage payload and scales with rank.
        double deadeyeDamageOnly = 3.4D * AbilityLevelScaling.multiplier(90);
        assertTrue(deadeye.normalizedEffect() > deadeyeDamageOnly);
        // Root and slow are visible control value, not zero-cost prose.
        assertTrue(trapper.normalizedEffect()
                > .35D * 5D * AbilityLevelScaling.multiplier(90));
        // All three physical cluster impacts contribute to the blast score.
        assertTrue(demolitionist.normalizedEffect()
                > 1.15D * AbilityLevelScaling.multiplier(90));
    }

    @Test
    void weakeningScoreUsesTheAuthoredPercentageInsteadOfAFixedLabelWeight() {
        ActiveAbilityBalanceReport.Row justicar = row(
                ActiveAbilityBalanceAnalyzer.analyze(catalog, registry),
                "justicar.utility");

        double expected = (1D - .80D) * 3D * Math.sqrt(80D / 100D) - .15D;

        assertEquals(expected, justicar.baseEffect(), 1.0E-9D);
    }

    @Test
    void resourceBurstAndPiercingChangeCadenceAndTargetDensity() {
        ActiveAbilityBalanceReport report = ActiveAbilityBalanceAnalyzer.analyze(catalog, registry);
        ActiveAbilityBalanceReport.Row windrunner = row(report, "windrunner.utility");
        ActiveAbilityBalanceReport.Row arcaneVolley = row(report, "mage.signature");

        assertEquals(0D, windrunner.cadence().resourceRefillSeconds(), 1.0E-9D);
        // A resource-positive cast is bounded only by the one-cast-per-tick input ceiling now.
        assertEquals(1_200D, windrunner.cadence().sustainableCastsPerMinute(), 1.0E-9D);
        assertEquals(1, arcaneVolley.scenario(ActiveAbilityBalanceReport.TargetDensity.DUEL)
                .affectedTargets());
        assertEquals(8, arcaneVolley.scenario(ActiveAbilityBalanceReport.TargetDensity.PACK)
                .affectedTargets());
    }

    @Test
    void rowOutlierDoesNotDropOppositeDuelAndPackSignals() {
        ActiveAbilityBalanceReport report = ActiveAbilityBalanceAnalyzer.analyze(catalog, registry);
        ActiveAbilityBalanceReport.Row warlord = row(report, "warlord.signature");

        assertEquals(ActiveAbilityBalanceReport.Outlier.LOW,
                warlord.scenario(ActiveAbilityBalanceReport.TargetDensity.DUEL).outlier());
        assertEquals(ActiveAbilityBalanceReport.Outlier.NONE,
                warlord.scenario(ActiveAbilityBalanceReport.TargetDensity.GROUP).outlier());
        assertEquals(ActiveAbilityBalanceReport.Outlier.HIGH,
                warlord.scenario(ActiveAbilityBalanceReport.TargetDensity.PACK).outlier());
        assertEquals("MIXED", warlord.outlier().name());
        assertTrue(report.outliers().contains(warlord));
    }

    private static ActiveAbilityBalanceReport.Row row(
            ActiveAbilityBalanceReport report,
            String abilityId) {
        return report.rows().stream()
                .filter(row -> row.abilityId().equals(abilityId))
                .findFirst()
                .orElseThrow();
    }
}
