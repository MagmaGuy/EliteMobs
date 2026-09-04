package com.magmaguy.elitemobs.experimentalcombat.analysis;

import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassBand;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassResourceType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Immutable, renderer-neutral active-ability balance report. */
public record ActiveAbilityBalanceReport(List<Row> rows) {
    public ActiveAbilityBalanceReport {
        rows = List.copyOf(Objects.requireNonNull(rows, "rows"));
    }

    public List<Row> outliers() {
        return rows.stream().filter(row -> row.outlier() != Outlier.NONE).toList();
    }

    /** Plain-text graphs suitable for the existing combat-test log. */
    public List<String> graphLines() {
        List<String> lines = new ArrayList<>();
        lines.add("ACTIVE ABILITY BALANCE GRAPH");
        lines.add("Score is a normalized matched-level effect budget. D/G/P means 1, 3 and 8 targets.");
        for (AbilitySlot slot : AbilitySlot.values()) {
            List<Row> slotRows = rows.stream()
                    .filter(row -> row.slot() == slot)
                    .sorted(Comparator.comparing((Row row) -> row.band().depth())
                            .thenComparing(Row::formId))
                    .toList();
            if (slotRows.isEmpty()) continue;
            lines.add("");
            lines.add(slot.name());
            double maximumEfficiency = slotRows.stream().mapToDouble(Row::resourceEfficiency).max().orElse(1D);
            for (Row row : slotRows) {
                int barLength = maximumEfficiency <= 0D
                        ? 0
                        : (int) Math.round(row.resourceEfficiency() / maximumEfficiency * 24D);
                String bar = "#".repeat(Math.max(0, barLength));
                TargetScenario duel = row.scenario(TargetDensity.DUEL);
                TargetScenario group = row.scenario(TargetDensity.GROUP);
                TargetScenario pack = row.scenario(TargetDensity.PACK);
                lines.add(String.format(Locale.ROOT,
                        "  %-27s Lv%3d-%3d %3.0f %s %-24s %5.2f->%5.2f effect | "
                                + "%4.2f casts/min | D/G/P %5.1f/%5.1f/%5.1f per min [%s/%s/%s] | %s",
                        row.formId() + "." + row.slot().name().toLowerCase(Locale.ROOT),
                        row.startLevel(), row.evaluationLevel(), row.resourceCost(), marker(row.outlier()), bar,
                        row.startEffect(), row.normalizedEffect(), row.cadence().sustainableCastsPerMinute(),
                        duel.sustainableEffectPerMinute(), group.sustainableEffectPerMinute(),
                        pack.sustainableEffectPerMinute(), marker(duel.outlier()), marker(group.outlier()),
                        marker(pack.outlier()), row.outlier()));
                row.summon().ifPresent(summon -> lines.add(String.format(Locale.ROOT,
                        "    summon %dx, cap %d, %.1fs uptime, %.1fs attacks, %.1f HP, %.2fx hit",
                        summon.summonCount(), summon.ownerCap(), summon.uptimeTicks() / 20D,
                        summon.attackPeriodTicks() / 20D, summon.maxHealth(), summon.scaledDamagePerHit())));
            }
        }
        return List.copyOf(lines);
    }

    public String csv() {
        StringBuilder output = new StringBuilder(
                "root_class,form_id,ability_id,slot,band,start_level,end_level,resource,cost,"
                        + "base_effect,start_effect,normalized_effect,efficiency,outlier,"
                        + "resource_refill_seconds,casts_per_minute,summon_uptime_ticks,"
                        + "summon_count,summon_cap,summon_health,summon_attack_period_ticks,"
                        + "duel_effect,group_effect,pack_effect,duel_per_minute,group_per_minute,pack_per_minute,"
                        + "duel_outlier,group_outlier,pack_outlier\n");
        for (Row row : rows) {
            SummonMetrics summon = row.summon().orElse(null);
            output.append(row.rootClassId()).append(',')
                    .append(row.formId()).append(',')
                    .append(row.abilityId()).append(',')
                    .append(row.slot()).append(',')
                    .append(row.band()).append(',')
                    .append(row.startLevel()).append(',')
                    .append(row.evaluationLevel()).append(',')
                    .append(row.resourceType()).append(',')
                    .append(format(row.resourceCost())).append(',')
                    .append(format(row.baseEffect())).append(',')
                    .append(format(row.startEffect())).append(',')
                    .append(format(row.normalizedEffect())).append(',')
                    .append(format(row.resourceEfficiency())).append(',')
                    .append(row.outlier()).append(',')
                    .append(format(row.cadence().resourceRefillSeconds())).append(',')
                    .append(format(row.cadence().sustainableCastsPerMinute())).append(',')
                    .append(summon == null ? "" : summon.uptimeTicks()).append(',')
                    .append(summon == null ? "" : summon.summonCount()).append(',')
                    .append(summon == null ? "" : summon.ownerCap()).append(',')
                    .append(summon == null ? "" : format(summon.maxHealth())).append(',')
                    .append(summon == null ? "" : summon.attackPeriodTicks()).append(',')
                    .append(format(row.scenario(TargetDensity.DUEL).castEffect())).append(',')
                    .append(format(row.scenario(TargetDensity.GROUP).castEffect())).append(',')
                    .append(format(row.scenario(TargetDensity.PACK).castEffect())).append(',')
                    .append(format(row.scenario(TargetDensity.DUEL).sustainableEffectPerMinute())).append(',')
                    .append(format(row.scenario(TargetDensity.GROUP).sustainableEffectPerMinute())).append(',')
                    .append(format(row.scenario(TargetDensity.PACK).sustainableEffectPerMinute())).append(',')
                    .append(row.scenario(TargetDensity.DUEL).outlier()).append(',')
                    .append(row.scenario(TargetDensity.GROUP).outlier()).append(',')
                    .append(row.scenario(TargetDensity.PACK).outlier()).append('\n');
        }
        return output.toString();
    }

