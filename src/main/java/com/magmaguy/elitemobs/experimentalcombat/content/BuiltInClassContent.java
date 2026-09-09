package com.magmaguy.elitemobs.experimentalcombat.content;

import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilityRegistry;
import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassResourceType;
import com.magmaguy.elitemobs.experimentalcombat.passives.FixedPassiveRegistry;
import com.magmaguy.elitemobs.experimentalcombat.passives.PassiveProfile;
import com.magmaguy.elitemobs.experimentalcombat.resources.ClassResourceDefinition;
import com.magmaguy.elitemobs.experimentalcombat.resources.ClassResourceDefinition.NearbyRecoveryBonus;
import com.magmaguy.elitemobs.experimentalcombat.resources.ClassResourceDefinition.DamageFreeRecoveryBonus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Immutable assembly and factory for the complete built-in class-system baseline. */
public final class BuiltInClassContent {
    private static final double BASELINE_MAX_RESOURCE = 100D;
    private static final double MANA_IN_COMBAT_PER_SECOND = BASELINE_MAX_RESOURCE / 60D;
    private static final double MANA_OUT_OF_COMBAT_PER_SECOND = MANA_IN_COMBAT_PER_SECOND;
    // Mana is the 100% baseline. Every positive passive recovery rate derives from it.
    private static final int RESOLVE_MANA_RATE_PERCENT = 66;
    private static final int FURY_MANA_RATE_PERCENT = 55;
    private static final int FOCUS_MANA_RATE_PERCENT = 85;
    private static final int GRACE_MANA_RATE_PERCENT = 200;
    private static final DamageFreeRecoveryBonus RANGER_FOCUSED_RECOVERY =
            new DamageFreeRecoveryBonus(100L, 110D / FOCUS_MANA_RATE_PERCENT);
    private static final NearbyRecoveryBonus PALADIN_NEARBY_ELITE_RECOVERY =
            new NearbyRecoveryBonus(NearbyRecoveryBonus.Target.ELITES, 15D, .20D, 5);
    private static final NearbyRecoveryBonus CLERIC_NEARBY_PLAYER_RECOVERY =
            new NearbyRecoveryBonus(NearbyRecoveryBonus.Target.OTHER_PLAYERS, 15D, .30D, 4);

    /** Increment when stable form identities or progression semantics change incompatibly. */
    public static final int PERSISTENCE_VERSION = 1;

    /** IDs are never removed from this reservation set after a released catalog retires them. */
    private static final Set<String> RETIRED_FORM_IDS = Set.of();

    private static final List<ClassTreeContribution> TREES = List.of(
            new ClassTreeContribution(
                    "adventurer", BuiltInClassDefinitions.adventurerTree(),
                    FixedAbilityRegistry.adventurerDefinitions(),
                    Map.of("adventurer", new PassiveProfile(0, 0, -.05, 0, 0, 0, 0, 0, 0, 0, 0, 0)),
                    resource(ClassResourceType.STAMINA, BASELINE_MAX_RESOURCE,
                            MANA_IN_COMBAT_PER_SECOND, MANA_OUT_OF_COMBAT_PER_SECOND,
                            0D, 0D, 0D, 0L, 0D, 0D, 0D, 0D, NearbyRecoveryBonus.NONE, DamageFreeRecoveryBonus.NONE)),
            new ClassTreeContribution(
                    "paladin",
                    BuiltInClassDefinitions.paladinTree(),
                    FixedAbilityRegistry.paladinDefinitions(),
                    FixedPassiveRegistry.paladinDefinitions(),
                    resource(ClassResourceType.RESOLVE,
                            0D, manaRelativeRecovery(MANA_IN_COMBAT_PER_SECOND, RESOLVE_MANA_RATE_PERCENT),
                            manaRelativeRecovery(MANA_OUT_OF_COMBAT_PER_SECOND, RESOLVE_MANA_RATE_PERCENT),
                            0D, 45D, 0D, 0L, 0D, 40D, 5D, 25D, PALADIN_NEARBY_ELITE_RECOVERY, DamageFreeRecoveryBonus.NONE)),
            new ClassTreeContribution(
                    "berserker",
                    BuiltInClassDefinitions.berserkerTree(),
                    FixedAbilityRegistry.berserkerDefinitions(),
                    FixedPassiveRegistry.berserkerDefinitions(),
                    resource(ClassResourceType.FURY,
                            0D, manaRelativeRecovery(MANA_IN_COMBAT_PER_SECOND, FURY_MANA_RATE_PERCENT),
                            manaRelativeRecovery(MANA_OUT_OF_COMBAT_PER_SECOND, FURY_MANA_RATE_PERCENT),
                            10D, 40D,
                            0D, 0L, 0D, 0D, 0D, 0D, NearbyRecoveryBonus.NONE, DamageFreeRecoveryBonus.NONE)),
            new ClassTreeContribution(
                    "ranger",
                    BuiltInClassDefinitions.rangerTree(),
                    FixedAbilityRegistry.rangerDefinitions(),
                    FixedPassiveRegistry.rangerDefinitions(),
                    resource(ClassResourceType.FOCUS,
                            BASELINE_MAX_RESOURCE,
                            manaRelativeRecovery(MANA_IN_COMBAT_PER_SECOND, FOCUS_MANA_RATE_PERCENT),
                            manaRelativeRecovery(MANA_OUT_OF_COMBAT_PER_SECOND, FOCUS_MANA_RATE_PERCENT),
                            0D, 0D, 0D,
                            0L, 0D, 0D, 0D, 0D, NearbyRecoveryBonus.NONE, RANGER_FOCUSED_RECOVERY)),
            new ClassTreeContribution(
                    "cleric",
                    BuiltInClassDefinitions.clericTree(),
                    FixedAbilityRegistry.clericDefinitions(),
                    FixedPassiveRegistry.clericDefinitions(),
                    resource(ClassResourceType.GRACE,
                            BASELINE_MAX_RESOURCE,
                            manaRelativeRecovery(MANA_IN_COMBAT_PER_SECOND, GRACE_MANA_RATE_PERCENT),
                            manaRelativeRecovery(MANA_OUT_OF_COMBAT_PER_SECOND, GRACE_MANA_RATE_PERCENT),
                            0D, 0D, 0D, 0L, 30D, 0D, 0D, 0D, CLERIC_NEARBY_PLAYER_RECOVERY, DamageFreeRecoveryBonus.NONE)),
            new ClassTreeContribution(
                    "spellcaster",
                    BuiltInClassDefinitions.spellcasterTree(),
                    FixedAbilityRegistry.spellcasterDefinitions(),
                    FixedPassiveRegistry.spellcasterDefinitions(),
                    resource(ClassResourceType.MANA,
                            BASELINE_MAX_RESOURCE, MANA_IN_COMBAT_PER_SECOND,
                            MANA_OUT_OF_COMBAT_PER_SECOND,
                            0D, 0D, 0D, 0L, 0D, 0D, 0D, 0D, NearbyRecoveryBonus.NONE, DamageFreeRecoveryBonus.NONE)));

