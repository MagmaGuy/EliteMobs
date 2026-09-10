package com.magmaguy.elitemobs.advancedcombat.analysis;

import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityEffect;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityFamily;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityLevelScaling;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityMechanic;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityCommitEffects;
import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilityRegistry;
import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilityDefinition;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassBand;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassResourceType;
import com.magmaguy.elitemobs.advancedcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.advancedcombat.minions.ClassMinionBalanceContract;
import com.magmaguy.elitemobs.advancedcombat.minions.ClassMinionTheme;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Static balance model for the active-ability catalog.
 *
 * <p>The score is deliberately normalized rather than pretending every heal, displacement and
 * mark is literal damage. Runtime-scaled payloads use the same level curve as execution; movement,
 * control and strategic mechanics remain fixed. Outliers compare resource efficiency with peers
 * in the same slot and progression band, avoiding false comparisons between root and terminal
 * abilities.</p>
 */
public final class ActiveAbilityBalanceAnalyzer {
    private static final double LOW_OUTLIER_RATIO = .67D;
    private static final double HIGH_OUTLIER_RATIO = 1.5D;

    private ActiveAbilityBalanceAnalyzer() {
    }

    public static ActiveAbilityBalanceReport analyzeBuiltIns() {
        return analyze(BuiltInClassContent.catalog(), BuiltInClassContent.abilityRegistry());
    }

    public static ActiveAbilityBalanceReport analyze(
            ClassCatalog catalog,
            FixedAbilityRegistry registry) {
        Objects.requireNonNull(catalog, "catalog");
        Objects.requireNonNull(registry, "registry");

        List<Draft> drafts = new ArrayList<>();
        for (ClassFormDefinition root : catalog.roots()) {
            add(drafts, registry, root.id(), root.id(), root.rootKit().resourceType(), root,
                    root.rootKit().mobility(), 1, 100);
        }
        for (ClassFormDefinition form : catalog.forms()) {
            ClassFormDefinition root = catalog.rootOf(form.id());
            int startLevel = form.band().effectiveStart();
            int evaluationLevel = Math.min(100, form.band().effectiveEnd());
            add(drafts, registry, root.id(), form.id(), root.rootKit().resourceType(), form,
                    form.signature(), startLevel, evaluationLevel);
            add(drafts, registry, root.id(), form.id(), root.rootKit().resourceType(), form,
                    form.utility(), startLevel, evaluationLevel);
        }

        Map<ScenarioPeer, Double> medians = scenarioMedians(drafts);
        List<ActiveAbilityBalanceReport.Row> rows = drafts.stream()
                .map(draft -> draft.toRow(medians))
                .sorted(Comparator.comparing((ActiveAbilityBalanceReport.Row row) -> row.slot().ordinal())
                        .thenComparing(row -> row.band().depth())
                        .thenComparing(ActiveAbilityBalanceReport.Row::formId))
                .toList();
        return new ActiveAbilityBalanceReport(rows);
    }

    private static void add(
            List<Draft> drafts,
            FixedAbilityRegistry registry,
            String rootClassId,
            String formId,
            ClassResourceType resourceType,
            ClassFormDefinition form,
            AbilityDefinition ability,
            int startLevel,
            int evaluationLevel) {
        FixedAbilitySpec spec = registry.require(ability.id());
        EffectBudget budget = effectBudget(spec);
        double start = budget.scaled() * AbilityLevelScaling.multiplier(startLevel)
                + budget.fixed();
        double normalized = budget.scaled() * AbilityLevelScaling.multiplier(evaluationLevel)
                + budget.fixed();
        double base = budget.scaled() + budget.fixed();
        double netResourceCost = Math.max(0D, spec.resourceCost()
                - AbilityCommitEffects.resolve(spec, evaluationLevel).resourceGrant());
        double efficiency = normalized / Math.max(1D, netResourceCost) * 100D;
        ActiveAbilityBalanceReport.Cadence cadence = cadence(spec, evaluationLevel, resourceType);
        List<ActiveAbilityBalanceReport.TargetScenario> scenarios = targetScenarios(
                spec, normalized, cadence.sustainableCastsPerMinute(), netResourceCost);
        Optional<ActiveAbilityBalanceReport.SummonMetrics> summon = summonMetrics(
                spec, evaluationLevel);
        drafts.add(new Draft(rootClassId, formId, spec, form.band(), resourceType,
                startLevel, evaluationLevel, base, start, normalized, efficiency,
                cadence, summon, scenarios));
    }

