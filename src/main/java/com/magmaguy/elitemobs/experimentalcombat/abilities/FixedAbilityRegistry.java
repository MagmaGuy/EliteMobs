package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.experimentalcombat.classes.AbilityDefinition;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Fixed executable data for every catalog ability. The constructor fails closed if the code-owned
 * catalog gains, loses or renames an ability without a corresponding mechanic.
 */
public final class FixedAbilityRegistry {
    private final Map<String, FixedAbilitySpec> specs;

    private FixedAbilityRegistry(Map<String, FixedAbilitySpec> specs) {
        this.specs = Collections.unmodifiableMap(new LinkedHashMap<>(specs));
    }

    public static FixedAbilityRegistry create(
            ClassCatalog catalog,
            Map<String, FixedAbilitySpec> definitions) {
        Objects.requireNonNull(catalog, "catalog");
        Map<String, FixedAbilitySpec> specs = new LinkedHashMap<>(
                Objects.requireNonNull(definitions, "definitions"));
        validateCoverage(catalog, specs);
        FixedAbilitySemanticsValidator.validate(specs.values());
        return new FixedAbilityRegistry(specs);
    }

    public Set<String> registeredIds() {
        return specs.keySet();
    }

    public FixedAbilitySpec require(String abilityId) {
        FixedAbilitySpec spec = specs.get(abilityId);
        if (spec == null) throw new IllegalArgumentException("No fixed mechanic for " + abilityId);
        return spec;
    }

    public static Map<String, FixedAbilitySpec> adventurerDefinitions() {
        Map<String, FixedAbilitySpec> specs = new LinkedHashMap<>();
        add(specs, "adventurer.mobility", AbilitySlot.MOBILITY, AbilityFamily.SAFE_DASH, AbilityTarget.SELF,
                tune(0, 0, 0, 0, 1, 4, 0, 0, 1, 1));
        instant(specs, "adventurer.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_ENEMY,
                tune(1.1, 0, 0, 0, 1, 3, 0, 1, 1, 1), AbilityEffect.DAMAGE);
        instant(specs, "adventurer.utility", AbilitySlot.UTILITY, AbilityTarget.SELF,
                support(.12, 0, 0, 0, 1), AbilityEffect.HEAL);
        cost(specs, "adventurer.mobility", 20D);
        cost(specs, "adventurer.signature", 20D);
        cost(specs, "adventurer.utility", 30D);
        return Collections.unmodifiableMap(specs);
    }

