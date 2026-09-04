package com.magmaguy.elitemobs.experimentalcombat.minions;

import com.magmaguy.elitemobs.api.mind.EliteMindBodyLocomotion;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityEffect;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityFamily;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityMechanic;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityRuntimeObservation;
import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilityRegistry;
import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Independent servant-fantasy oracle for every summoning branch and descendant. */
class EverySummonFantasySemanticContractTest {
    private static final double EPSILON = 1.0E-9D;
    private static final int CORPSE_LIFETIME_TICKS = 30 * 20;
    private static final FixedAbilityRegistry REGISTRY = BuiltInClassContent.abilityRegistry();
    private static final Map<String, SummonOracle> ORACLE = Map.ofEntries(
            Map.entry("necromancer.signature", new SummonOracle(
                    61, 90, ClassMinionTheme.UNDEAD, 24D,
                    List.of(grounded(EntityType.ZOMBIE), grounded(EntityType.SKELETON),
                            grounded(EntityType.HUSK)),
                    false, 1, 3, 800, 80, .28D, 45D,
                    .14D, Set.of(), true, false)),
            Map.entry("lich.signature", new SummonOracle(
                    91, 100, ClassMinionTheme.UNDEAD, 24D,
                    List.of(grounded(EntityType.ZOMBIE), grounded(EntityType.SKELETON),
                            grounded(EntityType.HUSK)),
                    false, 1, 3, 800, 80, .38D, 55D,
                    .14D, Set.of(AbilityEffect.WEAKEN), false, false)),
            Map.entry("plaguebringer.signature", new SummonOracle(
                    91, 100, ClassMinionTheme.UNDEAD, 24D,
                    List.of(grounded(EntityType.ZOMBIE), grounded(EntityType.SKELETON),
                            grounded(EntityType.HUSK)),
                    false, 1, 3, 800, 66, .25D, 50D,
                    0D, Set.of(AbilityEffect.SLOW, AbilityEffect.WEAKEN), false, false)),
            Map.entry("summoner.signature", new SummonOracle(
                    61, 90, ClassMinionTheme.ANIMAL, 20D,
                    List.of(grounded(EntityType.WOLF), grounded(EntityType.FOX),
                            grounded(EntityType.POLAR_BEAR)),
                    false, 1, 3, 600, 66, .32D, 50D,
                    0D, Set.of(AbilityEffect.INTERRUPT), false, false)),
            Map.entry("demonologist.signature", new SummonOracle(
                    91, 100, ClassMinionTheme.NETHER, 30D,
                    List.of(grounded(EntityType.PIGLIN_BRUTE), grounded(EntityType.HOGLIN),
                            flying(EntityType.BLAZE)),
                    false, 1, 3, 500, 62, .42D, 60D,
                    0D, Set.of(AbilityEffect.FEAR), false, true)),
            Map.entry("spiritbinder.signature", new SummonOracle(
                    91, 100, ClassMinionTheme.SPIRIT, 18D,
                    List.of(flying(EntityType.VEX)),
                    true, 1, 3, 600, 75, 0D, 60D,
                    0D, Set.of(), false, false)));

