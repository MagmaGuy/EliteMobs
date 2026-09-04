package com.magmaguy.elitemobs.experimentalcombat.content;

import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.passives.PassiveProfile;
import com.magmaguy.elitemobs.experimentalcombat.resources.ClassResourceDefinition;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * One self-contained class tree contribution: player-facing forms, executable abilities,
 * passive mechanics and its resource economy. Future roots register one contribution.
 */
public record ClassTreeContribution(
        String rootId,
        List<ClassFormDefinition> forms,
        Map<String, FixedAbilitySpec> abilities,
        Map<String, PassiveProfile> passives,
        ClassResourceDefinition resource) {

    public ClassTreeContribution {
        Objects.requireNonNull(rootId, "rootId");
        if (rootId.isBlank()) throw new IllegalArgumentException("rootId must not be blank");
        forms = List.copyOf(forms);
        abilities = immutableCopy(abilities, "abilities");
        passives = immutableCopy(passives, "passives");
        resource = Objects.requireNonNull(resource, "resource");
        if (forms.isEmpty()) throw new IllegalArgumentException(rootId + " tree has no forms");

        Set<String> formIds = new HashSet<>();
        int roots = 0;
        ClassFormDefinition rootForm = null;
        for (ClassFormDefinition form : forms) {
            if (!formIds.add(form.id()))
                throw new IllegalArgumentException("Duplicate form " + form.id() + " in " + rootId);
            if (form.band().isRoot()) {
                roots++;
                rootForm = form;
                if (!form.id().equals(rootId))
                    throw new IllegalArgumentException("Contribution " + rootId + " contains root " + form.id());
            }
        }
        if (roots != 1) throw new IllegalArgumentException(rootId + " must contain exactly one root form");
        if (rootForm == null || rootForm.rootKit().resourceType() != resource.type())
            throw new IllegalArgumentException(rootId + " resource definition does not match its root kit");
        for (ClassFormDefinition form : forms)
            if (!form.band().isRoot() && !formIds.contains(form.parentId()))
                throw new IllegalArgumentException(form.id() + " has a parent outside contribution " + rootId);

        Set<String> expectedAbilityIds = new HashSet<>();
        for (ClassFormDefinition form : forms) {
            expectedAbilityIds.add(form.signature().id());
            expectedAbilityIds.add(form.utility().id());
            form.optionalRootKit().ifPresent(kit -> expectedAbilityIds.add(kit.mobility().id()));
        }
        if (!abilities.keySet().equals(expectedAbilityIds))
            throw new IllegalArgumentException(rootId + " ability contribution does not match its forms");
        abilities.forEach((id, spec) -> {
            if (!id.equals(spec.id()))
                throw new IllegalArgumentException(rootId + " ability key does not match spec id " + id);
        });
        if (!passives.keySet().equals(formIds))
            throw new IllegalArgumentException(rootId + " passive contribution does not match its forms");
    }

    private static <K, V> Map<K, V> immutableCopy(Map<K, V> source, String field) {
        Objects.requireNonNull(source, field);
        LinkedHashMap<K, V> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            Objects.requireNonNull(key, field + " contains a null key");
            Objects.requireNonNull(value, field + " contains a null value");
            if (copy.putIfAbsent(key, value) != null)
                throw new IllegalArgumentException(field + " contains a duplicate key " + key);
        });
        return Collections.unmodifiableMap(copy);
    }
}