    public static Map<String, FixedAbilitySpec> paladinDefinitions() {
        Map<String, FixedAbilitySpec> specs = new LinkedHashMap<>();
        add(specs, "paladin.mobility", AbilitySlot.MOBILITY, AbilityFamily.MOUNTED_CHARGE, AbilityTarget.FORWARD_ENEMIES,
                tune(1.0, 0, 0, 0.9, 1, 40, 1.25, 100, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.KNOCKBACK, AbilityEffect.TAUNT);
        cost(specs, "paladin.mobility", 55D);
        paladin(specs);
        mechanics(specs, "guardian.signature", AbilityMechanic.DAMAGE_REDIRECT);
        mechanics(specs, "guardian.signature", AbilityMechanic.RECENT_ATTACKERS);
        mechanics(specs, "guardian.utility", AbilityMechanic.CONTROL_IMMUNITY);
        mechanics(specs, "aegis.signature", AbilityMechanic.DAMAGE_REDIRECT);
        mechanics(specs, "bulwark.signature", AbilityMechanic.CONTROL_IMMUNITY,
                AbilityMechanic.PLANTED_GUARD, AbilityMechanic.CASTER_ONLY_SUPPORT);
        mechanics(specs, "bulwark.utility", AbilityMechanic.CONTROL_IMMUNITY);
        mechanics(specs, "shieldbearer.signature", AbilityMechanic.MULTI_ALLY_REDIRECT);
        mechanics(specs, "justicar.signature", AbilityMechanic.RETALIATION_RELEASE);
        mechanics(specs, "templar.signature", AbilityMechanic.RETALIATION_RELEASE);
        mechanics(specs, "templar.signature", AbilityMechanic.RETALIATION_HEAL);
        mechanics(specs, "justicar.utility", AbilityMechanic.RECENT_ATTACKERS);
        mechanics(specs, "templar.utility", AbilityMechanic.DEBUFF_IMMUNITY);
        mechanics(specs, "inquisitor.signature", AbilityMechanic.EXECUTE_DAMAGE);
        mechanics(specs, "bannerlord.signature", AbilityMechanic.FOLLOW_CASTER_FIELD);
        mechanics(specs, "bannerlord.utility", AbilityMechanic.REQUIRES_ACTIVE_FIELD);
        mechanics(specs, "champion.signature", AbilityMechanic.TAUNTED_TARGETS_ONLY,
                AbilityMechanic.EXTEND_TAUNT);
        cost(specs, "paladin.signature", 40D);
        cost(specs, "paladin.utility", 30D);
        cost(specs, "marshal.signature", 60D);
        cost(specs, "bannerlord.signature", 65D);
        return Collections.unmodifiableMap(specs);
    }

    public static Map<String, FixedAbilitySpec> berserkerDefinitions() {
        Map<String, FixedAbilitySpec> specs = new LinkedHashMap<>();
        add(specs, "berserker.mobility", AbilitySlot.MOBILITY, AbilityFamily.BALLISTIC_LEAP, AbilityTarget.AIMED_LOCATION,
                tune(2.2, 0, 0, 1.1, 1, 13, 4.5, 35, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.LAUNCH, AbilityEffect.INTERRUPT);
        cost(specs, "berserker.mobility", 60D);
        berserker(specs);
        mechanics(specs, "bloodrager.signature", AbilityMechanic.MISSING_HEALTH_SCALING);
        mechanics(specs, "bloodrager.utility", AbilityMechanic.RESOURCE_BURST);
        mechanics(specs, "reaver.signature", AbilityMechanic.LIFESTEAL_WINDOW);
        mechanics(specs, "reaver.utility", AbilityMechanic.WOUNDED_TARGETS_ONLY);
        mechanics(specs, "bloodstorm.signature", AbilityMechanic.FOLLOW_CASTER_FIELD);
        mechanics(specs, "bloodstorm.utility", AbilityMechanic.CASTER_ONLY_FIELD);
        mechanics(specs, "deathless.signature", AbilityMechanic.DEATH_GUARD);
        mechanics(specs, "slayer.signature", AbilityMechanic.EXECUTE_DAMAGE);
        mechanics(specs, "headsman.signature", AbilityMechanic.EXECUTE_DAMAGE);
        mechanics(specs, "headsman.signature", AbilityMechanic.WOUNDED_TARGETS_ONLY);
        mechanics(specs, "headsman.utility", AbilityMechanic.HEALTH_SCALED_MARK);
        mechanics(specs, "harvester.signature", AbilityMechanic.EXECUTE_DAMAGE, AbilityMechanic.CHAINING_CAST);
        mechanics(specs, "harvester.signature", AbilityMechanic.WOUNDED_TARGETS_ONLY);
        mechanics(specs, "harvester.utility", AbilityMechanic.WOUNDED_TARGETS_ONLY);
        mechanics(specs, "juggernaut.utility", AbilityMechanic.CONTROL_IMMUNITY);
        mechanics(specs, "dreadnought.signature", AbilityMechanic.EXPANDING_PULSES);
        mechanics(specs, "dreadnought.utility", AbilityMechanic.CONTROL_IMMUNITY);
        mechanics(specs, "siegebreaker.signature", AbilityMechanic.OPENS_DEFENSE_BREAK);
        mechanics(specs, "siegebreaker.utility", AbilityMechanic.REQUIRES_DEFENSE_BREAK);
        mechanics(specs, "titanbane.signature", AbilityMechanic.LARGE_OR_BOSS_ONLY);
        mechanics(specs, "titanbane.utility", AbilityMechanic.LARGE_OR_BOSS_ONLY);
        mechanics(specs, "colossus.signature", AbilityMechanic.WIND_UP);
        mechanics(specs, "colossus.utility", AbilityMechanic.REQUIRES_WIND_UP);
        cost(specs, "deathless.signature", 85D);
        cost(specs, "colossus.signature", 75D);
        cost(specs, "berserker.signature", 35D);
        cost(specs, "berserker.utility", 30D);
        cost(specs, "bloodrager.utility", 30D);
        cost(specs, "deathless.utility", 55D);
        cost(specs, "colossus.utility", 50D);
        return Collections.unmodifiableMap(specs);
    }

    public static Map<String, FixedAbilitySpec> rangerDefinitions() {
        Map<String, FixedAbilitySpec> specs = new LinkedHashMap<>();
        add(specs, "ranger.mobility", AbilitySlot.MOBILITY, AbilityFamily.SAFE_DASH, AbilityTarget.SELF,
                tune(0, 0, 0, 0, 1, 7, 0, 0, 1, 1));
        cost(specs, "ranger.mobility", 40D);
        ranger(specs);
        mechanics(specs, "bowmaster.signature", AbilityMechanic.PIERCING_CAST);
        mechanics(specs, "raincaller.signature", AbilityMechanic.PROJECTILE_BOMBARDMENT);
        mechanics(specs, "sniper.signature", AbilityMechanic.WIND_UP);
        mechanics(specs, "sniper.signature", AbilityMechanic.MINIMUM_RANGE);
        mechanics(specs, "deadeye.signature", AbilityMechanic.WIND_UP);
        mechanics(specs, "deadeye.signature", AbilityMechanic.GUARANTEED_CRITICAL);
        mechanics(specs, "artillerist.utility", AbilityMechanic.RESOURCE_BURST);
        mechanics(specs, "windrunner.utility", AbilityMechanic.RESOURCE_BURST);
        mechanics(specs, "tempest_archer.utility", AbilityMechanic.RESOURCE_BURST);
        mechanics(specs, "tempest_archer.signature", AbilityMechanic.CHAINING_CAST);
        mechanics(specs, "saboteur.signature", AbilityMechanic.DELAYED_PAYLOAD);
        mechanics(specs, "demolitionist.utility", AbilityMechanic.DETONATING_MARK);
        mechanics(specs, "dragonslayer.signature", AbilityMechanic.LARGE_OR_BOSS_ONLY,
                AbilityMechanic.PIERCING_CAST);
        mechanics(specs, "dragonslayer.utility", AbilityMechanic.BOSS_ONLY);
        mechanics(specs, "skirmisher.signature", AbilityMechanic.REQUIRES_MOVEMENT);
        mechanics(specs, "trapper.utility", AbilityMechanic.EXTEND_CONTROL_DURATION);
        mechanics(specs, "demolitionist.signature", AbilityMechanic.CLUSTER_PROJECTILE);
        mechanics(specs, "pathfinder.signature", AbilityMechanic.PARTY_BUFF);
        cost(specs, "deadeye.signature", 70D);
        cost(specs, "ranger.signature", 60D);
        cost(specs, "ranger.utility", 25D);
        cost(specs, "artillerist.signature", 55D);
        cost(specs, "windrunner.signature", 60D);
        cost(specs, "tempest_archer.signature", 65D);
        cost(specs, "raincaller.signature", 90D);
        return Collections.unmodifiableMap(specs);
    }

    public static Map<String, FixedAbilitySpec> clericDefinitions() {
        Map<String, FixedAbilitySpec> specs = new LinkedHashMap<>();
        add(specs, "cleric.mobility", AbilitySlot.MOBILITY, AbilityFamily.ALLY_FLIGHT, AbilityTarget.AIMED_ALLY,
                tune(0, 0, .04, 0, 1, 18, 0, 60, 1, 1), AbilityEffect.SHIELD);
        cost(specs, "cleric.mobility", 45D);
        cleric(specs);
        mechanics(specs, "priest.signature", AbilityMechanic.LOWEST_HEALTH_FIRST);
        mechanics(specs, "saint.signature", AbilityMechanic.LOWEST_HEALTH_FIRST);
        mechanics(specs, "exorcist.signature", AbilityMechanic.LOWEST_HEALTH_FIRST);
        mechanics(specs, "oracle.signature", AbilityMechanic.BARRIER_BREAK_HEAL);
        mechanics(specs, "fateweaver.signature", AbilityMechanic.DEATH_GUARD);
        mechanics(specs, "fateweaver.utility", AbilityMechanic.THREAT_TRIGGERED_PROTECTION);
        mechanics(specs, "saint.signature", AbilityMechanic.LOW_HEALTH_ALLY_ONLY);
        mechanics(specs, "seraph.signature", AbilityMechanic.LOWEST_HEALTH_FIRST, AbilityMechanic.CHAINING_CAST);
        mechanics(specs, "seraph.utility", AbilityMechanic.DAMAGE_REDIRECT);
        mechanics(specs, "shaman.signature", AbilityMechanic.SUSTAINED_TETHER);
        mechanics(specs, "lifewarden.signature", AbilityMechanic.SUSTAINED_TETHER);
        mechanics(specs, "lifewarden.utility", AbilityMechanic.THREAT_TRIGGERED_PROTECTION);
        mechanics(specs, "shepherd.signature", AbilityMechanic.FOLLOW_CASTER_FIELD);
        mechanics(specs, "shepherd.signature", AbilityMechanic.GROUP_SCALING);
        mechanics(specs, "spiritcaller.signature", AbilityMechanic.HEAL_ECHO);
        mechanics(specs, "exorcist.signature", AbilityMechanic.SINGLE_ENEMY);
        mechanics(specs, "spiritcaller.utility", AbilityMechanic.DAMAGE_SHARE);
        mechanics(specs, "soulwarden.signature", AbilityMechanic.DAMAGE_SHARE);
        mechanics(specs, "soulwarden.utility", AbilityMechanic.DAMAGE_SHARE);
        cost(specs, "saint.signature", 65D);
        cost(specs, "fateweaver.signature", 85D);
        cost(specs, "cleric.signature", 30D);
        cost(specs, "cleric.utility", 30D);
        cost(specs, "priest.signature", 60D);
        cost(specs, "saint.utility", 75D);
        cost(specs, "grovekeeper.utility", 80D);
        cost(specs, "shepherd.signature", 60D);
        cost(specs, "soulwarden.utility", 65D);
        return Collections.unmodifiableMap(specs);
    }

    public static Map<String, FixedAbilitySpec> spellcasterDefinitions() {
        Map<String, FixedAbilitySpec> specs = new LinkedHashMap<>();
        add(specs, "spellcaster.mobility", AbilitySlot.MOBILITY, AbilityFamily.SAFE_BLINK, AbilityTarget.SELF,
                tune(0, 0, 0, 0, 1, 6, 0, 0, 1, 1));
        cost(specs, "spellcaster.mobility", 50D);
        spellcaster(specs);
        mechanics(specs, "mage.signature", AbilityMechanic.PIERCING_CAST);
        mechanics(specs, "battlemage.signature", AbilityMechanic.CASTER_ONLY_SUPPORT);
        mechanics(specs, "necromancer.signature", AbilityMechanic.REQUIRES_CORPSE);
        mechanics(specs, "spiritbinder.utility", AbilityMechanic.DAMAGE_SHARE);
        mechanics(specs, "lich.utility", AbilityMechanic.DEATH_GUARD,
                AbilityMechanic.WARD_BREAK_SIGNAL);
        cost(specs, "necromancer.signature", 45D);
        cost(specs, "lich.signature", 55D);
        cost(specs, "plaguebringer.signature", 50D);
        cost(specs, "summoner.signature", 50D);
        cost(specs, "demonologist.signature", 60D);
        cost(specs, "summoner.utility", 60D);
        cost(specs, "spiritbinder.signature", 60D);
        cost(specs, "spiritbinder.utility", 60D);
        cost(specs, "lich.utility", 60D);
        return Collections.unmodifiableMap(specs);
    }

    private static void paladin(Map<String, FixedAbilitySpec> specs) {
        instant(specs, "paladin.signature", AbilitySlot.SIGNATURE, AbilityTarget.NEARBY_ENEMIES, combat(0, 0, 8, 60), AbilityEffect.TAUNT, AbilityEffect.SELF_PROTECT);
        instant(specs, "guardian.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_ALLY, support(0, .12, 16, 8, 100), AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT, AbilityEffect.TAUNT);
        instant(specs, "aegis.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_ALLY, support(0, .18, 18, 0, 120), AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
        instant(specs, "bulwark.signature", AbilitySlot.SIGNATURE, AbilityTarget.MIXED_NEARBY, support(0, 0, 0, 9, 80), AbilityEffect.TAUNT, AbilityEffect.SELF_PROTECT, AbilityEffect.INTERRUPT);
        instant(specs, "shieldbearer.signature", AbilitySlot.SIGNATURE, AbilityTarget.NEARBY_ALLIES, support(0, .11, 0, 14, 100), AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
        instant(specs, "justicar.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_ENEMY, combat(1.8, 18, 0, 40), AbilityEffect.DAMAGE, AbilityEffect.TAUNT);
        instant(specs, "templar.signature", AbilitySlot.SIGNATURE, AbilityTarget.MIXED_NEARBY, tune(1.25, .08, 0, 0, 1, 0, 8, 40, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.HEAL);
        instant(specs, "inquisitor.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_ENEMY, combat(2.8, 20, 0, 45), AbilityEffect.DAMAGE, AbilityEffect.WEAKEN);
        weaken(specs, "inquisitor.signature", .82D);
        instant(specs, "warlord.signature", AbilitySlot.SIGNATURE, AbilityTarget.MIXED_NEARBY, tune(0, 0, 0, 0, 1.12, 0, 10, 100, 1, 1), AbilityEffect.TAUNT, AbilityEffect.STRENGTH);
        zone(specs, "marshal.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_LOCATION, tune(0, 0, .03, 0, 1.12, 14, 7, 120, 6, 1), AbilityEffect.TAUNT, AbilityEffect.STRENGTH, AbilityEffect.SHIELD);
        zone(specs, "bannerlord.signature", AbilitySlot.SIGNATURE, AbilityTarget.MIXED_NEARBY, tune(0, 0, 0, 0, 1.14, 0, 8, 120, 6, 1), AbilityEffect.TAUNT, AbilityEffect.STRENGTH, AbilityEffect.SPEED);
        instant(specs, "strategist.signature", AbilitySlot.SIGNATURE, AbilityTarget.NEARBY_ALLIES, support(0, .13, 0, 10, 100), AbilityEffect.SHIELD, AbilityEffect.STRENGTH);
        instant(specs, "conqueror.signature", AbilitySlot.SIGNATURE, AbilityTarget.NEARBY_ENEMIES, tune(1.1, 0, 0, .7, 1, 0, 9, 70, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.PULL, AbilityEffect.TAUNT, AbilityEffect.WEAKEN);
        weaken(specs, "conqueror.signature", .88D);
        instant(specs, "tyrant.signature", AbilitySlot.SIGNATURE, AbilityTarget.NEARBY_ENEMIES, combat(0, 0, 11, 90), AbilityEffect.FEAR, AbilityEffect.TAUNT, AbilityEffect.WEAKEN);
        weaken(specs, "tyrant.signature", .85D);
        instant(specs, "champion.signature", AbilitySlot.SIGNATURE, AbilityTarget.FORWARD_ENEMIES, combat(1.9, 10, 5, 60), AbilityEffect.DAMAGE, AbilityEffect.TAUNT);

        instant(specs, "paladin.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES, tune(0, 0, 0, .7, 1, 0, 7, 35, 1, 1), AbilityEffect.KNOCKBACK, AbilityEffect.INTERRUPT);
        instant(specs, "guardian.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ALLIES, support(0, .08, 0, 9, 80), AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
        zone(specs, "aegis.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_LOCATION, support(0, .05, 10, 6, 100), AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
        instant(specs, "bulwark.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ALLIES, support(0, .04, 0, 9, 80), AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
        instant(specs, "shieldbearer.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ALLIES, support(0, .1, 0, 12, 100), AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT, AbilityEffect.SPEED);
        instant(specs, "justicar.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES, combat(0, 0, 9, 80), AbilityEffect.WEAKEN);
        weaken(specs, "justicar.utility", .80D);
        instant(specs, "templar.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ALLIES, support(0, 0, 0, 10, 70), AbilityEffect.CLEANSE, AbilityEffect.ALLY_PROTECT);
        mark(specs, "inquisitor.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_ENEMY, 20, 0, 120, 1.15, AbilityEffect.WEAKEN);
        weaken(specs, "inquisitor.utility", .88D);
        instant(specs, "warlord.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ALLIES, support(0, 0, 0, 11, 80), AbilityEffect.CLEANSE, AbilityEffect.SPEED);
        instant(specs, "marshal.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ALLIES, support(0, 0, 0, 12, 80), AbilityEffect.SPEED, AbilityEffect.ALLY_PROTECT);
        instant(specs, "bannerlord.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ALLIES, support(0, 0, 0, 14, 80), AbilityEffect.SPEED);
        instant(specs, "strategist.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ALLIES, support(0, .08, 0, 13, 70), AbilityEffect.SHIELD, AbilityEffect.SPEED);
        instant(specs, "conqueror.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ALLIES, tune(0, 0, 0, 0, 1.12, 0, 11, 70, 1, 1), AbilityEffect.SPEED, AbilityEffect.STRENGTH);
        instant(specs, "tyrant.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES, combat(0, 0, 12, 90), AbilityEffect.SLOW, AbilityEffect.GLOW);
        instant(specs, "champion.utility", AbilitySlot.UTILITY, AbilityTarget.FORWARD_ENEMIES, tune(0, 0, 0, .7, 1, 12, 6, 50, 1, 1), AbilityEffect.KNOCKBACK, AbilityEffect.TAUNT);
    }

    private static void berserker(Map<String, FixedAbilitySpec> specs) {
        instant(specs, "berserker.signature", AbilitySlot.SIGNATURE, AbilityTarget.SELF, tune(0, 0, 0, 0, 1.18, 0, 0, 100, 1, 1), AbilityEffect.STRENGTH, AbilityEffect.SPEED);
        instant(specs, "bloodrager.signature", AbilitySlot.SIGNATURE, AbilityTarget.SELF, tune(0, 0, 0, 0, 1.22, 0, 0, 100, 1, 1), AbilityEffect.STRENGTH, AbilityEffect.SPEED);
        instant(specs, "reaver.signature", AbilitySlot.SIGNATURE, AbilityTarget.FORWARD_ENEMIES, combat(1.35, 9, 5, 60), AbilityEffect.DAMAGE, AbilityEffect.LIFESTEAL);
        instant(specs, "bloodstorm.signature", AbilitySlot.SIGNATURE, AbilityTarget.NEARBY_ENEMIES, tune(.55, 0, 0, .7, 1, 0, 7, 60, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.LIFESTEAL, AbilityEffect.KNOCKBACK);
        instant(specs, "deathless.signature", AbilitySlot.SIGNATURE, AbilityTarget.SELF, support(.08, .2, 0, 0, 100), AbilityEffect.HEAL, AbilityEffect.SHIELD, AbilityEffect.SELF_PROTECT);
        instant(specs, "slayer.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_ENEMY, combat(2.0, 8, 0, 30), AbilityEffect.DAMAGE);
        instant(specs, "headsman.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_ENEMY, combat(3.2, 9, 0, 30), AbilityEffect.DAMAGE, AbilityEffect.WEAKEN);
        weaken(specs, "headsman.signature", .85D);
        instant(specs, "harvester.signature", AbilitySlot.SIGNATURE, AbilityTarget.NEARBY_ENEMIES, combat(1.8, 0, 7, 40), AbilityEffect.DAMAGE, AbilityEffect.LIFESTEAL);
        instant(specs, "juggernaut.signature", AbilitySlot.SIGNATURE, AbilityTarget.FORWARD_ENEMIES, tune(1.4, 0, 0, .7, 1, 8, 5, 50, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.LAUNCH, AbilityEffect.WEAKEN);
        weaken(specs, "juggernaut.signature", .88D);
        instant(specs, "crusher.signature", AbilitySlot.SIGNATURE, AbilityTarget.NEARBY_ENEMIES, tune(1.7, 0, 0, .7, 1, 0, 8, 60, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.LAUNCH, AbilityEffect.WEAKEN);
        weaken(specs, "crusher.signature", .88D);
        instant(specs, "siegebreaker.signature", AbilitySlot.SIGNATURE, AbilityTarget.FORWARD_ENEMIES, combat(2.2, 10, 5, 80), AbilityEffect.DAMAGE, AbilityEffect.WEAKEN, AbilityEffect.INTERRUPT);
        weaken(specs, "siegebreaker.signature", .85D);
        instant(specs, "titanbane.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_ENEMY, combat(2.7, 11, 0, 65), AbilityEffect.DAMAGE, AbilityEffect.INTERRUPT, AbilityEffect.SLOW);
        zone(specs, "dreadnought.signature", AbilitySlot.SIGNATURE, AbilityTarget.MIXED_NEARBY, tune(.65, 0, 0, .8, 1, 0, 8, 60, 3, 1), AbilityEffect.DAMAGE, AbilityEffect.LAUNCH);
        instant(specs, "warmonger.signature", AbilitySlot.SIGNATURE, AbilityTarget.NEARBY_ENEMIES, combat(1.3, 0, 10, 70), AbilityEffect.DAMAGE, AbilityEffect.FEAR, AbilityEffect.INTERRUPT);
        zone(specs, "colossus.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_LOCATION, tune(3.6, 0, 0, 1.3, 1, 11, 6, 50, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.LAUNCH, AbilityEffect.INTERRUPT, AbilityEffect.SELF_PROTECT);

        instant(specs, "berserker.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES, tune(0, 0, 0, .7, 1, 0, 6, 25, 1, 1), AbilityEffect.INTERRUPT, AbilityEffect.KNOCKBACK);
        instant(specs, "bloodrager.utility", AbilitySlot.UTILITY, AbilityTarget.SELF, support(0, 0, 0, 0, 50), AbilityEffect.CLEANSE, AbilityEffect.SPEED);
        instant(specs, "reaver.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES,
                tune(0, 0, 0, 0, 1, 0, 12, 100, 1, 1),
                AbilityEffect.GLOW, AbilityEffect.SPEED);
        zone(specs, "bloodstorm.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_LOCATION,
                tune(0, .05, 0, 0, 1, 8, 5, 100, 5, 1), AbilityEffect.HEAL);
        instant(specs, "deathless.utility", AbilitySlot.UTILITY, AbilityTarget.SELF, support(.05, .18, 0, 0, 90), AbilityEffect.HEAL, AbilityEffect.SHIELD, AbilityEffect.SELF_PROTECT);
        instant(specs, "slayer.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES,
                tune(0, 0, 0, 0, 1, 0, 12, 100, 1, 1),
                AbilityEffect.GLOW, AbilityEffect.SPEED);
        mark(specs, "headsman.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_ENEMY, 16, 0, 120, 1.18, AbilityEffect.WEAKEN);
        weaken(specs, "headsman.utility", .88D);
        instant(specs, "harvester.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES, tune(0, 0, 0, .7, 1, 0, 8, 45, 1, 1), AbilityEffect.PULL, AbilityEffect.SLOW);
        instant(specs, "juggernaut.utility", AbilitySlot.UTILITY, AbilityTarget.SELF, support(0, 0, 0, 0, 80), AbilityEffect.CLEANSE, AbilityEffect.SELF_PROTECT);
        instant(specs, "crusher.utility", AbilitySlot.UTILITY, AbilityTarget.FORWARD_ENEMIES, tune(0, 0, 0, .7, 1, 12, 5, 45, 1, 1), AbilityEffect.LAUNCH, AbilityEffect.INTERRUPT);
        mark(specs, "siegebreaker.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES, 0, 9, 110, 1.12, AbilityEffect.WEAKEN);
        weaken(specs, "siegebreaker.utility", .90D);
        instant(specs, "titanbane.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_ENEMY, combat(0, 16, 0, 90), AbilityEffect.ROOT, AbilityEffect.SLOW, AbilityEffect.INTERRUPT);
        instant(specs, "dreadnought.utility", AbilitySlot.UTILITY, AbilityTarget.SELF, support(0, 0, 0, 0, 100), AbilityEffect.CLEANSE, AbilityEffect.SELF_PROTECT);
        instant(specs, "warmonger.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES, tune(0, 0, 0, .7, 1, 0, 11, 45, 1, 1), AbilityEffect.TAUNT, AbilityEffect.INTERRUPT, AbilityEffect.FEAR, AbilityEffect.KNOCKBACK);
        instant(specs, "colossus.utility", AbilitySlot.UTILITY, AbilityTarget.SELF, support(0, .24, 0, 0, 80), AbilityEffect.SHIELD, AbilityEffect.SELF_PROTECT);
    }

    private static void ranger(Map<String, FixedAbilitySpec> specs) {
        projectile(specs, "ranger.signature", AbilityTarget.FORWARD_ENEMIES, tune(.72, 0, 0, 0, 1, 18, 8, 20, 1, 5), AbilityEffect.DAMAGE);
        projectile(specs, "sniper.signature", AbilityTarget.AIMED_ENEMY, tune(2.0, 0, 0, 0, 1, 32, 0, 25, 1, 1), AbilityEffect.DAMAGE);
        projectile(specs, "bowmaster.signature", AbilityTarget.FORWARD_ENEMIES, tune(1.8, 0, 0, 0, 1, 36, 3, 30, 1, 1), AbilityEffect.DAMAGE);
        projectile(specs, "deadeye.signature", AbilityTarget.AIMED_ENEMY, tune(3.4, 0, 0, 0, 1, 48, 0, 30, 1, 1), AbilityEffect.DAMAGE);
        zone(specs, "raincaller.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_LOCATION, tune(.55, 0, 0, 0, 1, 28, 7, 80, 6, 4), AbilityEffect.DAMAGE);
        projectile(specs, "arbalist.signature", AbilityTarget.AIMED_ENEMY, tune(2.2, 0, 0, 0, 1, 30, 0, 60, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.WEAKEN);
        weaken(specs, "arbalist.signature", .85D);
        projectile(specs, "artillerist.signature", AbilityTarget.AIMED_ENEMY, tune(.85, 0, 0, 0, 1, 28, 0, 30, 1, 5), AbilityEffect.DAMAGE);
        projectile(specs, "dragonslayer.signature", AbilityTarget.AIMED_ENEMY, tune(3.1, 0, 0, 0, 1, 36, 0, 45, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.WEAKEN);
        weaken(specs, "dragonslayer.signature", .82D);
        projectile(specs, "skirmisher.signature", AbilityTarget.FORWARD_ENEMIES, tune(.62, 0, 0, .4, 1, 18, 10, 25, 1, 7), AbilityEffect.DAMAGE, AbilityEffect.KNOCKBACK);
        projectile(specs, "windrunner.signature", AbilityTarget.FORWARD_ENEMIES, tune(.8, 0, 0, 0, 1, 22, 8, 25, 1, 5), AbilityEffect.DAMAGE, AbilityEffect.SPEED);
        projectile(specs, "pathfinder.signature", AbilityTarget.AIMED_ENEMY, tune(1.5, 0, 0, 0, 1, 26, 12, 60, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.SPEED);
        projectile(specs, "tempest_archer.signature", AbilityTarget.NEARBY_ENEMIES, tune(.9, 0, 0, 0, 1, 0, 12, 35, 1, 4), AbilityEffect.DAMAGE, AbilityEffect.GLOW);
        projectile(specs, "saboteur.signature", AbilityTarget.AIMED_ENEMY, tune(1.7, 0, 0, .7, 1, 26, 4, 50, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.LAUNCH);
        zone(specs, "trapper.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_LOCATION, tune(.35, 0, 0, 0, 1, 20, 7, 100, 5, 1), AbilityEffect.DAMAGE, AbilityEffect.ROOT, AbilityEffect.SLOW, AbilityEffect.GLOW);
        zone(specs, "demolitionist.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_LOCATION, tune(1.15, 0, 0, .8, 1, 24, 6, 35, 3, 1), AbilityEffect.DAMAGE, AbilityEffect.LAUNCH);

        mark(specs, "ranger.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES, 0, 14, 120, 1.10);
        mark(specs, "sniper.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_ENEMY, 38, 0, 140, 1.16);
        mark(specs, "bowmaster.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_ENEMY, 48, 0, 120, 1.10);
        mark(specs, "deadeye.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_ENEMY, 48, 0, 160, 1.18);
        zone(specs, "raincaller.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_LOCATION, tune(0, 0, 0, 0, 1.10, 28, 7, 120, 6, 1), AbilityEffect.GLOW, AbilityEffect.PARTY_DAMAGE_MARK);
        mark(specs, "arbalist.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_ENEMY, 34, 0, 180, 1.10, AbilityEffect.SLOW);
        instant(specs, "artillerist.utility", AbilitySlot.UTILITY, AbilityTarget.SELF, support(0, 0, 0, 0, 100), AbilityEffect.SPEED, AbilityEffect.STRENGTH);
        mark(specs, "dragonslayer.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_ENEMY, 42, 0, 180, 1.18, AbilityEffect.WEAKEN);
        weaken(specs, "dragonslayer.utility", .88D);
        mark(specs, "skirmisher.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES, 0, 12, 100, 1.10, AbilityEffect.SPEED);
        instant(specs, "windrunner.utility", AbilitySlot.UTILITY, AbilityTarget.SELF, support(0, 0, 0, 0, 100), AbilityEffect.SPEED);
        zone(specs, "pathfinder.utility", AbilitySlot.UTILITY, AbilityTarget.MIXED_NEARBY, tune(0, 0, 0, 0, 1, 0, 7, 100, 5, 1), AbilityEffect.SPEED);
        mark(specs, "tempest_archer.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES, 0, 13, 100, 1.10, AbilityEffect.SPEED);
        zone(specs, "saboteur.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_LOCATION, tune(0, 0, 0, 0, 1, 20, 6, 120, 6, 1), AbilityEffect.SLOW, AbilityEffect.GLOW);
        instant(specs, "trapper.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES, combat(0, 0, 10, 110), AbilityEffect.SLOW, AbilityEffect.GLOW);
        mark(specs, "demolitionist.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_ENEMY, 28, 0, 100, 1.14, AbilityEffect.WEAKEN);
        weaken(specs, "demolitionist.utility", .88D);
    }

    private static void cleric(Map<String, FixedAbilitySpec> specs) {
        instant(specs, "cleric.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_ALLY,
                support(.18, 0, 20, 0, 20), AbilityExecutionTraits.reducedSelfHealing(.6D), AbilityEffect.HEAL);
        instant(specs, "priest.signature", AbilitySlot.SIGNATURE, AbilityTarget.NEARBY_ALLIES,
                tune(0, .14, 0, 0, 1, 0, 12, 20, 1, 3), AbilityEffect.HEAL);
        instant(specs, "hierophant.signature", AbilitySlot.SIGNATURE, AbilityTarget.NEARBY_ALLIES, support(.2, 0, 0, 14, 25), AbilityEffect.HEAL);
        instant(specs, "saint.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_ALLY, support(.38, .08, 24, 0, 60), AbilityEffect.HEAL, AbilityEffect.SHIELD);
        instant(specs, "exorcist.signature", AbilitySlot.SIGNATURE, AbilityTarget.MIXED_NEARBY, tune(1.4, .11, 0, 0, 1, 18, 8, 30, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.HEAL);
        instant(specs, "oracle.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_ALLY, support(.08, .2, 22, 0, 120), AbilityEffect.HEAL, AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
        instant(specs, "fateweaver.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_ALLY, support(.24, .28, 24, 0, 140), AbilityEffect.HEAL, AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
        instant(specs, "seraph.signature", AbilitySlot.SIGNATURE, AbilityTarget.NEARBY_ALLIES, support(.17, .03, 0, 18, 30), AbilityEffect.HEAL, AbilityEffect.SHIELD);
        instant(specs, "shaman.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_ALLY, support(.15, 0, 20, 0, 80), AbilityEffect.HEAL);
        instant(specs, "lifewarden.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_ALLY, support(.14, .08, 22, 0, 100), AbilityEffect.HEAL, AbilityEffect.SHIELD);
        zone(specs, "grovekeeper.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_LOCATION, tune(0, .055, .015, 0, 1, 18, 8, 120, 6, 1), AbilityEffect.HEAL, AbilityEffect.SHIELD);
        zone(specs, "shepherd.signature", AbilitySlot.SIGNATURE, AbilityTarget.MIXED_NEARBY, tune(0, .045, .01, 0, 1, 0, 12, 120, 6, 1), AbilityEffect.HEAL, AbilityEffect.SHIELD);
        instant(specs, "spiritcaller.signature", AbilitySlot.SIGNATURE, AbilityTarget.SELF,
                tune(0, 0, 0, 0, 1, 0, 0, 45, 1, 1), AbilityEffect.HEAL);
        instant(specs, "mistweaver.signature", AbilitySlot.SIGNATURE, AbilityTarget.MIXED_NEARBY, tune(.8, .12, 0, 0, 1, 0, 11, 40, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.HEAL);
        instant(specs, "soulwarden.signature", AbilitySlot.SIGNATURE, AbilityTarget.NEARBY_ALLIES, support(.08, .16, 0, 14, 120), AbilityEffect.HEAL, AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);

        zone(specs, "cleric.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_LOCATION,
                tune(0, .04, 0, 0, 1, 14, 6, 120, 6, 1), AbilityExecutionTraits.mobilityAnchor(), AbilityEffect.HEAL);
        instant(specs, "priest.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_ALLY, support(.03, 0, 22, 0, 30), AbilityEffect.CLEANSE, AbilityEffect.HEAL);
        instant(specs, "hierophant.utility", AbilitySlot.UTILITY, AbilityTarget.MIXED_NEARBY, support(0, 0, 0, 11, 35), AbilityEffect.CLEANSE, AbilityEffect.INTERRUPT);
        zone(specs, "saint.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_LOCATION, tune(0, .045, .03, 0, 1, 18, 7, 120, 6, 1), AbilityEffect.HEAL, AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
        instant(specs, "exorcist.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES, tune(0, 0, 0, .7, 1, 0, 8, 70, 1, 1), AbilityEffect.KNOCKBACK, AbilityEffect.WEAKEN);
        weaken(specs, "exorcist.utility", .82D);
        instant(specs, "oracle.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_ALLY, support(0, .14, 24, 0, 80), AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
        instant(specs, "fateweaver.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_ALLY, support(0, .18, 24, 0, 120), AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
        instant(specs, "seraph.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_ALLY, support(0, .12, 24, 0, 80), AbilityEffect.SHIELD, AbilityEffect.SPEED);
        zone(specs, "shaman.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_LOCATION, tune(0, .04, 0, 0, 1, 16, 6, 120, 6, 1), AbilityEffect.HEAL);
        instant(specs, "lifewarden.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_ALLY, support(0, .2, 22, 0, 120), AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
        zone(specs, "grovekeeper.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_LOCATION, tune(.35, 0, .025, 0, 1, 18, 7, 120, 6, 1), AbilityEffect.DAMAGE, AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
        instant(specs, "shepherd.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ALLIES, support(0, 0, 0, 14, 80), AbilityEffect.SPEED, AbilityEffect.ALLY_PROTECT);
        instant(specs, "spiritcaller.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ALLIES, support(.04, .12, 0, 12, 100), AbilityEffect.HEAL, AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
        instant(specs, "mistweaver.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ALLIES, support(0, 0, 0, 12, 70), AbilityEffect.CLEANSE, AbilityEffect.SPEED, AbilityEffect.ALLY_PROTECT);
        instant(specs, "soulwarden.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ALLIES, support(.07, .14, 0, 15, 120), AbilityEffect.HEAL, AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
    }

    private static void spellcaster(Map<String, FixedAbilitySpec> specs) {
        projectile(specs, "spellcaster.signature", AbilityTarget.AIMED_ENEMY,
                tune(1.2, 0, 0, 0, 1, 22, 0, 20, 1, 1), AbilityEffect.DAMAGE);
        projectile(specs, "mage.signature", AbilityTarget.FORWARD_ENEMIES,
                tune(.65, 0, 0, 0, 1, 24, 7, 25, 1, 3), AbilityEffect.DAMAGE);
        zone(specs, "elementalist.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_LOCATION,
                tune(.7, 0, 0, .35, 1, 24, 6, 60, 3, 1), AbilityEffect.DAMAGE, AbilityEffect.SLOW, AbilityEffect.LAUNCH);
        zone(specs, "pyromancer.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_LOCATION,
                tune(.45, 0, 0, 0, 1, 26, 7, 100, 6, 1), AbilityEffect.DAMAGE, AbilityEffect.BURN);
        zone(specs, "cryomancer.signature", AbilitySlot.SIGNATURE, AbilityTarget.AIMED_LOCATION,
                tune(.35, 0, 0, 0, 1, 24, 8, 120, 6, 1), AbilityEffect.DAMAGE, AbilityEffect.SLOW);
        instant(specs, "battlemage.signature", AbilitySlot.SIGNATURE, AbilityTarget.MIXED_NEARBY,
                tune(1.45, 0, .05, 0, 1, 0, 6, 50, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.SHIELD, AbilityEffect.SELF_PROTECT);
        instant(specs, "spellblade.signature", AbilitySlot.SIGNATURE, AbilityTarget.FORWARD_ENEMIES,
                tune(2.2, 0, 0, 0, 1, 11, 5, 35, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.INTERRUPT);
        instant(specs, "arcane_knight.signature", AbilitySlot.SIGNATURE, AbilityTarget.NEARBY_ENEMIES,
                combat(1.35, 0, 8, 70), AbilityEffect.DAMAGE, AbilityEffect.INTERRUPT, AbilityEffect.WEAKEN);
        weaken(specs, "arcane_knight.signature", .85D);
        projectile(specs, "occultist.signature", AbilityTarget.AIMED_ENEMY,
                tune(1.65, 0, 0, 0, 1, 25, 0, 70, 1, 1), AbilityEffect.DAMAGE, AbilityEffect.WEAKEN);
        weaken(specs, "occultist.signature", .82D);
        summon(specs, "necromancer.signature", AbilityTarget.AIMED_LOCATION,
                tune(.28, 0, 0, 0, 1, 24, 0, 800, 10, 1), AbilityEffect.DAMAGE, AbilityEffect.LIFESTEAL);
        summon(specs, "lich.signature", AbilityTarget.AIMED_LOCATION,
                tune(.38, 0, 0, 0, 1, 24, 0, 800, 10, 1), AbilityEffect.DAMAGE, AbilityEffect.LIFESTEAL, AbilityEffect.WEAKEN);
        weaken(specs, "lich.signature", .92D);
        summon(specs, "plaguebringer.signature", AbilityTarget.AIMED_LOCATION,
                tune(.25, 0, 0, 0, 1, 24, 0, 800, 12, 1), AbilityEffect.DAMAGE, AbilityEffect.WEAKEN, AbilityEffect.SLOW);
        weaken(specs, "plaguebringer.signature", .92D);
        summon(specs, "summoner.signature", AbilityTarget.AIMED_LOCATION,
                tune(.32, 0, 0, 0, 1, 22, 6, 600, 9, 1), AbilityEffect.DAMAGE, AbilityEffect.INTERRUPT);
        summon(specs, "demonologist.signature", AbilityTarget.AIMED_LOCATION,
                tune(.42, 0, 0, 0, 1, 25, 8, 500, 8, 1), AbilityEffect.DAMAGE, AbilityEffect.FEAR);
        summon(specs, "spiritbinder.signature", AbilityTarget.AIMED_LOCATION,
                tune(0, .015, .01, 0, 1, 22, 7, 600, 8, 1), AbilityEffect.HEAL, AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);

        // Mana Ward buffs for a stretch of combat, not one or two hits: long duration, near-full
        // Mana cost.
        instant(specs, "spellcaster.utility", AbilitySlot.UTILITY, AbilityTarget.SELF,
                support(0, .12, 0, 0, 200), AbilityEffect.SHIELD, AbilityEffect.SELF_PROTECT);
        cost(specs, "spellcaster.utility", 100D);
        instant(specs, "mage.utility", AbilitySlot.UTILITY, AbilityTarget.SELF,
                tune(0, 0, 0, 0, 1.14, 0, 0, 80, 1, 1), AbilityEffect.STRENGTH);
        mark(specs, "elementalist.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES,
                0, 12, 100, 1.10, AbilityEffect.WEAKEN);
        weaken(specs, "elementalist.utility", .90D);
        mark(specs, "pyromancer.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES,
                0, 11, 100, 1.15, AbilityEffect.WEAKEN);
        weaken(specs, "pyromancer.utility", .88D);
        instant(specs, "cryomancer.utility", AbilitySlot.UTILITY, AbilityTarget.SELF,
                support(0, .2, 0, 0, 100), AbilityEffect.CLEANSE, AbilityEffect.SHIELD, AbilityEffect.SELF_PROTECT);
        instant(specs, "battlemage.utility", AbilitySlot.UTILITY, AbilityTarget.SELF,
                support(0, .16, 0, 0, 100), AbilityEffect.SHIELD, AbilityEffect.SELF_PROTECT);
        instant(specs, "spellblade.utility", AbilitySlot.UTILITY, AbilityTarget.SELF,
                tune(0, 0, .08, 0, 1.16, 0, 0, 80, 1, 1), AbilityEffect.SHIELD, AbilityEffect.SPEED, AbilityEffect.STRENGTH);
        instant(specs, "arcane_knight.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ALLIES,
                support(0, .14, 0, 10, 100), AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
        mark(specs, "occultist.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES,
                0, 13, 110, 1.10, AbilityEffect.WEAKEN);
        weaken(specs, "occultist.utility", .88D);
        instant(specs, "necromancer.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES,
                combat(0, 0, 10, 100), AbilityEffect.SLOW, AbilityEffect.WEAKEN);
        weaken(specs, "necromancer.utility", .85D);
        instant(specs, "lich.utility", AbilitySlot.UTILITY, AbilityTarget.SELF,
                support(.04, .22, 0, 0, 100), AbilityEffect.HEAL, AbilityEffect.SHIELD, AbilityEffect.SELF_PROTECT);
        instant(specs, "plaguebringer.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ENEMIES,
                combat(0, 0, 12, 120), AbilityEffect.SLOW, AbilityEffect.WEAKEN);
        weaken(specs, "plaguebringer.utility", .82D);
        zone(specs, "summoner.utility", AbilitySlot.UTILITY, AbilityTarget.AIMED_LOCATION,
                tune(0, 0, .04, 0, 1.10, 20, 7, 120, 6, 1), AbilityEffect.SHIELD, AbilityEffect.STRENGTH, AbilityEffect.ALLY_PROTECT);
        instant(specs, "demonologist.utility", AbilitySlot.UTILITY, AbilityTarget.SELF,
                tune(0, 0, .1, 0, 1.22, 0, 0, 90, 1, 1), AbilityEffect.SHIELD,
                AbilityEffect.SPELL_STRENGTH, AbilityEffect.SELF_VULNERABLE);
        instant(specs, "spiritbinder.utility", AbilitySlot.UTILITY, AbilityTarget.NEARBY_ALLIES,
                support(.04, .14, 0, 12, 110), AbilityEffect.HEAL, AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT);
    }

    private static void instant(Map<String, FixedAbilitySpec> specs, String id, AbilitySlot slot,
                                AbilityTarget target, AbilityTuning tuning, AbilityEffect... effects) {
        add(specs, id, slot, AbilityFamily.INSTANT, target, tuning, effects);
    }

    private static void instant(Map<String, FixedAbilitySpec> specs, String id, AbilitySlot slot,
                                AbilityTarget target, AbilityTuning tuning, AbilityExecutionTraits traits,
                                AbilityEffect... effects) {
        add(specs, id, slot, AbilityFamily.INSTANT, target, tuning, traits, effects);
    }

    private static void projectile(Map<String, FixedAbilitySpec> specs, String id, AbilityTarget target,
                                   AbilityTuning tuning, AbilityEffect... effects) {
        add(specs, id, AbilitySlot.SIGNATURE, AbilityFamily.PROJECTILE, target, tuning, effects);
    }

    private static void summon(Map<String, FixedAbilitySpec> specs, String id, AbilityTarget target,
                               AbilityTuning tuning, AbilityEffect... effects) {
        add(specs, id, AbilitySlot.SIGNATURE, AbilityFamily.SUMMON, target, tuning, effects);
    }

    private static void zone(Map<String, FixedAbilitySpec> specs, String id, AbilitySlot slot,
                             AbilityTarget target, AbilityTuning tuning, AbilityEffect... effects) {
        add(specs, id, slot, AbilityFamily.ZONE, target, tuning, effects);
    }

    private static void zone(Map<String, FixedAbilitySpec> specs, String id, AbilitySlot slot,
                             AbilityTarget target, AbilityTuning tuning, AbilityExecutionTraits traits,
                             AbilityEffect... effects) {
        add(specs, id, slot, AbilityFamily.ZONE, target, tuning, traits, effects);
    }

    private static void mark(Map<String, FixedAbilitySpec> specs, String id, AbilitySlot slot,
                             AbilityTarget target, double range, double radius, int duration, double multiplier,
                             AbilityEffect... additionalEffects) {
        EnumSet<AbilityEffect> effects = EnumSet.of(AbilityEffect.GLOW, AbilityEffect.PARTY_DAMAGE_MARK);
        Collections.addAll(effects, additionalEffects);
        add(specs, id, slot, AbilityFamily.INSTANT, target,
                tune(0, 0, 0, 0, multiplier, range, radius, duration, 1, 1),
                effects.toArray(AbilityEffect[]::new));
    }

    private static AbilityTuning combat(double damage, double range, double radius, int duration) {
        return AbilityTuning.combat(damage, range, radius, duration);
    }

    private static AbilityTuning support(double healing, double shield, double range, double radius, int duration) {
        return AbilityTuning.support(healing, shield, range, radius, duration);
    }

    private static AbilityTuning tune(double damage, double healing, double shield, double displacement,
                                      double modifier, double range, double radius, int duration,
                                      int repetitions, int projectiles) {
        return new AbilityTuning(damage, healing, shield, displacement, modifier, range, radius, duration,
                repetitions, projectiles);
    }

    private static void add(Map<String, FixedAbilitySpec> specs, String id, AbilitySlot slot,
                            AbilityFamily family, AbilityTarget target, AbilityTuning tuning,
                            AbilityEffect... effects) {
        add(specs, id, slot, family, target, tuning, AbilityExecutionTraits.STANDARD, effects);
    }

    private static void add(Map<String, FixedAbilitySpec> specs, String id, AbilitySlot slot,
                            AbilityFamily family, AbilityTarget target, AbilityTuning tuning,
                            AbilityExecutionTraits traits, AbilityEffect... effects) {
        FixedAbilitySpec previous = specs.putIfAbsent(id,
                new FixedAbilitySpec(id, slot, family, target,
                        effects.length == 0 ? Set.of() : EnumSet.of(effects[0], effects), tuning, traits,
                        defaultResourceCost(slot)));
        if (previous != null) throw new IllegalArgumentException("Duplicate ability mechanic: " + id);
    }

    private static double defaultResourceCost(AbilitySlot slot) {
        return switch (slot) {
            case MOBILITY -> 50D;
            case SIGNATURE -> 45D;
            case UTILITY -> 35D;
        };
    }

    private static void mechanics(
            Map<String, FixedAbilitySpec> specs,
            String id,
            AbilityMechanic... mechanics) {
        FixedAbilitySpec current = specs.get(id);
        if (current == null) throw new IllegalArgumentException("Unknown ability mechanic target: " + id);
        specs.put(id, current.withMechanics(mechanics));
    }

    private static void cost(Map<String, FixedAbilitySpec> specs, String id, double amount) {
        FixedAbilitySpec current = specs.get(id);
        if (current == null) throw new IllegalArgumentException("Unknown ability resource-cost target: " + id);
        specs.put(id, current.withResourceCost(amount));
    }

    private static void weaken(
            Map<String, FixedAbilitySpec> specs,
            String id,
            double multiplier) {
        FixedAbilitySpec current = specs.get(id);
        if (current == null) throw new IllegalArgumentException("Unknown weakening target: " + id);
        if (!current.effects().contains(AbilityEffect.WEAKEN))
            throw new IllegalArgumentException("Ability does not weaken: " + id);
        specs.put(id, current.withWeakenMultiplier(multiplier));
    }

    private static void validateCoverage(ClassCatalog catalog, Map<String, FixedAbilitySpec> specs) {
        Map<String, AbilitySlot> expected = new LinkedHashMap<>();
        for (ClassFormDefinition root : catalog.roots()) addExpected(expected, root.rootKit().mobility());
        for (ClassFormDefinition form : catalog.forms()) {
            addExpected(expected, form.signature());
            addExpected(expected, form.utility());
        }

        Set<String> missing = new LinkedHashSet<>(expected.keySet());
        missing.removeAll(specs.keySet());
        Set<String> unexpected = new LinkedHashSet<>(specs.keySet());
        unexpected.removeAll(expected.keySet());
        if (!missing.isEmpty() || !unexpected.isEmpty())
            throw new IllegalStateException("Fixed ability coverage mismatch. Missing=" + missing + ", unexpected=" + unexpected);
        for (Map.Entry<String, FixedAbilitySpec> entry : specs.entrySet()) {
            AbilitySlot expectedSlot = expected.get(entry.getKey());
            if (entry.getValue().slot() != expectedSlot)
                throw new IllegalStateException(entry.getKey() + " registered in " + entry.getValue().slot()
                        + " but catalog declares " + expectedSlot);
        }

        long mobilityCount = specs.values().stream().filter(spec -> spec.slot() == AbilitySlot.MOBILITY).count();
        long signatureCount = specs.values().stream().filter(spec -> spec.slot() == AbilitySlot.SIGNATURE).count();
        long utilityCount = specs.values().stream().filter(spec -> spec.slot() == AbilitySlot.UTILITY).count();
        long expectedMobilities = catalog.roots().size();
        long expectedSignatures = catalog.forms().size();
        long expectedUtilities = catalog.forms().size();
        if (mobilityCount != expectedMobilities || signatureCount != expectedSignatures
                || utilityCount != expectedUtilities)
            throw new IllegalStateException("Ability slot coverage is " + mobilityCount + "/" + signatureCount
                    + "/" + utilityCount + "; expected " + expectedMobilities + "/"
                    + expectedSignatures + "/" + expectedUtilities);
    }

    private static void addExpected(Map<String, AbilitySlot> expected, AbilityDefinition ability) {
        AbilitySlot previous = expected.putIfAbsent(ability.id(), ability.slot());
        if (previous != null) throw new IllegalStateException("Catalog repeats ability id " + ability.id());
    }
}
