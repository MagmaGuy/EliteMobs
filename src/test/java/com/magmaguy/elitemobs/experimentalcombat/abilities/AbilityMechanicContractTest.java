package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityMechanicContractTest {
    private final FixedAbilityRegistry registry = BuiltInClassContent.abilityRegistry();

    @Test
    void statefulClassPromisesAreDeclaredAsReusableMechanics() {
        Map<String, Set<AbilityMechanic>> required = Map.ofEntries(
                Map.entry("guardian.signature", Set.of(AbilityMechanic.DAMAGE_REDIRECT)),
                Map.entry("bulwark.signature", Set.of(
                        AbilityMechanic.PLANTED_GUARD, AbilityMechanic.CONTROL_IMMUNITY)),
                Map.entry("justicar.signature", Set.of(AbilityMechanic.RETALIATION_RELEASE)),
                Map.entry("bloodrager.signature", Set.of(AbilityMechanic.MISSING_HEALTH_SCALING)),
                Map.entry("deathless.signature", Set.of(AbilityMechanic.DEATH_GUARD)),
                Map.entry("dreadnought.signature", Set.of(AbilityMechanic.EXPANDING_PULSES)),
                Map.entry("mage.signature", Set.of(AbilityMechanic.PIERCING_CAST)),
                Map.entry("bowmaster.signature", Set.of(AbilityMechanic.PIERCING_CAST)),
                Map.entry("raincaller.signature", Set.of(AbilityMechanic.PROJECTILE_BOMBARDMENT)),
                Map.entry("tempest_archer.signature", Set.of(AbilityMechanic.CHAINING_CAST)),
                Map.entry("saboteur.signature", Set.of(AbilityMechanic.DELAYED_PAYLOAD)),
                Map.entry("demolitionist.utility", Set.of(AbilityMechanic.DETONATING_MARK)),
                Map.entry("oracle.signature", Set.of(AbilityMechanic.BARRIER_BREAK_HEAL)),
                Map.entry("fateweaver.signature", Set.of(AbilityMechanic.DEATH_GUARD)),
                Map.entry("lifewarden.signature", Set.of(AbilityMechanic.SUSTAINED_TETHER)),
                Map.entry("spiritcaller.signature", Set.of(AbilityMechanic.HEAL_ECHO)),
                Map.entry("soulwarden.utility", Set.of(AbilityMechanic.DAMAGE_SHARE)),
                Map.entry("lich.utility", Set.of(
                        AbilityMechanic.DEATH_GUARD, AbilityMechanic.WARD_BREAK_SIGNAL)),
                Map.entry("spiritbinder.utility", Set.of(AbilityMechanic.DAMAGE_SHARE)));

        required.forEach((abilityId, mechanics) -> assertTrue(
                registry.require(abilityId).executionTraits().mechanics().containsAll(mechanics),
                () -> abilityId + " is missing " + mechanics));
    }

    @Test
    void emergencyAbilitiesOwnTheirPremiumResourceCosts() {
        // Cooldowns no longer exist: the emergency weight of these casts lives in their cost.
        assertEquals(85D, registry.require("deathless.signature").resourceCost());
        assertEquals(85D, registry.require("fateweaver.signature").resourceCost());
        assertEquals(65D, registry.require("saint.signature").resourceCost());
        assertEquals(70D, registry.require("deadeye.signature").resourceCost());
    }

    @Test
    void playerFacingConditionsAreExecutableMechanicsRatherThanProseOnly() {
        Map<String, Set<AbilityMechanic>> required = Map.ofEntries(
                Map.entry("guardian.signature", Set.of(AbilityMechanic.RECENT_ATTACKERS)),
                Map.entry("justicar.utility", Set.of(AbilityMechanic.RECENT_ATTACKERS)),
                Map.entry("templar.signature", Set.of(AbilityMechanic.RETALIATION_HEAL)),
                Map.entry("templar.utility", Set.of(AbilityMechanic.DEBUFF_IMMUNITY)),
                Map.entry("bannerlord.utility", Set.of(AbilityMechanic.REQUIRES_ACTIVE_FIELD)),
                Map.entry("champion.signature", Set.of(
                        AbilityMechanic.TAUNTED_TARGETS_ONLY, AbilityMechanic.EXTEND_TAUNT)),
                Map.entry("reaver.utility", Set.of(AbilityMechanic.WOUNDED_TARGETS_ONLY)),
                Map.entry("bloodstorm.utility", Set.of(AbilityMechanic.CASTER_ONLY_FIELD)),
                Map.entry("headsman.utility", Set.of(AbilityMechanic.HEALTH_SCALED_MARK)),
                Map.entry("harvester.signature", Set.of(AbilityMechanic.WOUNDED_TARGETS_ONLY)),
                Map.entry("harvester.utility", Set.of(AbilityMechanic.WOUNDED_TARGETS_ONLY)),
                Map.entry("siegebreaker.signature", Set.of(AbilityMechanic.OPENS_DEFENSE_BREAK)),
                Map.entry("siegebreaker.utility", Set.of(AbilityMechanic.REQUIRES_DEFENSE_BREAK)),
                Map.entry("titanbane.signature", Set.of(AbilityMechanic.LARGE_OR_BOSS_ONLY)),
                Map.entry("titanbane.utility", Set.of(AbilityMechanic.LARGE_OR_BOSS_ONLY)),
                Map.entry("colossus.utility", Set.of(AbilityMechanic.REQUIRES_WIND_UP)),
                Map.entry("sniper.signature", Set.of(AbilityMechanic.MINIMUM_RANGE)),
                Map.entry("deadeye.signature", Set.of(AbilityMechanic.GUARANTEED_CRITICAL)),
                Map.entry("dragonslayer.signature", Set.of(
                        AbilityMechanic.LARGE_OR_BOSS_ONLY, AbilityMechanic.PIERCING_CAST)),
                Map.entry("dragonslayer.utility", Set.of(AbilityMechanic.BOSS_ONLY)),
                Map.entry("skirmisher.signature", Set.of(AbilityMechanic.REQUIRES_MOVEMENT)),
                Map.entry("trapper.utility", Set.of(AbilityMechanic.EXTEND_CONTROL_DURATION)),
                Map.entry("demolitionist.signature", Set.of(AbilityMechanic.CLUSTER_PROJECTILE)),
                Map.entry("saint.signature", Set.of(AbilityMechanic.LOW_HEALTH_ALLY_ONLY)),
                Map.entry("oracle.signature", Set.of(AbilityMechanic.BARRIER_BREAK_HEAL)),
                Map.entry("fateweaver.utility", Set.of(AbilityMechanic.THREAT_TRIGGERED_PROTECTION)),
                Map.entry("lifewarden.utility", Set.of(AbilityMechanic.THREAT_TRIGGERED_PROTECTION)),
                Map.entry("spiritcaller.signature", Set.of(AbilityMechanic.HEAL_ECHO)));

        required.forEach((abilityId, mechanics) -> assertTrue(
                registry.require(abilityId).executionTraits().mechanics().containsAll(mechanics),
                () -> abilityId + " is missing executable promise mechanics " + mechanics));
    }

    @Test
    void evolvedAndPhysicalPromisesHaveTruthfulFixedSpecs() {
        FixedAbilitySpec bowmasterMark = registry.require("bowmaster.utility");
        assertEquals(AbilityTarget.AIMED_ENEMY, bowmasterMark.target());
        assertTrue(bowmasterMark.tuning().range()
                > registry.require("sniper.utility").tuning().range());
        assertTrue(registry.require("deathless.utility").effects().contains(AbilityEffect.HEAL));
        assertTrue(registry.require("warmonger.utility").effects().containsAll(
                Set.of(AbilityEffect.FEAR, AbilityEffect.KNOCKBACK)));
        assertTrue(registry.require("trapper.signature").effects().contains(AbilityEffect.ROOT));
        assertTrue(registry.require("pyromancer.signature").effects().contains(AbilityEffect.BURN));

        FixedAbilitySpec goreTrail = registry.require("bloodstorm.utility");
        assertEquals(AbilityTarget.AIMED_LOCATION, goreTrail.target());
        assertEquals(5, goreTrail.tuning().repetitions());
        assertTrue(goreTrail.effects().contains(AbilityEffect.HEAL));
        assertTrue(goreTrail.executionTraits().mechanics()
                .contains(AbilityMechanic.CASTER_ONLY_FIELD));

        FixedAbilitySpec pathfinderBeacon = registry.require("pathfinder.utility");
        assertTrue(pathfinderBeacon.tuning().repetitions() > 1);
        assertTrue(pathfinderBeacon.effects().contains(AbilityEffect.SPEED));
        assertTrue(!pathfinderBeacon.effects().contains(AbilityEffect.SHIELD));
    }
}
