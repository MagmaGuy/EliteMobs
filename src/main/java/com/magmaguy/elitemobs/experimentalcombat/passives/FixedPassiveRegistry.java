package com.magmaguy.elitemobs.experimentalcombat.passives;

import com.magmaguy.elitemobs.experimentalcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Fixed baseline passive mechanics for every form in the built-in class tree. */
public final class FixedPassiveRegistry {

    private static final PassiveProfile DEFENDER = profile(-.012, 0, -.025, -.00035, -.01, 0, 0, 0, 0, 0);
    private static final PassiveProfile PROTECTOR = profile(-.01, 0, -.015, -.00025, -.01, 0, .01, .00015, 0, 0);
    private static final PassiveProfile COMMANDER = profile(.01, .00020, .005, 0, .005, .00010, 0, 0, .01, .00010);
    private static final PassiveProfile BRUISER = profile(.025, .00035, .018, .00020, 0, 0, 0, 0, 0, 0);
    private static final PassiveProfile EXECUTIONER = profile(.035, .00040, .012, .00015, 0, 0, -.01, 0, 0, 0);
    private static final PassiveProfile SIEGE_SUPPORT = profile(-.035, -.00025, 0, 0, 0, 0, 0, 0, 0, 0);
    private static final PassiveProfile JUGGERNAUT = profile(.01, .00015, -.018, -.00025, -.018, 0, 0, 0, 0, 0);
    private static final PassiveProfile MOBILE_RANGER = profile(.015, .00020, .012, .00010, .025, .00025, 0, 0, 0, 0);
    private static final PassiveProfile MARKSMAN = profile(.03, .00035, .01, .00010, -.01, 0, 0, 0, 0, 0);
    private static final PassiveProfile CONTROLLER = profile(.008, .00010, .008, .00010, .01, .00012, 0, 0, .015, .00012);
    private static final PassiveProfile HEALER = profile(-.025, 0, 0, 0, 0, 0, .045, .00050, 0, 0);
    private static final PassiveProfile BARRIER_HEALER = profile(-.015, 0, -.008, -.00010, -.005, 0, .025, .00030, .01, .00010);
    private static final PassiveProfile SUSTAIN_HEALER = profile(-.015, 0, .008, .00008, 0, 0, .035, .00040, .008, .00008);
    private static final PassiveProfile BATTLE_HEALER = profile(.012, .00015, .01, .00010, .005, .00008, .018, .00022, 0, 0);
    private static final PassiveProfile SPELLCASTER = profile(.015, .00020, .008, 0, 0, 0, 0, 0, .005, .00008);
    private static final PassiveProfile MAGE = profile(0, 0, .012, .00010, 0, 0, 0, 0, 0, 0);
    private static final PassiveProfile ELEMENTALIST = profile(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    private static final PassiveProfile PYROMANCER = profile(0, 0, .015, .00010, 0, 0, 0, 0, 0, 0);
    private static final PassiveProfile CRYOMANCER = profile(0, 0, -.015, -.00010, 0, 0, 0, 0, .02, .00015);
    private static final PassiveProfile BATTLEMAGE = profile(.01, .00015, -.018, -.00020, -.01, 0, 0, 0, 0, 0);
    private static final PassiveProfile SPELLBLADE = profile(.03, .00030, .005, 0, .015, .00015, 0, 0, 0, 0);
    private static final PassiveProfile ARCANE_KNIGHT = profile(-.01, 0, -.03, -.00025, 0, 0, 0, 0, .012, .00010);
    private static final PassiveProfile OCCULTIST = profile(.02, .00025, 0, 0, 0, 0, -.01, 0, .012, .00010);
    private static final PassiveProfile NECROMANCER = profile(.015, .00020, 0, 0, -.008, 0, .025, .00025, 0, 0);
    private static final PassiveProfile LICH = profile(.03, .00035, 0, 0, 0, 0, .03, .00030, 0, 0);
    private static final PassiveProfile PLAGUEBRINGER = profile(.025, .00030, 0, 0, -.012, 0, 0, 0, .018, .00015);
    private static final PassiveProfile SUMMONER = profile(0, 0, -.012, -.00010, 0, 0, 0, 0, .02, .00020);
    private static final PassiveProfile DEMONOLOGIST = profile(.045, .00040, .025, .00020, 0, 0, 0, 0, 0, 0);
    private static final PassiveProfile SPIRITBINDER = profile(0, 0, -.008, 0, 0, 0, .03, .00035, .015, .00015);

    private final Map<String, PassiveProfile> profiles;
    private final Map<String, List<PassiveTrait>> traits;
    private final Map<String, List<PassiveMechanicTrait>> mechanicTraits;

    private FixedPassiveRegistry(
            Map<String, PassiveProfile> profiles,
            Map<String, List<PassiveTrait>> traits,
            Map<String, List<PassiveMechanicTrait>> mechanicTraits) {
        this.profiles = Collections.unmodifiableMap(new LinkedHashMap<>(profiles));
        Map<String, List<PassiveTrait>> copy = new LinkedHashMap<>();
        traits.forEach((id, formTraits) -> copy.put(id, List.copyOf(formTraits)));
        this.traits = Collections.unmodifiableMap(copy);
        Map<String, List<PassiveMechanicTrait>> mechanicCopy = new LinkedHashMap<>();
        mechanicTraits.forEach((id, formTraits) -> mechanicCopy.put(id, List.copyOf(formTraits)));
        this.mechanicTraits = Collections.unmodifiableMap(mechanicCopy);
    }

    public static FixedPassiveRegistry create(
            ClassCatalog catalog,
            Map<String, PassiveProfile> definitions) {
        Objects.requireNonNull(catalog, "catalog");
        Map<String, PassiveProfile> profiles = new LinkedHashMap<>(
                Objects.requireNonNull(definitions, "definitions"));

        for (ClassFormDefinition form : catalog.forms()) {
            if (!profiles.containsKey(form.id()))
                throw new IllegalArgumentException("No fixed passive mechanic for " + form.id());
        }
        if (profiles.size() != catalog.forms().size())
            throw new IllegalArgumentException("Passive registry contains forms outside the catalog");
        Map<String, List<PassiveTrait>> traits = traitDefinitions(profiles.keySet());
        if (!traits.keySet().equals(profiles.keySet()))
            throw new IllegalArgumentException("Passive trait coverage does not match profile coverage");
        Map<String, List<PassiveMechanicTrait>> mechanicTraits = mechanicDefinitions(profiles.keySet());
        if (!mechanicTraits.keySet().equals(profiles.keySet()))
            throw new IllegalArgumentException("Passive mechanic trait coverage does not match profile coverage");
        validatePromisedMechanics(mechanicTraits);
        return new FixedPassiveRegistry(profiles, traits, mechanicTraits);
    }

    public static Map<String, PassiveProfile> paladinDefinitions() {
        Map<String, PassiveProfile> profiles = new LinkedHashMap<>();

        put(profiles, DEFENDER, "paladin", "guardian", "aegis", "bulwark", "shieldbearer");
        put(profiles, PROTECTOR, "justicar", "templar");
        put(profiles, COMMANDER, "warlord", "marshal", "bannerlord", "strategist");
        put(profiles, BRUISER, "conqueror", "champion");
        put(profiles, EXECUTIONER, "inquisitor", "tyrant");
        return Collections.unmodifiableMap(profiles);
    }

    public static Map<String, PassiveProfile> berserkerDefinitions() {
        Map<String, PassiveProfile> profiles = new LinkedHashMap<>();
        put(profiles, BRUISER, "berserker", "bloodrager", "bloodstorm", "reaver", "warmonger");
        put(profiles, EXECUTIONER, "slayer", "headsman", "harvester", "titanbane");
        put(profiles, SIEGE_SUPPORT, "siegebreaker");
        put(profiles, JUGGERNAUT, "deathless", "juggernaut", "crusher", "dreadnought", "colossus");
        return Collections.unmodifiableMap(profiles);
    }

    public static Map<String, PassiveProfile> rangerDefinitions() {
        Map<String, PassiveProfile> profiles = new LinkedHashMap<>();
        put(profiles, MOBILE_RANGER, "ranger", "skirmisher", "windrunner", "pathfinder", "tempest_archer");
        put(profiles, MARKSMAN, "sniper", "bowmaster", "deadeye", "raincaller", "arbalist", "artillerist", "dragonslayer");
        put(profiles, CONTROLLER, "saboteur", "trapper", "demolitionist");
        return Collections.unmodifiableMap(profiles);
    }

    public static Map<String, PassiveProfile> clericDefinitions() {
        Map<String, PassiveProfile> profiles = new LinkedHashMap<>();
        put(profiles, HEALER, "cleric", "priest", "hierophant", "saint");
        put(profiles, BATTLE_HEALER, "exorcist", "mistweaver");
        put(profiles, BARRIER_HEALER, "oracle", "fateweaver", "seraph", "spiritcaller", "soulwarden");
        put(profiles, SUSTAIN_HEALER, "shaman", "lifewarden", "grovekeeper", "shepherd");
        return Collections.unmodifiableMap(profiles);
    }

    public static Map<String, PassiveProfile> spellcasterDefinitions() {
        Map<String, PassiveProfile> profiles = new LinkedHashMap<>();
        put(profiles, SPELLCASTER, "spellcaster");
        put(profiles, MAGE, "mage");
        put(profiles, ELEMENTALIST, "elementalist");
        put(profiles, PYROMANCER, "pyromancer");
        put(profiles, CRYOMANCER, "cryomancer");
        put(profiles, BATTLEMAGE, "battlemage");
        put(profiles, SPELLBLADE, "spellblade");
        put(profiles, ARCANE_KNIGHT, "arcane_knight");
        put(profiles, OCCULTIST, "occultist");
        put(profiles, NECROMANCER, "necromancer");
        put(profiles, LICH, "lich");
        put(profiles, PLAGUEBRINGER, "plaguebringer");
        put(profiles, SUMMONER, "summoner");
        put(profiles, DEMONOLOGIST, "demonologist");
        put(profiles, SPIRITBINDER, "spiritbinder");
        return Collections.unmodifiableMap(profiles);
    }

    public PassiveProfile require(String formId) {
        PassiveProfile profile = profiles.get(formId);
        if (profile == null) throw new IllegalArgumentException("No fixed passive mechanic for " + formId);
        return profile;
    }

    public List<PassiveTrait> traits(String formId) {
        List<PassiveTrait> formTraits = traits.get(formId);
        if (formTraits == null) throw new IllegalArgumentException("No passive trait mapping for " + formId);
        return formTraits;
    }

    public Set<String> mappedFormIds() {
        return traits.keySet();
    }

    public List<PassiveMechanicTrait> mechanicTraits(String formId) {
        List<PassiveMechanicTrait> formTraits = mechanicTraits.get(formId);
        if (formTraits == null) throw new IllegalArgumentException("No passive mechanic trait mapping for " + formId);
        return formTraits;
    }

    private static Map<String, List<PassiveMechanicTrait>> mechanicDefinitions(Set<String> formIds) {
        Map<String, List<PassiveMechanicTrait>> definitions = new LinkedHashMap<>();
        formIds.forEach(id -> definitions.put(id, List.of()));

        addMechanics(definitions, "guardian", mechanics(
                new PassiveMechanics(1D, 1D, 1D, 1D, 1D, .90D, 1D, 1D, 0D, 0D)));
        addMechanics(definitions, "aegis", mechanics(
                new PassiveMechanics(1D, 1D, 1D, 1D, 1.15D, .88D, 1D, 1D, 0D, 0D)));
        addMechanics(definitions, "juggernaut", mechanics(
                new PassiveMechanics(1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D, .20D, 0D)));
        addMechanics(definitions, "dreadnought", mechanics(
                new PassiveMechanics(1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D, .15D, 0D)));
        addMechanics(definitions, "pathfinder", mechanics(
                new PassiveMechanics(1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D, 0D, .04D)));
        addMechanics(definitions, "trapper", mechanics(
                new PassiveMechanics(1D, 1D, 1D, 1D, 1D, 1D, 1.20D, 1.12D, 0D, 0D)));
        addMechanics(definitions, "lifewarden", mechanics(
                new PassiveMechanics(.90D, 1D, 1D, 1.18D, 1D, 1D, 1D, 1D, 0D, 0D)));
        addMechanics(definitions, "bloodstorm", mechanics(
                new PassiveMechanics(1D, 1D, 1.12D, 1D, 1D, 1D, 1D, 1D, 0D, 0D)));
        addMechanics(definitions, "strategist",
                mechanics(PassiveCondition.GROUPED,
                        new PassiveMechanics(1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D,
                                .97D, 0D, 0D)),
                mechanics(PassiveCondition.SOLO,
                        new PassiveMechanics(1D, 1D, 1D, 1D, 1D, 1D, 1D, 1D,
                                1.03D, 0D, 0D)));
        addMechanics(definitions, "oracle", mechanics(
                new PassiveMechanics(1D, 1D, 1D, 1D, 1.10D, 1D, 1D, 1D, 0D, 0D)));
        return Collections.unmodifiableMap(definitions);
    }

    private static void validatePromisedMechanics(Map<String, List<PassiveMechanicTrait>> definitions) {
        requireMechanic(definitions, "guardian", mechanics -> mechanics.redirectedDamageMultiplier() < 1D,
                "redirected damage reduction");
        requireMechanic(definitions, "aegis", mechanics -> mechanics.shieldStrengthMultiplier() > 1D
                        && mechanics.redirectedDamageMultiplier() < 1D,
                "shield and link strength");
        requireMechanic(definitions, "juggernaut", mechanics -> mechanics.controlResistanceFraction() > 0D,
                "control resistance");
        requireMechanic(definitions, "dreadnought", mechanics -> mechanics.controlResistanceFraction() > 0D,
                "control resistance");
        requireMechanic(definitions, "pathfinder", mechanics -> mechanics.partyMovementSpeedAdjustment() > 0D,
                "party movement");
        requireMechanic(definitions, "trapper", mechanics -> mechanics.controlDurationMultiplier() > 1D
                        && mechanics.controlPotencyMultiplier() > 1D,
                "control duration and potency");
        requireMechanic(definitions, "lifewarden", mechanics -> mechanics.periodicDurationMultiplier() > 1D
                        && mechanics.burstHealingMultiplier() < 1D,
                "periodic duration and burst tradeoff");
        requireMechanic(definitions, "bloodstorm", mechanics -> mechanics.groupedEnemyHealingMultiplier() > 1D,
                "healing against enemy groups");
        requireMechanic(definitions, "strategist", mechanics -> mechanics.abilityCostMultiplier() != 1D,
                "grouped and solo ability cost");
        requireMechanic(definitions, "oracle", mechanics -> mechanics.shieldStrengthMultiplier() > 1D,
                "barrier strength");
    }

    private static void requireMechanic(
            Map<String, List<PassiveMechanicTrait>> definitions,
            String formId,
            java.util.function.Predicate<PassiveMechanics> requirement,
            String promise) {
        boolean implemented = definitions.getOrDefault(formId, List.of()).stream()
                .map(PassiveMechanicTrait::authoredAtLevelThirty)
                .anyMatch(requirement);
        if (!implemented)
            throw new IllegalArgumentException(formId + " promises " + promise + " without a typed mechanic");
    }

    private static PassiveMechanicTrait mechanics(PassiveMechanics mechanics) {
        return new PassiveMechanicTrait(Set.of(PassiveCondition.ALWAYS), mechanics);
    }

    private static PassiveMechanicTrait mechanics(
            PassiveCondition condition,
            PassiveMechanics mechanics) {
        return new PassiveMechanicTrait(Set.of(condition), mechanics);
    }

    private static void addMechanics(
            Map<String, List<PassiveMechanicTrait>> definitions,
            String formId,
            PassiveMechanicTrait... additions) {
        if (!definitions.containsKey(formId)) return;
        definitions.put(formId, List.of(additions));
    }

    private static Map<String, List<PassiveTrait>> traitDefinitions(Set<String> formIds) {
        Map<String, List<PassiveTrait>> definitions = new LinkedHashMap<>();
        formIds.forEach(id -> definitions.put(id, List.of()));

        // Paladin: defensive branches trade damage for durability; control branches earn damage
        // only while their target is actually controlled.
        add(definitions, "bulwark", trait(PassiveCondition.STANDING, 0, -.12, 0, 0, 0, 0));
        add(definitions, "shieldbearer", trait(PassiveCondition.ALWAYS, -.035, 0, 0, 0, 0, 0));
        add(definitions, "justicar", trait(PassiveCondition.ALWAYS, 0, 0, 0, 0, 0, -.12));
        add(definitions, "templar", trait(PassiveCondition.ALWAYS, -.025, 0, 0, .02, 0, 0));
        add(definitions, "inquisitor",
                trait(PassiveCondition.TARGET_ISOLATED, .10, 0, 0, 0, 0, 0),
                trait(PassiveCondition.ALWAYS, 0, 0, 0, 0, 0, -.15));
        add(definitions, "warlord", trait(PassiveCondition.GROUPED, .04, .02, 0, 0, 0, 0));
        add(definitions, "marshal",
                trait(PassiveCondition.GROUPED, .06, -.05, 0, 0, 0, 0),
                trait(PassiveCondition.SOLO, -.04, .04, 0, 0, 0, 0));
        add(definitions, "bannerlord", trait(PassiveCondition.ALWAYS, -.035, 0, 0, 0, 0, 0));
        add(definitions, "conqueror", trait(PassiveCondition.TARGET_CONTROLLED, .10, 0, 0, 0, 0, 0));
        add(definitions, "tyrant",
                trait(PassiveCondition.TARGET_CONTROLLED, .16, 0, 0, 0, 0, 0),
                trait(PassiveCondition.TARGET_UNCONTROLLED, -.08, 0, 0, 0, 0, 0));

        // Berserker: health, wounded targets, enemy density and commitment drive the tree.
        add(definitions, "bloodrager",
                trait(PassiveCondition.HEALTH_BELOW_75, .04, .03, 0, 0, 0, 0),
                trait(PassiveCondition.HEALTH_BELOW_50, .06, .04, 0, 0, 0, 0),
                trait(PassiveCondition.HEALTH_BELOW_25, .10, .06, 0, 0, 0, 0));
        add(definitions, "reaver",
                trait(PassiveCondition.HEALTH_BELOW_50, .04, .02, 0, 0, 0, 0),
                trait(PassiveCondition.ALWAYS, 0, 0, 0, .08, 0, -.10));
        add(definitions, "bloodstorm",
                trait(EnumSet.of(PassiveCondition.TARGET_BOSS, PassiveCondition.TARGET_ISOLATED),
                        -.06, 0, 0, 0, 0, 0),
                trait(PassiveCondition.ALWAYS, 0, 0, 0, .06, 0, 0));
        add(definitions, "deathless", trait(PassiveCondition.HEALTH_BELOW_25, .04, -.16, 0, 0, 0, 0));
        add(definitions, "slayer", trait(PassiveCondition.TARGET_WOUNDED, .12, 0, 0, 0, 0, 0));
        add(definitions, "headsman",
                trait(EnumSet.of(PassiveCondition.TARGET_WOUNDED, PassiveCondition.TARGET_BOSS),
                        .18, 0, 0, 0, 0, 0),
                trait(PassiveCondition.TARGET_GROUPED, -.12, 0, 0, 0, 0, 0));
        add(definitions, "harvester",
                trait(PassiveCondition.TARGET_WOUNDED, .10, 0, 0, 0, 0, 0),
                trait(PassiveCondition.TARGET_GROUPED, .08, 0, 0, 0, 0, 0),
                trait(EnumSet.of(PassiveCondition.TARGET_GROUPED, PassiveCondition.RECENT_ELITE_KILL),
                        .10, 0, 0, 0, 0, 0),
                trait(EnumSet.of(PassiveCondition.TARGET_HEALTHY, PassiveCondition.TARGET_BOSS),
                        -.12, 0, 0, 0, 0, 0));
        add(definitions, "titanbane",
                trait(PassiveCondition.TARGET_BOSS, .16, 0, 0, 0, 0, 0),
                trait(PassiveCondition.TARGET_ORDINARY, -.06, 0, 0, 0, 0, 0),
                trait(PassiveCondition.TARGET_GROUPED, -.12, 0, 0, 0, 0, 0));
        add(definitions, "crusher", trait(PassiveCondition.CLOSE_RANGE, .10, -.10, 0, 0, 0, 0));
        add(definitions, "dreadnought", trait(PassiveCondition.CRITICAL_HIT, -.10, 0, 0, 0, 0, 0));
        add(definitions, "warmonger",
                trait(PassiveCondition.TARGET_GROUPED, .12, 0, 0, 0, 0, 0),
                trait(PassiveCondition.TARGET_ISOLATED, -.08, 0, 0, 0, 0, 0));
        add(definitions, "colossus",
                trait(PassiveCondition.STANDING, .12, -.12, 0, 0, 0, 0),
                trait(PassiveCondition.MOVING, -.08, .05, 0, 0, 0, 0));

        // Ranger: distance, motion, recent hits and target type are real runtime conditions.
        add(definitions, "ranger",
                trait(PassiveCondition.NOT_RECENTLY_HIT, 0, 0, .05, 0, 0, 0),
                trait(PassiveCondition.RECENTLY_HIT, 0, .03, -.08, 0, 0, 0));
        add(definitions, "sniper",
                trait(PassiveCondition.LONG_RANGE, .10, 0, 0, 0, 0, 0),
                trait(PassiveCondition.ALWAYS, 0, 0, -.025, 0, 0, 0));
        add(definitions, "bowmaster",
                trait(PassiveCondition.LONG_RANGE, .13, 0, 0, 0, 0, 0),
                trait(PassiveCondition.CLOSE_RANGE, -.09, 0, 0, 0, 0, 0));
        add(definitions, "deadeye", trait(PassiveCondition.CRITICAL_HIT, .18, 0, 0, 0, 0, 0));
        add(definitions, "raincaller",
                trait(PassiveCondition.TARGET_GROUPED, .10, 0, 0, 0, 0, 0),
                trait(PassiveCondition.TARGET_ISOLATED, -.08, 0, 0, 0, 0, 0));
        add(definitions, "artillerist", trait(PassiveCondition.ALWAYS, -.08, 0, 0, 0, 0, 0));
        add(definitions, "dragonslayer",
                trait(PassiveCondition.TARGET_BOSS, .18, 0, 0, 0, 0, 0),
                trait(PassiveCondition.TARGET_ORDINARY, -.10, 0, 0, 0, 0, 0));
        add(definitions, "skirmisher",
                trait(PassiveCondition.MOVING, .08, 0, 0, 0, 0, 0),
                trait(PassiveCondition.RECENTLY_HIT, 0, .04, -.08, 0, 0, 0));
        add(definitions, "windrunner",
                trait(PassiveCondition.MOVING, .12, 0, 0, 0, 0, 0),
                trait(PassiveCondition.STANDING, -.08, 0, 0, 0, 0, 0));
        add(definitions, "pathfinder", trait(PassiveCondition.ALWAYS, -.06, 0, 0, 0, 0, 0));
        add(definitions, "tempest_archer",
                trait(EnumSet.of(PassiveCondition.MOVING, PassiveCondition.TARGET_GROUPED), .12, 0, 0, 0, 0, 0),
                trait(PassiveCondition.RECENTLY_HIT, 0, .09, 0, 0, 0, 0));
        add(definitions, "saboteur",
                trait(PassiveCondition.TRAP_CLASS_ABILITY_DAMAGE, .14, 0, 0, 0, 0, 0),
                trait(PassiveCondition.RANGED_ATTACK, -.07, 0, 0, 0, 0, 0));
        add(definitions, "trapper", trait(PassiveCondition.ALWAYS, -.07, 0, 0, 0, 0, 0));
        add(definitions, "demolitionist",
                trait(PassiveCondition.BLAST_CLASS_ABILITY_DAMAGE, .16, 0, 0, 0, 0, 0),
                trait(PassiveCondition.CLOSE_RANGE, 0, .10, 0, 0, 0, 0));

        // Cleric: numerical profiles cover throughput; these conditions make party/solo and
        // survivability tradeoffs observable without pretending the passive owns ability triggers.
        add(definitions, "hierophant",
                trait(PassiveCondition.GROUPED, 0, 0, 0, .08, 0, 0),
                trait(PassiveCondition.SOLO, 0, 0, 0, -.06, 0, 0),
                trait(PassiveCondition.ALWAYS, 0, 0, 0, .04, 0, 0));
        add(definitions, "saint", trait(PassiveCondition.ALWAYS, -.06, 0, 0, .08, 0, 0));
        add(definitions, "exorcist", trait(PassiveCondition.ALWAYS, .05, 0, 0, -.025, 0, 0));
        add(definitions, "oracle", trait(PassiveCondition.ALWAYS, 0, -.04, 0, .03, 0, 0));
        add(definitions, "seraph",
                trait(PassiveCondition.GROUPED, 0, -.04, .03, 0, 0, 0),
                trait(PassiveCondition.ALWAYS, 0, 0, 0, .05, 0, 0));
        add(definitions, "shaman", trait(PassiveCondition.ALWAYS, 0, 0, 0, .06, 0, 0));
        add(definitions, "lifewarden", trait(PassiveCondition.ALWAYS, 0, 0, 0, .07, 0, 0));
        add(definitions, "grovekeeper",
                trait(PassiveCondition.STANDING, 0, -.06, 0, .04, 0, 0),
                trait(PassiveCondition.MOVING, 0, .04, 0, 0, 0, 0));
        add(definitions, "shepherd",
                trait(PassiveCondition.GROUPED, 0, 0, 0, .10, 0, 0),
                trait(PassiveCondition.SOLO, 0, 0, 0, -.08, 0, 0),
                trait(PassiveCondition.ALWAYS, 0, 0, 0, .04, 0, 0));
        add(definitions, "spiritcaller", trait(PassiveCondition.ALWAYS, 0, .06, 0, .04, 0, 0));
        add(definitions, "mistweaver", trait(PassiveCondition.ALWAYS, .03, 0, 0, .03, 0, 0));
        add(definitions, "soulwarden",
                trait(PassiveCondition.GROUPED, 0, -.08, 0, 0, 0, 0),
                trait(PassiveCondition.SOLO, 0, .04, 0, 0, 0, 0),
                trait(PassiveCondition.ALWAYS, 0, 0, 0, .035, 0, 0));

        // Spellcaster already has honest fixed profiles. Only the advertised positional clauses
        // need runtime conditions.
        add(definitions, "battlemage",
                trait(PassiveCondition.CLOSE_RANGE, .04, -.06, 0, 0, 0, 0),
                trait(PassiveCondition.LONG_RANGE, -.03, .03, 0, 0, 0, 0));
        add(definitions, "mage", trait(PassiveCondition.SPELL_DAMAGE, .04, 0, 0, 0, 0, 0));
        add(definitions, "elementalist",
                trait(PassiveCondition.AREA_CLASS_ABILITY_DAMAGE, .0385, 0, 0, 0, 0, 0),
                trait(PassiveCondition.CLOSE_RANGE, 0, .08, 0, 0, 0, 0));
        add(definitions, "pyromancer", trait(PassiveCondition.SPELL_DAMAGE, .05, 0, 0, 0, 0, 0));
        add(definitions, "cryomancer", trait(PassiveCondition.SPELL_DAMAGE, -.01, 0, 0, 0, 0, 0));
        add(definitions, "necromancer", trait(PassiveCondition.ALWAYS, 0, 0, -.02, .05, 0, 0));
        add(definitions, "lich",
                trait(PassiveCondition.ALWAYS, .04, 0, 0, .05, 0, 0),
                trait(PassiveCondition.WARD_BROKEN, 0, .08, 0, 0, 0, 0));
        add(definitions, "plaguebringer", trait(PassiveCondition.ALWAYS, .04, 0, -.02, 0, .03, 0));
        add(definitions, "demonologist", trait(PassiveCondition.ALWAYS, .08, .08, 0, 0, 0, 0));
        add(definitions, "summoner", trait(PassiveCondition.SPELL_DAMAGE, -.02, 0, 0, 0, 0, 0));
        add(definitions, "spiritbinder",
                trait(PassiveCondition.SPELL_DAMAGE, -.055, 0, 0, 0, 0, 0),
                trait(PassiveCondition.ALWAYS, 0, 0, 0, .07, .03, 0));

        return Collections.unmodifiableMap(definitions);
    }

    private static void add(Map<String, List<PassiveTrait>> definitions, String formId, PassiveTrait... additions) {
        if (!definitions.containsKey(formId)) return;
        definitions.put(formId, List.of(additions));
    }

    private static PassiveTrait trait(
            PassiveCondition condition,
            double outgoing,
            double incoming,
            double movement,
            double healingDone,
            double costReduction,
            double healingReceived) {
        return trait(EnumSet.of(condition), outgoing, incoming, movement, healingDone, costReduction, healingReceived);
    }

    private static PassiveTrait trait(
            Set<PassiveCondition> conditions,
            double outgoing,
            double incoming,
            double movement,
            double healingDone,
            double costReduction,
            double healingReceived) {
        return new PassiveTrait(conditions, new PassiveProfile(
                traitBase(outgoing), traitPerLevel(outgoing),
                traitBase(incoming), traitPerLevel(incoming),
                traitBase(movement), traitPerLevel(movement),
                traitBase(healingDone), traitPerLevel(healingDone),
                traitBase(costReduction), traitPerLevel(costReduction),
                traitBase(healingReceived), traitPerLevel(healingReceived)));
    }

    /** At contribution level 30 a conditional trait reaches the authored value, then keeps growing. */
    private static double traitBase(double authoredAtThirty) {
        return authoredAtThirty * .70D;
    }

    private static double traitPerLevel(double authoredAtThirty) {
        return authoredAtThirty * .01D;
    }

    private static void put(Map<String, PassiveProfile> profiles, PassiveProfile profile, String... ids) {
        for (String id : ids)
            if (profiles.putIfAbsent(id, profile) != null)
                throw new IllegalArgumentException("Duplicate passive mechanic for " + id);
    }

    private static PassiveProfile profile(
            double outgoingBase, double outgoingPerLevel,
            double incomingBase, double incomingPerLevel,
            double speedBase, double speedPerLevel,
            double healingBase, double healingPerLevel,
            double costReductionBase, double costReductionPerLevel) {
        return new PassiveProfile(
                outgoingBase, outgoingPerLevel,
                incomingBase, incomingPerLevel,
                speedBase, speedPerLevel,
                healingBase, healingPerLevel,
                costReductionBase, costReductionPerLevel,
                0, 0);
    }
}