    private static EffectBudget effectBudget(FixedAbilitySpec spec) {
        double repetitions = spec.executionTraits().mechanics().contains(AbilityMechanic.EXPANDING_PULSES)
                ? 1D
                : Math.max(1, spec.tuning().repetitions());
        double deliveries = repetitions
                * Math.max(1, spec.tuning().projectileCount());
        double scaled = spec.tuning().damageMultiplier() * deliveries;
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.GUARANTEED_CRITICAL))
            scaled += spec.tuning().damageMultiplier() * deliveries * .5D;
        scaled += spec.tuning().healingFraction() * 10D * deliveries;
        scaled += spec.tuning().shieldFraction() * 8D * deliveries;
        if (spec.effects().contains(AbilityEffect.LIFESTEAL))
            scaled += spec.tuning().damageMultiplier() * deliveries * .22D;
        double fixed = movementBudget(spec);
        boolean missingHealthScaling = spec.executionTraits().mechanics()
                .contains(AbilityMechanic.MISSING_HEALTH_SCALING);
        for (AbilityEffect effect : spec.effects()) {
            double weight = fixedEffectWeight(effect, spec);
            if (missingHealthScaling
                    && (effect == AbilityEffect.STRENGTH || effect == AbilityEffect.SPEED)) {
                scaled += weight;
            } else if (levelScaledEffect(effect)) {
                scaled += weight;
            } else {
                fixed += weight;
            }
        }
        for (AbilityMechanic mechanic : spec.executionTraits().mechanics()) {
            double weight = mechanicWeight(mechanic);
            if (mechanic == AbilityMechanic.MISSING_HEALTH_SCALING
                    || levelScaledMechanic(mechanic)) {
                scaled += weight;
            } else {
                fixed += weight;
            }
        }
        return new EffectBudget(scaled, fixed);
    }

    private static boolean levelScaledEffect(AbilityEffect effect) {
        return switch (effect) {
            // Cleanse is immediate and binary. Every other non-payload effect is backed by a
            // level-scaled duration, displacement, modifier, or state amount at runtime.
            case CLEANSE -> false;
            default -> true;
        };
    }

    private static boolean levelScaledMechanic(AbilityMechanic mechanic) {
        return switch (mechanic) {
            case FOLLOW_CASTER_FIELD, WIND_UP, PLANTED_GUARD,
                    CONTROL_IMMUNITY, DEBUFF_IMMUNITY, OPENS_DEFENSE_BREAK,
                    DAMAGE_REDIRECT, MULTI_ALLY_REDIRECT, RETALIATION_RELEASE,
                    DEATH_GUARD, BARRIER_BREAK_HEAL, THREAT_TRIGGERED_PROTECTION,
                    DAMAGE_SHARE, HEAL_ECHO, SUSTAINED_TETHER, LIFESTEAL_WINDOW,
                    RETALIATION_HEAL, RESOURCE_BURST,
                    DETONATING_MARK, EXTEND_TAUNT, EXTEND_CONTROL_DURATION,
                    WARD_BREAK_SIGNAL -> true;
            default -> false;
        };
    }

    private static List<ActiveAbilityBalanceReport.TargetScenario> targetScenarios(
            FixedAbilitySpec spec,
            double perTargetEffect,
            double sustainableCastsPerMinute,
            double netResourceCost) {
        List<ActiveAbilityBalanceReport.TargetScenario> scenarios = new ArrayList<>();
        for (ActiveAbilityBalanceReport.TargetDensity density : ActiveAbilityBalanceReport.TargetDensity.values()) {
            int affectedTargets = affectedTargets(spec, density.targetCount());
            double castEffect = perTargetEffect * affectedTargets;
            scenarios.add(new ActiveAbilityBalanceReport.TargetScenario(
                    density,
                    density.targetCount(),
                    affectedTargets,
                    castEffect,
                    castEffect / Math.max(1D, netResourceCost) * 100D,
                    castEffect * sustainableCastsPerMinute,
                    ActiveAbilityBalanceReport.Outlier.NONE));
        }
        return List.copyOf(scenarios);
    }

    private static int affectedTargets(FixedAbilitySpec spec, int availableTargets) {
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.PROJECTILE_BOMBARDMENT))
            return 1;
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.SINGLE_ENEMY)) return 1;
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.PIERCING_CAST))
            return availableTargets;
        ClassMinionTheme minionTheme = ClassMinionTheme.forAbility(spec.id()).orElse(null);
        if (minionTheme != null && !minionTheme.supportRole()) return 1;
        return switch (spec.target()) {
            case SELF, AIMED_ENEMY, AIMED_ALLY -> 1;
            case FORWARD_ENEMIES -> spec.family() == AbilityFamily.PROJECTILE
                    ? 1
                    : availableTargets;
            case NEARBY_ENEMIES, NEARBY_ALLIES, MIXED_NEARBY, AIMED_LOCATION -> availableTargets;
        };
    }

    /** One cast per tick is the input-dedup ceiling for a resource-positive ability. */
    private static final double MAXIMUM_CASTS_PER_MINUTE = 1_200D;

    private static ActiveAbilityBalanceReport.Cadence cadence(
            FixedAbilitySpec spec,
            int evaluationLevel,
            ClassResourceType resourceType) {
        double netResourceCost = Math.max(0D, spec.resourceCost()
                - AbilityCommitEffects.resolve(spec, evaluationLevel).resourceGrant());
        double recoveryPerSecond = BuiltInClassContent.resourceDefinitions().get(resourceType).inCombatTickDelta();
        double resourceRefillSeconds = netResourceCost / recoveryPerSecond;
        double sustainable = resourceRefillSeconds <= 0D
                ? MAXIMUM_CASTS_PER_MINUTE
                : Math.min(MAXIMUM_CASTS_PER_MINUTE, 60D / resourceRefillSeconds);
        return new ActiveAbilityBalanceReport.Cadence(resourceRefillSeconds, sustainable);
    }

    private static Optional<ActiveAbilityBalanceReport.SummonMetrics> summonMetrics(
            FixedAbilitySpec spec,
            int evaluationLevel) {
        ClassMinionTheme theme = ClassMinionTheme.forAbility(spec.id()).orElse(null);
        if (theme == null) return Optional.empty();
        ClassMinionBalanceContract contract = ClassMinionBalanceContract.from(
                spec, theme, evaluationLevel);
        return Optional.of(new ActiveAbilityBalanceReport.SummonMetrics(
                contract.summonCount(),
                contract.ownerCap(),
                contract.uptimeTicks(),
                Math.max(1, contract.uptimeTicks() / contract.attackPeriodTicks()),
                contract.attackPeriodTicks(),
                contract.maxHealth(),
                contract.scaledDamageMultiplierPerHit(),
                theme.supportRole()));
    }

    private static double movementBudget(FixedAbilitySpec spec) {
        return switch (spec.family()) {
            case MOUNTED_CHARGE, BALLISTIC_LEAP, SAFE_DASH, SAFE_BLINK, ALLY_FLIGHT ->
                    Math.max(0D, spec.tuning().range()) / 8D;
            default -> 0D;
        };
    }

    private static double fixedEffectWeight(AbilityEffect effect, FixedAbilitySpec spec) {
        if (effect == AbilityEffect.ALLY_PROTECT
                && spec.executionTraits().mechanics()
                .contains(AbilityMechanic.THREAT_TRIGGERED_PROTECTION)) return 0D;
        double durationFactor = Math.sqrt(Math.max(20, spec.tuning().durationTicks()) / 100D);
        return switch (effect) {
            case DAMAGE, HEAL, LIFESTEAL, SHIELD -> 0D;
            case CLEANSE -> .55D;
            case KNOCKBACK -> .25D;
            case PULL -> .35D;
            case LAUNCH -> .45D;
            case SLOW -> .35D * durationFactor;
            case WEAKEN -> Math.max(0D, 1D - spec.tuning().weakenMultiplier())
                    * 3D * durationFactor;
            case GLOW -> .1D * durationFactor;
            case PARTY_DAMAGE_MARK -> Math.max(0D, spec.tuning().modifierMultiplier() - 1D)
                    * Math.max(1D, spec.tuning().durationTicks() / 20D) * .5D;
            case TAUNT -> .55D * durationFactor;
            case SELF_PROTECT -> .7D * durationFactor;
            case ALLY_PROTECT -> .8D * durationFactor;
            case SPEED -> .35D * durationFactor;
            case STRENGTH -> Math.max(.45D,
                    Math.max(0D, spec.tuning().modifierMultiplier() - 1D) * 3D) * durationFactor;
            case SPELL_STRENGTH -> Math.max(.45D,
                    Math.max(0D, spec.tuning().modifierMultiplier() - 1D) * 3D) * durationFactor;
            case SELF_VULNERABLE -> -Math.max(.35D,
                    Math.max(0D, spec.tuning().modifierMultiplier() - 1D) * 2D) * durationFactor;
            case INTERRUPT -> .45D;
            case FEAR -> .65D * durationFactor;
            case ROOT -> .75D * durationFactor;
            case BURN -> .2D * durationFactor;
        };
    }

    private static double mechanicWeight(AbilityMechanic mechanic) {
        return switch (mechanic) {
            case LOWEST_HEALTH_FIRST, PIERCING_CAST, FOLLOW_CASTER_FIELD, WIND_UP,
                    PLANTED_GUARD, HEAL_ECHO, GROUP_SCALING -> .25D;
            case EXECUTE_DAMAGE, CHAINING_CAST, DELAYED_PAYLOAD, RETALIATION_RELEASE,
                    LIFESTEAL_WINDOW, DETONATING_MARK, PARTY_BUFF -> .4D;
            case DAMAGE_REDIRECT, MULTI_ALLY_REDIRECT, SUSTAINED_TETHER,
                    CONTROL_IMMUNITY -> .55D;
            case DEATH_GUARD, DAMAGE_SHARE -> .8D;
            case RESOURCE_BURST, GUARANTEED_CRITICAL -> 0D;
            case MISSING_HEALTH_SCALING -> .4D;
            case EXPANDING_PULSES, PROJECTILE_BOMBARDMENT -> 0D;
            case RECENT_ATTACKERS, TAUNTED_TARGETS_ONLY, WOUNDED_TARGETS_ONLY,
                    CASTER_ONLY_FIELD, LARGE_OR_BOSS_ONLY, BOSS_ONLY,
                    MINIMUM_RANGE, REQUIRES_MOVEMENT, LOW_HEALTH_ALLY_ONLY -> -.15D;
            case RETALIATION_HEAL, HEALTH_SCALED_MARK,
                    CLUSTER_PROJECTILE, BARRIER_BREAK_HEAL,
                    THREAT_TRIGGERED_PROTECTION -> .4D;
            case DEBUFF_IMMUNITY, EXTEND_CONTROL_DURATION -> .55D;
            case REQUIRES_ACTIVE_FIELD, REQUIRES_WIND_UP,
                    REQUIRES_DEFENSE_BREAK, REQUIRES_CORPSE -> -.2D;
            case OPENS_DEFENSE_BREAK -> .2D;
            case WARD_BREAK_SIGNAL -> 0D;
            case EXTEND_TAUNT -> .2D;
            case SINGLE_ENEMY, CASTER_ONLY_SUPPORT, ASSISTED_ALLY_TARGETING -> 0D;
        };
    }

    private static Map<ScenarioPeer, Double> scenarioMedians(List<Draft> drafts) {
        Map<ScenarioPeer, List<Double>> grouped = new HashMap<>();
        for (Draft draft : drafts) {
            for (ActiveAbilityBalanceReport.TargetScenario scenario : draft.scenarios()) {
                ScenarioPeer peer = new ScenarioPeer(
                        draft.spec().slot(), draft.band(), scenario.density());
                grouped.computeIfAbsent(peer, ignored -> new ArrayList<>())
                        .add(scenario.sustainableEffectPerMinute());
            }
        }
        Map<ScenarioPeer, Double> medians = new HashMap<>();
        grouped.forEach((group, values) -> medians.put(group, median(values)));
        return medians;
    }

    private static double median(List<Double> input) {
        List<Double> sorted = input.stream().sorted().toList();
        int middle = sorted.size() / 2;
        return sorted.size() % 2 == 0
                ? (sorted.get(middle - 1) + sorted.get(middle)) / 2D
                : sorted.get(middle);
    }

    static ActiveAbilityBalanceReport.Outlier detectOutlier(double efficiency, double median) {
        if (median <= 0D) return ActiveAbilityBalanceReport.Outlier.NONE;
        if (efficiency < median * LOW_OUTLIER_RATIO) return ActiveAbilityBalanceReport.Outlier.LOW;
        if (efficiency > median * HIGH_OUTLIER_RATIO) return ActiveAbilityBalanceReport.Outlier.HIGH;
        return ActiveAbilityBalanceReport.Outlier.NONE;
    }

    private record EffectBudget(double scaled, double fixed) {
    }

    private record ScenarioPeer(
            AbilitySlot slot,
            ClassBand band,
            ActiveAbilityBalanceReport.TargetDensity density) {
    }

    private record Draft(
            String rootClassId,
            String formId,
            FixedAbilitySpec spec,
            ClassBand band,
            ClassResourceType resourceType,
            int startLevel,
            int evaluationLevel,
            double baseEffect,
            double startEffect,
            double normalizedEffect,
            double efficiency,
            ActiveAbilityBalanceReport.Cadence cadence,
            Optional<ActiveAbilityBalanceReport.SummonMetrics> summon,
            List<ActiveAbilityBalanceReport.TargetScenario> scenarios) {
        private ActiveAbilityBalanceReport.Row toRow(Map<ScenarioPeer, Double> medians) {
            List<ActiveAbilityBalanceReport.TargetScenario> rated = scenarios.stream()
                    .map(scenario -> new ActiveAbilityBalanceReport.TargetScenario(
                            scenario.density(),
                            scenario.targetCount(),
                            scenario.affectedTargets(),
                            scenario.castEffect(),
                            scenario.resourceEfficiency(),
                            scenario.sustainableEffectPerMinute(),
                            detectOutlier(
                                    scenario.sustainableEffectPerMinute(),
                                    medians.get(new ScenarioPeer(
                                            spec.slot(), band, scenario.density())))))
                    .toList();
            boolean high = rated.stream()
                    .anyMatch(scenario -> scenario.outlier() == ActiveAbilityBalanceReport.Outlier.HIGH);
            boolean low = rated.stream()
                    .anyMatch(scenario -> scenario.outlier() == ActiveAbilityBalanceReport.Outlier.LOW);
            ActiveAbilityBalanceReport.Outlier outlier = high && low
                    ? ActiveAbilityBalanceReport.Outlier.MIXED
                    : high
                    ? ActiveAbilityBalanceReport.Outlier.HIGH
                    : low
                    ? ActiveAbilityBalanceReport.Outlier.LOW
                    : ActiveAbilityBalanceReport.Outlier.NONE;
            return new ActiveAbilityBalanceReport.Row(rootClassId, formId, spec.id(), spec.slot(), band,
                    startLevel, evaluationLevel, resourceType, spec.resourceCost(), baseEffect,
                    startEffect, normalizedEffect, efficiency, outlier, cadence, summon, rated);
        }
    }
}