    @Test
    void oracleCoversEveryAndOnlyCanonicalSummonEndpoint() {
        Set<String> canonical = REGISTRY.registeredIds().stream()
                .map(REGISTRY::require)
                .filter(spec -> spec.family() == AbilityFamily.SUMMON)
                .map(FixedAbilitySpec::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        assertEquals(ORACLE.keySet(), canonical);
        assertEquals(Set.of(
                        AbilityRuntimeObservation.Kind.SUMMON_SPAWNED,
                        AbilityRuntimeObservation.Kind.SUMMON_CLEARED),
                summonLifecycleEvidence());
    }

    @TestFactory
    Stream<DynamicTest> everyServantHasExactThemeBalanceImpactAndLevelProjection() {
        return ORACLE.entrySet().stream().flatMap(entry -> Stream.of(
                summonTest(entry.getKey(), entry.getValue(), entry.getValue().entryLevel(), "entry"),
                summonTest(entry.getKey(), entry.getValue(), entry.getValue().capLevel(), "cap")));
    }

    @Test
    void servantAiPrioritizesCombatThenOwnerFollowThenLocalWander() {
        assertEquals(MinionBehaviorPolicy.Intent.COMBAT,
                MinionBehaviorPolicy.choose(true, 100D, true));
        assertEquals(MinionBehaviorPolicy.Intent.FOLLOW,
                MinionBehaviorPolicy.choose(false, 4.01D, true));
        assertEquals(MinionBehaviorPolicy.Intent.WANDER,
                MinionBehaviorPolicy.choose(false, 4D, true));
        assertEquals(MinionBehaviorPolicy.Intent.IDLE,
                MinionBehaviorPolicy.choose(false, 4D, false));
    }

    @Test
    void ownerCapReplacementAndExplicitCleanupAreDeterministic() {
        ClassMinionRoster<String> roster = new ClassMinionRoster<>(3);
        assertTrue(roster.admitAll(List.of("first", "second", "third")).isEmpty());
        assertEquals(List.of("first"), roster.admit("replacement"));
        assertEquals(List.of("second", "third", "replacement"), roster.entries());
        assertEquals(List.of("second", "third", "replacement"), roster.drain());
        assertTrue(roster.entries().isEmpty());
    }

    @Test
    void necromancerAloneConsumesAThirtySecondPerPlayerCorpseOpportunity() {
        ORACLE.forEach((abilityId, expected) -> assertEquals(
                expected.requiresCorpse(),
                REGISTRY.require(abilityId).executionTraits().mechanics()
                        .contains(AbilityMechanic.REQUIRES_CORPSE),
                abilityId));

        CorpseOpportunityLedger ledger = new CorpseOpportunityLedger();
        UUID corpse = UUID.randomUUID();
        UUID necromancer = UUID.randomUUID();
        UUID secondNecromancer = UUID.randomUUID();
        ledger.offer(corpse, Set.of(necromancer, secondNecromancer), CORPSE_LIFETIME_TICKS);

        assertTrue(ledger.availableTo(corpse, necromancer, CORPSE_LIFETIME_TICKS - 1L));
        assertTrue(ledger.consume(corpse, necromancer, CORPSE_LIFETIME_TICKS - 1L));
        assertTrue(ledger.availableTo(corpse, secondNecromancer, CORPSE_LIFETIME_TICKS - 1L));
        assertFalse(ledger.availableTo(corpse, secondNecromancer, CORPSE_LIFETIME_TICKS));
        assertFalse(ledger.contains(corpse));
    }

    @Test
    void demonologistOwnsAReferenceCountedSixBlockPortalThatFullyClears() {
        assertTrue(ORACLE.get("demonologist.signature").requiresPortal());
        ORACLE.forEach((abilityId, expected) -> assertEquals(
                expected.requiresPortal(), expected.theme() == ClassMinionTheme.NETHER,
                abilityId));

        List<GateBlockPosition> surface = List.of(
                new GateBlockPosition(0, 65, 0), new GateBlockPosition(1, 65, 0),
                new GateBlockPosition(0, 66, 0), new GateBlockPosition(1, 66, 0),
                new GateBlockPosition(0, 67, 0), new GateBlockPosition(1, 67, 0));
        AbyssalGateFootprint footprint = new AbyssalGateFootprint(GateAxis.X, surface);
        AbyssalGateOwnershipLedger ownership = new AbyssalGateOwnershipLedger();
        UUID cast = UUID.randomUUID();
        ownership.reserve(cast, footprint.blocks());

        assertEquals(6, footprint.blocks().size());
        assertTrue(surface.stream().allMatch(ownership::isOwned));
        assertEquals(Set.copyOf(surface), ownership.release(cast));
        assertTrue(ownership.isEmpty());
    }

    private static DynamicTest summonTest(
            String abilityId,
            SummonOracle expected,
            int effectiveLevel,
            String boundary) {
        return DynamicTest.dynamicTest(abilityId + "@" + boundary + "-" + effectiveLevel, () -> {
            FixedAbilitySpec spec = REGISTRY.require(abilityId);
            ClassMinionTheme theme = ClassMinionTheme.forAbility(abilityId).orElseThrow();
            ClassMinionBalanceContract actual = ClassMinionBalanceContract.from(
                    spec, theme, effectiveLevel);
            ClassMinionImpactPlan impact = ClassMinionImpactPlan.from(spec);

            assertEquals(AbilityFamily.SUMMON, spec.family());
            assertEquals(expected.theme(), theme);
            assertEquals(expected.supportRole(), theme.supportRole());
            assertEquals(expected.baseHealth(), theme.baseHealth(), EPSILON);
            assertEquals(expected.carriers(), theme.carriers().stream()
                    .map(carrier -> new CarrierOracle(carrier.entityType(), carrier.locomotion()))
                    .toList());
            assertEquals(expected.summonCount(), actual.summonCount());
            assertEquals(expected.ownerCap(), actual.ownerCap());
            assertEquals(expected.uptimeTicks(), actual.uptimeTicks());
            assertEquals(expected.attackPeriodTicks(), actual.attackPeriodTicks());
            assertEquals(expected.damageMultiplierPerHit(),
                    actual.damageMultiplierPerHit(), EPSILON);
            assertEquals(expected.damageMultiplierPerHit() * expectedLevelScale(effectiveLevel),
                    actual.scaledDamageMultiplierPerHit(), EPSILON);
            assertEquals(expected.baseHealth() * expectedLevelScale(effectiveLevel),
                    actual.maxHealth(), EPSILON);
            assertEquals(expected.resourceCost(), actual.resourceCost(), EPSILON);
            assertEquals(expected.lifestealFraction(), impact.lifestealFraction(), EPSILON);
            assertEquals(expected.controlEffects(), impact.controlEffects());

            ClassMinionBalanceContract entry = ClassMinionBalanceContract.from(
                    spec, theme, expected.entryLevel());
            ClassMinionBalanceContract cap = ClassMinionBalanceContract.from(
                    spec, theme, expected.capLevel());
            assertTrue(cap.maxHealth() > entry.maxHealth(), abilityId);
            if (expected.damageMultiplierPerHit() > 0D) {
                assertTrue(cap.scaledDamageMultiplierPerHit()
                        > entry.scaledDamageMultiplierPerHit(), abilityId);
            } else {
                assertEquals(0D, entry.scaledDamageMultiplierPerHit(), EPSILON);
                assertEquals(0D, cap.scaledDamageMultiplierPerHit(), EPSILON);
                assertTrue(spec.tuning().healingFraction() > 0D);
                assertTrue(spec.tuning().shieldFraction() > 0D);
            }
        });
    }

    private static Set<AbilityRuntimeObservation.Kind> summonLifecycleEvidence() {
        return Set.of(
                AbilityRuntimeObservation.Kind.SUMMON_SPAWNED,
                AbilityRuntimeObservation.Kind.SUMMON_CLEARED);
    }

    private static double expectedLevelScale(int effectiveLevel) {
        return 1D + effectiveLevel * .0025D;
    }

    private static CarrierOracle grounded(EntityType entityType) {
        return new CarrierOracle(entityType, EliteMindBodyLocomotion.GROUNDED);
    }

    private static CarrierOracle flying(EntityType entityType) {
        return new CarrierOracle(entityType, EliteMindBodyLocomotion.FLYING);
    }

    private record CarrierOracle(
            EntityType entityType,
            EliteMindBodyLocomotion locomotion) {
    }

    private record SummonOracle(
            int entryLevel,
            int capLevel,
            ClassMinionTheme theme,
            double baseHealth,
            List<CarrierOracle> carriers,
            boolean supportRole,
            int summonCount,
            int ownerCap,
            int uptimeTicks,
            int attackPeriodTicks,
            double damageMultiplierPerHit,
            double resourceCost,
            double lifestealFraction,
            Set<AbilityEffect> controlEffects,
            boolean requiresCorpse,
            boolean requiresPortal) {
        private SummonOracle {
            carriers = List.copyOf(carriers);
            controlEffects = Set.copyOf(controlEffects);
        }
    }
}
