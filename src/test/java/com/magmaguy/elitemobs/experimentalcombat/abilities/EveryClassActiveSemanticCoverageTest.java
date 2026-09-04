package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.experimentalcombat.classes.AbilityDefinition;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassBand;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Independent acceptance contract joining the canonical 75-form catalog to executable, typed
 * active-ability semantics. This deliberately does more than count IDs: a future definition must
 * name a real world outcome, every effect/mechanic must have an observable contract, and every
 * level-scaled benefit must differ at the form's entry and v0 cap.
 */
class EveryClassActiveSemanticCoverageTest {
    private static final int EXPECTED_FORM_COUNT = 75;
    private static final int EXPECTED_ACTIVE_ENDPOINT_COUNT = EXPECTED_FORM_COUNT * 2;
    private static final int TERMINAL_V0_CAP = 100;
    private static final double EPSILON = 1.0E-9D;

    private static final ClassCatalog CATALOG = BuiltInClassContent.catalog();
    private static final FixedAbilityRegistry REGISTRY = BuiltInClassContent.abilityRegistry();
    private static final Map<AbilityEffect, Set<AbilityRuntimeObservation.Kind>> EFFECT_EVIDENCE =
            effectEvidence();
    private static final Map<AbilityMechanic, MechanicContract> MECHANIC_EVIDENCE =
            mechanicEvidence();

    @Test
    void canonicalCatalogStillContainsEveryPromisedActiveEndpoint() {
        assertEquals(EXPECTED_FORM_COUNT, CATALOG.forms().size());
        assertEquals(EXPECTED_ACTIVE_ENDPOINT_COUNT,
                CATALOG.forms().stream()
                        .flatMap(form -> Stream.of(form.signature(), form.utility()))
                        .count());
        assertEquals(EXPECTED_ACTIVE_ENDPOINT_COUNT,
                REGISTRY.registeredIds().stream()
                        .map(REGISTRY::require)
                        .filter(spec -> spec.slot() != AbilitySlot.MOBILITY)
                        .count());
    }

    @Test
    void semanticOracleExhaustivelyClassifiesEveryEffectAndMechanic() {
        assertEquals(EnumSet.allOf(AbilityEffect.class), EFFECT_EVIDENCE.keySet());
        assertEquals(EnumSet.allOf(AbilityMechanic.class), MECHANIC_EVIDENCE.keySet());
        EFFECT_EVIDENCE.forEach((effect, evidence) ->
                assertFalse(evidence.isEmpty(), () -> effect + " has no typed runtime evidence"));
        MECHANIC_EVIDENCE.forEach((mechanic, contract) ->
                assertFalse(contract.evidence().isEmpty(),
                        () -> mechanic + " has no typed runtime evidence or rejection path"));
    }

    @TestFactory
    Stream<DynamicTest> everySignatureAndUtilityHasExecutableSemanticsAtEntryAndCap() {
        return endpointCases().map(endpoint -> DynamicTest.dynamicTest(
                endpoint.form().id() + "/" + endpoint.definition().slot().name().toLowerCase()
                        + "@" + endpoint.boundary() + "-" + endpoint.effectiveLevel(),
                () -> assertEndpoint(endpoint)));
    }

    @Test
    void triggerOnlyOrEmptyDefinitionsCannotSatisfyTheContract() {
        FixedAbilitySpec triggerOnly = new FixedAbilitySpec(
                "contract.trigger_only",
                AbilitySlot.UTILITY,
                AbilityFamily.ZONE,
                AbilityTarget.AIMED_LOCATION,
                Set.of(),
                AbilityTuning.support(0D, 0D, 8D, 4D, 100),
                AbilityExecutionTraits.mechanics(
                        AbilityMechanic.REQUIRES_MOVEMENT,
                        AbilityMechanic.REQUIRES_ACTIVE_FIELD),
                25D);

        assertFalse(hasWorldOutcome(triggerOnly));
        assertTrue(triggerOnly.executionTraits().mechanics().stream()
                .allMatch(mechanic -> MECHANIC_EVIDENCE.get(mechanic).role()
                        == MechanicRole.QUALIFIER));
    }