    private static String marker(Outlier outlier) {
        return switch (outlier) {
            case HIGH -> "+";
            case LOW -> "-";
            case MIXED -> "±";
            case NONE -> "=";
        };
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.4f", value);
    }

    public enum Outlier {
        LOW,
        NONE,
        HIGH,
        MIXED
    }

    public enum TargetDensity {
        DUEL(1),
        GROUP(3),
        PACK(8);

        private final int targetCount;

        TargetDensity(int targetCount) {
            this.targetCount = targetCount;
        }

        public int targetCount() {
            return targetCount;
        }
    }

    public record TargetScenario(
            TargetDensity density,
            int targetCount,
            int affectedTargets,
            double castEffect,
            double resourceEfficiency,
            double sustainableEffectPerMinute,
            Outlier outlier) {
        public TargetScenario {
            Objects.requireNonNull(density, "density");
            Objects.requireNonNull(outlier, "outlier");
            if (targetCount < 1 || affectedTargets < 1 || affectedTargets > targetCount)
                throw new IllegalArgumentException("Target scenario counts are invalid");
            if (!Double.isFinite(castEffect) || castEffect < 0D
                    || !Double.isFinite(resourceEfficiency) || resourceEfficiency < 0D
                    || !Double.isFinite(sustainableEffectPerMinute) || sustainableEffectPerMinute < 0D)
                throw new IllegalArgumentException("Target scenario scores must be finite and non-negative");
        }
    }

    public record Cadence(
            double resourceRefillSeconds,
            double sustainableCastsPerMinute) {
        public Cadence {
            if (!Double.isFinite(resourceRefillSeconds) || resourceRefillSeconds < 0D
                    || !Double.isFinite(sustainableCastsPerMinute) || sustainableCastsPerMinute <= 0D)
                throw new IllegalArgumentException("Ability cadence must be finite and positive");
        }
    }

    public record SummonMetrics(
            int summonCount,
            int ownerCap,
            int uptimeTicks,
            int attacksPerMinion,
            int attackPeriodTicks,
            double maxHealth,
            double scaledDamagePerHit,
            boolean supportRole) {
        public SummonMetrics {
            if (summonCount < 1 || ownerCap < summonCount || uptimeTicks < 1
                    || attacksPerMinion < 1 || attackPeriodTicks < 1
                    || !Double.isFinite(maxHealth) || maxHealth <= 0D
                    || !Double.isFinite(scaledDamagePerHit) || scaledDamagePerHit < 0D)
                throw new IllegalArgumentException("Summon diagnostics are invalid");
        }
    }

    public record Row(
            String rootClassId,
            String formId,
            String abilityId,
            AbilitySlot slot,
            ClassBand band,
            int startLevel,
            int evaluationLevel,
            ClassResourceType resourceType,
            double resourceCost,
            double baseEffect,
            double startEffect,
            double normalizedEffect,
            double resourceEfficiency,
            Outlier outlier,
            Cadence cadence,
            Optional<SummonMetrics> summon,
            List<TargetScenario> scenarios) {
        public Row {
            Objects.requireNonNull(rootClassId, "rootClassId");
            Objects.requireNonNull(formId, "formId");
            Objects.requireNonNull(abilityId, "abilityId");
            Objects.requireNonNull(slot, "slot");
            Objects.requireNonNull(band, "band");
            Objects.requireNonNull(resourceType, "resourceType");
            Objects.requireNonNull(outlier, "outlier");
            Objects.requireNonNull(cadence, "cadence");
            summon = Objects.requireNonNull(summon, "summon");
            scenarios = List.copyOf(Objects.requireNonNull(scenarios, "scenarios"));
            if (startLevel < 1 || evaluationLevel < startLevel)
                throw new IllegalArgumentException("Ability level band is invalid");
            if (scenarios.size() != TargetDensity.values().length)
                throw new IllegalArgumentException("Every row must model every target density");
        }

        public TargetScenario scenario(TargetDensity density) {
            Objects.requireNonNull(density, "density");
            return scenarios.stream()
                    .filter(scenario -> scenario.density() == density)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Missing target density " + density));
        }
    }
}