    private static final List<ClassFormDefinition> FORMS = aggregateForms();
    private static final Map<String, FixedAbilitySpec> ABILITIES = aggregateAbilities();
    private static final Map<String, PassiveProfile> PASSIVES = aggregatePassives();
    private static final Map<ClassResourceType, ClassResourceDefinition> RESOURCES = aggregateResources();
    private static final ClassCatalog CATALOG = ClassCatalog.create(
            PERSISTENCE_VERSION, FORMS, RETIRED_FORM_IDS);
    private static final FixedAbilityRegistry ABILITY_REGISTRY =
            FixedAbilityRegistry.create(CATALOG, ABILITIES);
    private static final FixedPassiveRegistry PASSIVE_REGISTRY =
            FixedPassiveRegistry.create(CATALOG, PASSIVES);

    private BuiltInClassContent() {
    }

    private static double manaRelativeRecovery(double manaPerSecond, int percent) {
        return manaPerSecond * percent / 100D;
    }

    public static ClassCatalog catalog() {
        return CATALOG;
    }

    public static FixedAbilityRegistry abilityRegistry() {
        return ABILITY_REGISTRY;
    }

    public static FixedPassiveRegistry passiveRegistry() {
        return PASSIVE_REGISTRY;
    }

    public static Map<ClassResourceType, ClassResourceDefinition> resourceDefinitions() {
        return RESOURCES;
    }

    private static List<ClassFormDefinition> aggregateForms() {
        List<ClassFormDefinition> forms = new ArrayList<>();
        for (ClassTreeContribution tree : TREES) forms.addAll(tree.forms());
        return List.copyOf(forms);
    }

    private static Map<String, FixedAbilitySpec> aggregateAbilities() {
        Map<String, FixedAbilitySpec> abilities = new LinkedHashMap<>();
        for (ClassTreeContribution tree : TREES)
            tree.abilities().forEach((id, spec) -> putUnique(abilities, id, spec, "ability"));
        return Collections.unmodifiableMap(abilities);
    }

    private static Map<String, PassiveProfile> aggregatePassives() {
        Map<String, PassiveProfile> passives = new LinkedHashMap<>();
        for (ClassTreeContribution tree : TREES)
            tree.passives().forEach((id, profile) -> putUnique(passives, id, profile, "passive"));
        return Collections.unmodifiableMap(passives);
    }

    private static Map<ClassResourceType, ClassResourceDefinition> aggregateResources() {
        Map<ClassResourceType, ClassResourceDefinition> resources = new LinkedHashMap<>();
        for (ClassTreeContribution tree : TREES) {
            ClassResourceDefinition definition = tree.resource();
            if (resources.putIfAbsent(definition.type(), definition) != null)
                throw new IllegalArgumentException("Duplicate resource contribution: " + definition.type());
        }
        return Collections.unmodifiableMap(resources);
    }

    private static ClassResourceDefinition resource(
            ClassResourceType type,
            double initialAmount,
            double inCombatTickDelta,
            double outOfCombatTickDelta,
            double damageDealtFlatGain,
            double damageReceivedHealthEquivalentGain,
            double damageReceivedFlatChange,
            long recoveryDelayAfterDamageTicks,
            double healingHealthEquivalentGain,
            double preventedDamageHealthEquivalentGain,
            double tauntGainPerEnemy,
            double tauntGainCap,
            NearbyRecoveryBonus nearbyRecoveryBonus,
            DamageFreeRecoveryBonus damageFreeRecoveryBonus) {
        return new ClassResourceDefinition(
                type,
                BASELINE_MAX_RESOURCE,
                initialAmount,
                inCombatTickDelta,
                outOfCombatTickDelta,
                damageDealtFlatGain,
                damageReceivedHealthEquivalentGain,
                damageReceivedFlatChange,
                recoveryDelayAfterDamageTicks,
                healingHealthEquivalentGain,
                preventedDamageHealthEquivalentGain,
                tauntGainPerEnemy,
                tauntGainCap,
                nearbyRecoveryBonus,
                damageFreeRecoveryBonus);
    }

    private static <V> void putUnique(Map<String, V> target, String id, V value, String type) {
        if (target.putIfAbsent(id, value) != null)
            throw new IllegalArgumentException("Duplicate " + type + " contribution: " + id);
    }
}