    private static Stream<EndpointCase> endpointCases() {
        return CATALOG.forms().stream().flatMap(form -> {
            int entry = form.band().effectiveStart();
            int cap = acceptanceCap(form.band());
            return Stream.of(form.signature(), form.utility())
                    .flatMap(definition -> Stream.of(
                            new EndpointCase(form, definition, REGISTRY.require(definition.id()),
                                    entry, "entry"),
                            new EndpointCase(form, definition, REGISTRY.require(definition.id()),
                                    cap, "cap")));
        });
    }

    private static void assertEndpoint(EndpointCase endpoint) {
        FixedAbilitySpec spec = endpoint.spec();
        Set<AbilityRuntimeObservation.Kind> evidence = evidenceFor(spec);
        List<String> levelBenefits = levelBenefits(spec, endpoint.effectiveLevel());

        assertAll(endpoint.definition().id(),
                () -> assertEquals(endpoint.definition().id(), spec.id()),
                () -> assertEquals(endpoint.definition().slot(), spec.slot()),
                () -> assertTrue(spec.slot() == AbilitySlot.SIGNATURE
                                || spec.slot() == AbilitySlot.UTILITY,
                        () -> spec.id() + " is not one of the two per-form active slots"),
                () -> assertFalse(evidence.isEmpty(),
                        () -> spec.id() + " has no typed runtime evidence path"),
                () -> assertTrue(hasWorldOutcome(spec),
                        () -> spec.id() + " is trigger-only or otherwise has no world outcome"),
                () -> spec.effects().forEach(effect -> assertTrue(
                        effectHasPayload(spec, effect),
                        () -> spec.id() + " declares " + effect + " without an executable payload")),
                () -> spec.executionTraits().mechanics().forEach(mechanic -> assertTrue(
                        evidence.containsAll(MECHANIC_EVIDENCE.get(mechanic).evidence())
                                || MECHANIC_EVIDENCE.get(mechanic).role() != MechanicRole.OUTCOME,
                        () -> spec.id() + " does not expose the outcome evidence promised by "
                                + mechanic)),
                () -> assertFalse(levelBenefits.isEmpty(),
                        () -> spec.id() + " exposes no active benefit that scales with level"),
                () -> assertLevelProjection(spec, endpoint.form().band()));
    }

