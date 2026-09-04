package com.magmaguy.elitemobs.experimentalcombat.content;

import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilityRegistry;
import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassResourceType;
import com.magmaguy.elitemobs.experimentalcombat.passives.FixedPassiveRegistry;
import com.magmaguy.elitemobs.experimentalcombat.passives.PassiveProfile;
import com.magmaguy.elitemobs.experimentalcombat.resources.ClassResourceDefinition;
import com.magmaguy.elitemobs.experimentalcombat.resources.FuryCombatBudget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Immutable assembly and factory for the complete built-in class-system baseline. */
public final class BuiltInClassContent {
    private static final double BASELINE_MAX_RESOURCE = 100D;
    private static final long FOCUS_RECOVERY_DELAY_TICKS = 60L;
    private static final double MANA_IN_COMBAT_PER_SECOND = BASELINE_MAX_RESOURCE / 60D;
    private static final double MANA_OUT_OF_COMBAT_PER_SECOND = MANA_IN_COMBAT_PER_SECOND;
    private static final double RESOLVE_IN_COMBAT_PER_SECOND = MANA_IN_COMBAT_PER_SECOND / 3D;
    private static final double RESOLVE_OUT_OF_COMBAT_PER_SECOND = MANA_OUT_OF_COMBAT_PER_SECOND / 3D;

    /** Increment when stable form identities or progression semantics change incompatibly. */
    public static final int PERSISTENCE_VERSION = 1;

    /** IDs are never removed from this reservation set after a released catalog retires them. */
    private static final Set<String> RETIRED_FORM_IDS = Set.of();

    private static final List<ClassTreeContribution> TREES = List.of(
            new ClassTreeContribution(
                    "paladin",
                    BuiltInClassDefinitions.paladinTree(),
                    FixedAbilityRegistry.paladinDefinitions(),
                    FixedPassiveRegistry.paladinDefinitions(),
                    resource(ClassResourceType.RESOLVE,
                            0D, RESOLVE_IN_COMBAT_PER_SECOND, RESOLVE_OUT_OF_COMBAT_PER_SECOND,
                            0D, 45D, 0D, 0L, 0D, 40D, 5D, 25D)),
            new ClassTreeContribution(
                    "berserker",
                    BuiltInClassDefinitions.berserkerTree(),
                    FixedAbilityRegistry.berserkerDefinitions(),
                    FixedPassiveRegistry.berserkerDefinitions(),
                    resource(ClassResourceType.FURY,
                            0D, 0D, -15D,
                            FuryCombatBudget.DEALT_GAIN_PER_HEALTH_EQUIVALENT,
                            FuryCombatBudget.RECEIVED_GAIN_PER_HEALTH_EQUIVALENT,
                            0D, 0L, 0D, 0D, 0D, 0D)),
            new ClassTreeContribution(
                    "ranger",
                    BuiltInClassDefinitions.rangerTree(),
                    FixedAbilityRegistry.rangerDefinitions(),
                    FixedPassiveRegistry.rangerDefinitions(),
                    resource(ClassResourceType.FOCUS,
                            BASELINE_MAX_RESOURCE, 12D, 12D, 0D, 0D, -20D,
                            FOCUS_RECOVERY_DELAY_TICKS, 0D, 0D, 0D, 0D)),
            new ClassTreeContribution(
                    "cleric",
                    BuiltInClassDefinitions.clericTree(),
                    FixedAbilityRegistry.clericDefinitions(),
                    FixedPassiveRegistry.clericDefinitions(),
                    resource(ClassResourceType.GRACE,
                            BASELINE_MAX_RESOURCE, 8D, 8D, 0D, 0D, 0D, 0L, 30D, 0D, 0D, 0D)),
            new ClassTreeContribution(
                    "spellcaster",
                    BuiltInClassDefinitions.spellcasterTree(),
                    FixedAbilityRegistry.spellcasterDefinitions(),
                    FixedPassiveRegistry.spellcasterDefinitions(),
                    resource(ClassResourceType.MANA,
                            BASELINE_MAX_RESOURCE, MANA_IN_COMBAT_PER_SECOND,
                            MANA_OUT_OF_COMBAT_PER_SECOND,
                            0D, 0D, 0D, 0L, 0D, 0D, 0D, 0D)));

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
            double damageDealtHealthEquivalentGain,
            double damageReceivedHealthEquivalentGain,
            double damageReceivedFlatChange,
            long recoveryDelayAfterDamageTicks,
            double healingHealthEquivalentGain,
            double preventedDamageHealthEquivalentGain,
            double tauntGainPerEnemy,
            double tauntGainCap) {
        return new ClassResourceDefinition(
                type,
                BASELINE_MAX_RESOURCE,
                initialAmount,
                inCombatTickDelta,
                outOfCombatTickDelta,
                damageDealtHealthEquivalentGain,
                damageReceivedHealthEquivalentGain,
                damageReceivedFlatChange,
                recoveryDelayAfterDamageTicks,
                healingHealthEquivalentGain,
                preventedDamageHealthEquivalentGain,
                tauntGainPerEnemy,
                tauntGainCap);
    }

    private static <V> void putUnique(Map<String, V> target, String id, V value, String type) {
        if (target.putIfAbsent(id, value) != null)
            throw new IllegalArgumentException("Duplicate " + type + " contribution: " + id);
    }
}