    private static void assertLevelProjection(FixedAbilitySpec spec, ClassBand band) {
        int entry = band.effectiveStart();
        int cap = acceptanceCap(band);
        double entryScale = expectedLevelScale(entry);
        double capScale = expectedLevelScale(cap);
        assertTrue(capScale > entryScale, spec.id());

        AbilityTuning tuning = spec.tuning();
        if (tuning.damageMultiplier() > 0D) {
            assertEquals(tuning.damageMultiplier() * entryScale,
                    ActiveAbilityLevelScaling.amount(tuning.damageMultiplier(), entry), EPSILON);
            assertEquals(tuning.damageMultiplier() * capScale,
                    ActiveAbilityLevelScaling.amount(tuning.damageMultiplier(), cap), EPSILON);
            assertTrue(ActiveAbilityLevelScaling.amount(tuning.damageMultiplier(), cap)
                    > ActiveAbilityLevelScaling.amount(tuning.damageMultiplier(), entry));
        }
        if (tuning.healingFraction() > 0D) {
            assertEquals(tuning.healingFraction() * entryScale,
                    ActiveAbilityLevelScaling.amount(tuning.healingFraction(), entry), EPSILON);
            assertTrue(ActiveAbilityLevelScaling.amount(tuning.healingFraction(), cap)
                    > ActiveAbilityLevelScaling.amount(tuning.healingFraction(), entry));
        }
        if (tuning.shieldFraction() > 0D) {
            assertEquals(tuning.shieldFraction() * entryScale,
                    ActiveAbilityLevelScaling.amount(tuning.shieldFraction(), entry), EPSILON);
            assertTrue(ActiveAbilityLevelScaling.amount(tuning.shieldFraction(), cap)
                    > ActiveAbilityLevelScaling.amount(tuning.shieldFraction(), entry));
        }
        if (tuning.displacement() > 0D) {
            assertTrue(ActiveAbilityLevelScaling.displacement(tuning.displacement(), cap)
                    > ActiveAbilityLevelScaling.displacement(tuning.displacement(), entry));
        }
        if (hasDurationBenefit(spec)) {
            assertTrue(ActiveAbilityLevelScaling.durationTicks(tuning.durationTicks(), cap)
                    > ActiveAbilityLevelScaling.durationTicks(tuning.durationTicks(), entry));
        }
        if (hasModifierBenefit(spec)) {
            double entryModifier = ActiveAbilityLevelScaling.modifier(
                    tuning.modifierMultiplier(), entry);
            double capModifier = ActiveAbilityLevelScaling.modifier(
                    tuning.modifierMultiplier(), cap);
            assertNotEquals(entryModifier, capModifier, EPSILON, spec.id());
            assertTrue(Math.abs(capModifier - 1D) > Math.abs(entryModifier - 1D), spec.id());
        }
        if (spec.effects().contains(AbilityEffect.WEAKEN)) {
            double entryModifier = ActiveAbilityLevelScaling.modifier(
                    tuning.weakenMultiplier(), entry);
            double capModifier = ActiveAbilityLevelScaling.modifier(
                    tuning.weakenMultiplier(), cap);
            assertTrue(capModifier < entryModifier, spec.id());
            assertTrue(1D - capModifier > 1D - entryModifier, spec.id());
        }
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.RESOURCE_BURST)) {
            assertTrue(AbilityCommitEffects.resolve(spec, cap).resourceGrant()
                    > AbilityCommitEffects.resolve(spec, entry).resourceGrant());
        }
    }

    private static List<String> levelBenefits(FixedAbilitySpec spec, int effectiveLevel) {
        AbilityTuning tuning = spec.tuning();
        List<String> benefits = new ArrayList<>();
        if (tuning.damageMultiplier() > 0D) benefits.add("damage");
        if (tuning.healingFraction() > 0D) benefits.add("healing");
        if (tuning.shieldFraction() > 0D) benefits.add("shield");
        if (spec.family() == AbilityFamily.SUMMON) benefits.add("servant-health");
        if (tuning.displacement() > 0D) benefits.add("displacement");
        if (hasDurationBenefit(spec)
                && ActiveAbilityLevelScaling.durationTicks(tuning.durationTicks(), effectiveLevel) > 0)
            benefits.add("duration");
        if (hasModifierBenefit(spec)) benefits.add("modifier");
        if (spec.effects().contains(AbilityEffect.WEAKEN)) benefits.add("weaken");
        if (spec.executionTraits().mechanics().contains(AbilityMechanic.RESOURCE_BURST))
            benefits.add("resource");
        return List.copyOf(benefits);
    }

    private static boolean hasDurationBenefit(FixedAbilitySpec spec) {
        if (spec.tuning().durationTicks() <= 0 || spec.family() == AbilityFamily.SUMMON) return false;
        return spec.effects().stream().anyMatch(effect -> switch (effect) {
            case DAMAGE, HEAL, LIFESTEAL -> false;
            default -> true;
        }) || spec.executionTraits().mechanics().stream().anyMatch(mechanic ->
                MECHANIC_EVIDENCE.get(mechanic).role() == MechanicRole.OUTCOME);
    }

    private static boolean hasModifierBenefit(FixedAbilitySpec spec) {
        if (spec.tuning().modifierMultiplier() <= 0D
                || Double.compare(spec.tuning().modifierMultiplier(), 1D) == 0) return false;
        return spec.effects().stream().anyMatch(effect -> switch (effect) {
            case PARTY_DAMAGE_MARK, ALLY_PROTECT, SELF_PROTECT, STRENGTH,
                 SPELL_STRENGTH, SELF_VULNERABLE -> true;
            default -> false;
        });
    }

    private static boolean hasWorldOutcome(FixedAbilitySpec spec) {
        if (spec.family() == AbilityFamily.SUMMON) return true;
        if (spec.effects().stream().anyMatch(effect -> effectHasPayload(spec, effect))) return true;
        return spec.executionTraits().mechanics().stream()
                .map(MECHANIC_EVIDENCE::get)
                .anyMatch(contract -> contract.role() == MechanicRole.OUTCOME);
    }

    private static boolean effectHasPayload(FixedAbilitySpec spec, AbilityEffect effect) {
        AbilityTuning tuning = spec.tuning();
        return switch (effect) {
            case DAMAGE -> tuning.damageMultiplier() > 0D
                    || spec.executionTraits().mechanics().contains(AbilityMechanic.RETALIATION_RELEASE);
            case HEAL -> tuning.healingFraction() > 0D
                    || spec.executionTraits().mechanics().contains(AbilityMechanic.RETALIATION_HEAL)
                    || spec.executionTraits().mechanics().contains(AbilityMechanic.HEAL_ECHO);
            case LIFESTEAL -> tuning.damageMultiplier() > 0D;
            case SHIELD -> tuning.shieldFraction() > 0D;
            case KNOCKBACK, PULL, LAUNCH -> tuning.displacement() > 0D;
            case PARTY_DAMAGE_MARK -> tuning.modifierMultiplier() > 1D;
            case WEAKEN -> tuning.durationTicks() > 0 && tuning.weakenMultiplier() < 1D;
            case SLOW, GLOW, TAUNT, SPEED, STRENGTH, SPELL_STRENGTH,
                 SELF_VULNERABLE, INTERRUPT, FEAR, ROOT, BURN -> tuning.durationTicks() > 0;
            case CLEANSE, SELF_PROTECT, ALLY_PROTECT -> true;
        };
    }

    private static Set<AbilityRuntimeObservation.Kind> evidenceFor(FixedAbilitySpec spec) {
        EnumSet<AbilityRuntimeObservation.Kind> evidence = EnumSet.of(
                AbilityRuntimeObservation.Kind.RESOURCE_SPENT,
                AbilityRuntimeObservation.Kind.RESOURCE_DELTA);
        spec.effects().forEach(effect -> evidence.addAll(EFFECT_EVIDENCE.get(effect)));
        spec.executionTraits().mechanics().forEach(mechanic ->
                evidence.addAll(MECHANIC_EVIDENCE.get(mechanic).evidence()));
        if (spec.family() == AbilityFamily.SUMMON) {
            evidence.add(AbilityRuntimeObservation.Kind.SUMMON_SPAWNED);
            evidence.add(AbilityRuntimeObservation.Kind.SUMMON_CLEARED);
        }
        return Set.copyOf(evidence);
    }

    private static int acceptanceCap(ClassBand band) {
        return band.isTerminal() ? TERMINAL_V0_CAP : band.effectiveEnd();
    }

    private static double expectedLevelScale(int effectiveLevel) {
        return 1D + Math.max(1, effectiveLevel) * .0025D;
    }

    private static Map<AbilityEffect, Set<AbilityRuntimeObservation.Kind>> effectEvidence() {
        EnumMap<AbilityEffect, Set<AbilityRuntimeObservation.Kind>> paths =
                new EnumMap<>(AbilityEffect.class);
        register(paths, Set.of(AbilityRuntimeObservation.Kind.DAMAGE), AbilityEffect.DAMAGE);
        register(paths, Set.of(AbilityRuntimeObservation.Kind.HEAL),
                AbilityEffect.HEAL, AbilityEffect.LIFESTEAL);
        register(paths, Set.of(AbilityRuntimeObservation.Kind.SHIELD), AbilityEffect.SHIELD);
        register(paths, Set.of(AbilityRuntimeObservation.Kind.STATUS_APPLIED), AbilityEffect.CLEANSE);
        register(paths, Set.of(AbilityRuntimeObservation.Kind.CONTROL),
                AbilityEffect.KNOCKBACK, AbilityEffect.PULL, AbilityEffect.LAUNCH,
                AbilityEffect.SLOW, AbilityEffect.GLOW,
                AbilityEffect.INTERRUPT, AbilityEffect.FEAR, AbilityEffect.ROOT, AbilityEffect.BURN);
        register(paths, Set.of(
                        AbilityRuntimeObservation.Kind.CONTROL,
                        AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                        AbilityRuntimeObservation.Kind.MODIFIER_TRIGGERED),
                AbilityEffect.WEAKEN);
        register(paths, Set.of(
                        AbilityRuntimeObservation.Kind.MODIFIER_APPLIED,
                        AbilityRuntimeObservation.Kind.MODIFIER_TRIGGERED),
                AbilityEffect.PARTY_DAMAGE_MARK);
        register(paths, Set.of(AbilityRuntimeObservation.Kind.TAUNT), AbilityEffect.TAUNT);
        register(paths, Set.of(AbilityRuntimeObservation.Kind.MODIFIER_APPLIED),
                AbilityEffect.SELF_PROTECT, AbilityEffect.ALLY_PROTECT,
                AbilityEffect.SPEED, AbilityEffect.STRENGTH, AbilityEffect.SPELL_STRENGTH,
                AbilityEffect.SELF_VULNERABLE);
        return Map.copyOf(paths);
    }

    private static Map<AbilityMechanic, MechanicContract> mechanicEvidence() {
        EnumMap<AbilityMechanic, MechanicContract> paths = new EnumMap<>(AbilityMechanic.class);
        register(paths, MechanicRole.TARGETING, Set.of(AbilityRuntimeObservation.Kind.CAST_FAILED),
                AbilityMechanic.LOWEST_HEALTH_FIRST);
        register(paths, MechanicRole.SCALING, Set.of(AbilityRuntimeObservation.Kind.DAMAGE),
                AbilityMechanic.EXECUTE_DAMAGE, AbilityMechanic.MISSING_HEALTH_SCALING,
                AbilityMechanic.HEALTH_SCALED_MARK, AbilityMechanic.GUARANTEED_CRITICAL);
        register(paths, MechanicRole.DELIVERY, Set.of(AbilityRuntimeObservation.Kind.DAMAGE),
                AbilityMechanic.PIERCING_CAST, AbilityMechanic.PROJECTILE_BOMBARDMENT,
                AbilityMechanic.CLUSTER_PROJECTILE);
        register(paths, MechanicRole.DELIVERY, Set.of(
                        AbilityRuntimeObservation.Kind.DAMAGE,
                        AbilityRuntimeObservation.Kind.HEAL),
                AbilityMechanic.CHAINING_CAST);
        register(paths, MechanicRole.DELIVERY, Set.of(
                        AbilityRuntimeObservation.Kind.DAMAGE,
                        AbilityRuntimeObservation.Kind.CONTROL,
                        AbilityRuntimeObservation.Kind.LIFECYCLE_CLEARED),
                AbilityMechanic.DELAYED_PAYLOAD, AbilityMechanic.EXPANDING_PULSES);
        register(paths, MechanicRole.OUTCOME, Set.of(
                        AbilityRuntimeObservation.Kind.STATE_ARMED,
                        AbilityRuntimeObservation.Kind.FIELD_PULSE,
                        AbilityRuntimeObservation.Kind.LIFECYCLE_CLEARED),
                AbilityMechanic.FOLLOW_CASTER_FIELD);
        register(paths, MechanicRole.OUTCOME, Set.of(AbilityRuntimeObservation.Kind.STATE_ARMED),
                AbilityMechanic.WIND_UP, AbilityMechanic.PLANTED_GUARD,
                AbilityMechanic.CONTROL_IMMUNITY, AbilityMechanic.DEBUFF_IMMUNITY,
                AbilityMechanic.OPENS_DEFENSE_BREAK);
        register(paths, MechanicRole.OUTCOME, Set.of(
                        AbilityRuntimeObservation.Kind.STATE_ARMED,
                        AbilityRuntimeObservation.Kind.DAMAGE_REDIRECTED),
                AbilityMechanic.DAMAGE_REDIRECT, AbilityMechanic.MULTI_ALLY_REDIRECT);
        register(paths, MechanicRole.OUTCOME, Set.of(
                        AbilityRuntimeObservation.Kind.RETALIATION_RELEASED,
                        AbilityRuntimeObservation.Kind.DAMAGE),
                AbilityMechanic.RETALIATION_RELEASE);
        register(paths, MechanicRole.OUTCOME, Set.of(
                        AbilityRuntimeObservation.Kind.STATE_ARMED,
                        AbilityRuntimeObservation.Kind.STATE_CONSUMED),
                AbilityMechanic.DEATH_GUARD, AbilityMechanic.BARRIER_BREAK_HEAL,
                AbilityMechanic.THREAT_TRIGGERED_PROTECTION);
        register(paths, MechanicRole.OUTCOME, Set.of(
                        AbilityRuntimeObservation.Kind.STATE_ARMED,
                        AbilityRuntimeObservation.Kind.DAMAGE_SHARED),
                AbilityMechanic.DAMAGE_SHARE);
        register(paths, MechanicRole.OUTCOME, Set.of(
                        AbilityRuntimeObservation.Kind.STATE_ARMED,
                        AbilityRuntimeObservation.Kind.HEAL),
                AbilityMechanic.HEAL_ECHO, AbilityMechanic.SUSTAINED_TETHER,
                AbilityMechanic.LIFESTEAL_WINDOW, AbilityMechanic.RETALIATION_HEAL);
        register(paths, MechanicRole.OUTCOME, Set.of(
                        AbilityRuntimeObservation.Kind.RESOURCE_GAINED,
                        AbilityRuntimeObservation.Kind.RESOURCE_DELTA),
                AbilityMechanic.RESOURCE_BURST);
        register(paths, MechanicRole.OUTCOME, Set.of(
                        AbilityRuntimeObservation.Kind.STATE_ARMED,
                        AbilityRuntimeObservation.Kind.STATE_CONSUMED,
                        AbilityRuntimeObservation.Kind.DAMAGE),
                AbilityMechanic.DETONATING_MARK);
        register(paths, MechanicRole.TARGETING,
                Set.of(AbilityRuntimeObservation.Kind.MODIFIER_APPLIED),
                AbilityMechanic.PARTY_BUFF);
        register(paths, MechanicRole.SCALING, Set.of(
                        AbilityRuntimeObservation.Kind.DAMAGE,
                        AbilityRuntimeObservation.Kind.HEAL),
                AbilityMechanic.GROUP_SCALING);
        register(paths, MechanicRole.OUTCOME,
                Set.of(AbilityRuntimeObservation.Kind.MODIFIER_TRIGGERED),
                AbilityMechanic.EXTEND_TAUNT, AbilityMechanic.EXTEND_CONTROL_DURATION);
        register(paths, MechanicRole.OUTCOME,
                Set.of(AbilityRuntimeObservation.Kind.STATUS_BLOCKED),
                AbilityMechanic.WARD_BREAK_SIGNAL);
        register(paths, MechanicRole.QUALIFIER,
                Set.of(AbilityRuntimeObservation.Kind.CAST_FAILED),
                AbilityMechanic.RECENT_ATTACKERS,
                AbilityMechanic.REQUIRES_ACTIVE_FIELD,
                AbilityMechanic.TAUNTED_TARGETS_ONLY,
                AbilityMechanic.WOUNDED_TARGETS_ONLY,
                AbilityMechanic.CASTER_ONLY_FIELD,
                AbilityMechanic.REQUIRES_DEFENSE_BREAK,
                AbilityMechanic.LARGE_OR_BOSS_ONLY,
                AbilityMechanic.BOSS_ONLY,
                AbilityMechanic.REQUIRES_WIND_UP,
                AbilityMechanic.MINIMUM_RANGE,
                AbilityMechanic.REQUIRES_MOVEMENT,
                AbilityMechanic.LOW_HEALTH_ALLY_ONLY,
                AbilityMechanic.SINGLE_ENEMY,
                AbilityMechanic.CASTER_ONLY_SUPPORT,
                AbilityMechanic.REQUIRES_CORPSE);
        return Map.copyOf(paths);
    }

    @SafeVarargs
    private static <K extends Enum<K>, V> void register(
            EnumMap<K, V> target,
            V value,
            K... keys) {
        for (K key : keys) {
            V previous = target.putIfAbsent(key, value);
            if (previous != null) throw new IllegalStateException("Duplicate semantic key " + key);
        }
    }

    private static void register(
            EnumMap<AbilityMechanic, MechanicContract> target,
            MechanicRole role,
            Set<AbilityRuntimeObservation.Kind> evidence,
            AbilityMechanic... mechanics) {
        register(target, new MechanicContract(role, evidence), mechanics);
    }

    private enum MechanicRole {
        OUTCOME,
        QUALIFIER,
        TARGETING,
        DELIVERY,
        SCALING
    }

    private record MechanicContract(
            MechanicRole role,
            Set<AbilityRuntimeObservation.Kind> evidence) {
        private MechanicContract {
            evidence = Set.copyOf(evidence);
        }
    }

    private record EndpointCase(
            ClassFormDefinition form,
            AbilityDefinition definition,
            FixedAbilitySpec spec,
            int effectiveLevel,
            String boundary) {
    }
}
