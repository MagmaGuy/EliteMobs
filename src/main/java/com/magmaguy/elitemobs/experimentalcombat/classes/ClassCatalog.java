package com.magmaguy.elitemobs.experimentalcombat.classes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Validated fixed catalog for the Experimental Combat class forest.
 */
public final class ClassCatalog {
    private final int persistenceVersion;
    private final Set<String> retiredFormIds;
    private final Map<String, ClassFormDefinition> formsById;
    private final Map<String, List<ClassFormDefinition>> childrenById;
    private final List<ClassFormDefinition> roots;

    private ClassCatalog(int persistenceVersion,
                         Set<String> retiredFormIds,
                         Map<String, ClassFormDefinition> formsById,
                         Map<String, List<ClassFormDefinition>> childrenById,
                         List<ClassFormDefinition> roots) {
        this.persistenceVersion = persistenceVersion;
        this.retiredFormIds = retiredFormIds;
        this.formsById = formsById;
        this.childrenById = childrenById;
        this.roots = roots;
    }

    public static ClassCatalog create(
            int persistenceVersion,
            Collection<ClassFormDefinition> definitions,
            Collection<String> retiredFormIds) {
        if (persistenceVersion < 1)
            throw new IllegalArgumentException("persistenceVersion must be positive");
        Objects.requireNonNull(definitions, "definitions");
        Objects.requireNonNull(retiredFormIds, "retiredFormIds");

        Set<String> retired = new HashSet<>();
        for (String retiredId : retiredFormIds) {
            Objects.requireNonNull(retiredId, "retiredFormIds contains null");
            if (retiredId.isBlank()) throw new IllegalArgumentException("Retired form id must not be blank");
            if (!retired.add(retiredId))
                throw new IllegalArgumentException("Duplicate retired form id: " + retiredId);
        }

        Map<String, ClassFormDefinition> forms = new LinkedHashMap<>();
        Set<String> displayNames = new HashSet<>();
        Set<String> mechanicIds = new HashSet<>();
        for (ClassFormDefinition form : definitions) {
            Objects.requireNonNull(form, "definitions contains null");
            if (retired.contains(form.id()))
                throw new IllegalArgumentException("Retired class form id cannot be reused: " + form.id());
            if (forms.putIfAbsent(form.id(), form) != null)
                throw new IllegalArgumentException("Duplicate class form id: " + form.id());
            if (!displayNames.add(form.displayName().toLowerCase(Locale.ROOT)))
                throw new IllegalArgumentException("Duplicate class form display name: " + form.displayName());
            validateMechanicIds(form, mechanicIds);
        }

        if (forms.isEmpty())
            throw new IllegalArgumentException("Experimental Combat requires at least one class form");

        Map<String, List<ClassFormDefinition>> mutableChildren = new LinkedHashMap<>();
        forms.keySet().forEach(id -> mutableChildren.put(id, new ArrayList<>()));
        List<ClassFormDefinition> roots = new ArrayList<>();
        for (ClassFormDefinition form : forms.values()) {
            if (form.band().isRoot()) roots.add(form);
            if (form.parentId() == null) continue;

            ClassFormDefinition parent = forms.get(form.parentId());
            if (parent == null)
                throw new IllegalArgumentException("Missing parent " + form.parentId() + " for " + form.id());
            if (parent.band().depth() + 1 != form.band().depth())
                throw new IllegalArgumentException("Parent " + parent.id() + " is not in the preceding band for " + form.id());
            mutableChildren.get(parent.id()).add(form);
        }

        validateForest(forms, mutableChildren, roots);

        Map<String, List<ClassFormDefinition>> immutableChildren = new LinkedHashMap<>();
        mutableChildren.forEach((id, children) -> immutableChildren.put(id, List.copyOf(children)));
        return new ClassCatalog(
                persistenceVersion,
                Set.copyOf(retired),
                Collections.unmodifiableMap(new LinkedHashMap<>(forms)),
                Collections.unmodifiableMap(immutableChildren),
                List.copyOf(roots));
    }

    public Collection<ClassFormDefinition> forms() {
        return formsById.values();
    }

    public int persistenceVersion() {
        return persistenceVersion;
    }

    public Set<String> retiredFormIds() {
        return retiredFormIds;
    }

    /** Roots of combat kits, including the starter. Progression can connect these kits. */
    public List<ClassFormDefinition> roots() {
        return roots;
    }

    public Optional<ClassFormDefinition> find(String formId) {
        return Optional.ofNullable(formsById.get(formId));
    }

    public ClassFormDefinition require(String formId) {
        ClassFormDefinition form = formsById.get(formId);
        if (form == null) throw new IllegalArgumentException("Unknown class form: " + formId);
        return form;
    }

    public List<ClassFormDefinition> childrenOf(String formId) {
        List<ClassFormDefinition> children = childrenById.get(formId);
        if (children == null) throw new IllegalArgumentException("Unknown class form: " + formId);
        return children;
    }

    public ClassLineage lineageOf(String formId) {
        List<ClassFormDefinition> path = progressionPathOf(formId);
        // A new root replaces the starter kit rather than inheriting its resource or passives.
        for (int index = path.size() - 1; index >= 0; index--)
            if (path.get(index).rootKit() != null)
                return new ClassLineage(path.subList(index, path.size()));
        throw new IllegalStateException("Class has no combat kit: " + formId);
    }

    /** Full prerequisite path, including the starter before a resource-owning root class. */
    public List<ClassFormDefinition> progressionPathOf(String formId) {
        ClassFormDefinition current = require(formId);
        List<ClassFormDefinition> reversed = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        while (current != null) {
            if (!visited.add(current.id())) throw new IllegalStateException("Cycle in class lineage at " + current.id());
            reversed.add(current);
            current = current.parentId() == null ? null : require(current.parentId());
        }
        return List.copyOf(reversed.reversed());
    }

    public ClassFormDefinition rootOf(String formId) {
        return lineageOf(formId).root();
    }

    private static void validateMechanicIds(ClassFormDefinition form, Set<String> mechanicIds) {
        requireScopedId(form.id(), form.signature().id(), "signature");
        requireScopedId(form.id(), form.utility().id(), "utility");
        requireScopedId(form.id(), form.passive().id(), "passive");
        addUniqueMechanicId(mechanicIds, form.signature().id());
        addUniqueMechanicId(mechanicIds, form.utility().id());
        addUniqueMechanicId(mechanicIds, form.passive().id());
        if (form.rootKit() != null) {
            requireScopedId(form.id(), form.rootKit().mobility().id(), "mobility");
            addUniqueMechanicId(mechanicIds, form.rootKit().mobility().id());
        }
    }

    private static void requireScopedId(String formId, String mechanicId, String mechanic) {
        String expected = formId + "." + mechanic;
        if (!mechanicId.equals(expected))
            throw new IllegalArgumentException("The " + mechanic + " id for " + formId + " must be " + expected);
    }

    private static void addUniqueMechanicId(Set<String> ids, String id) {
        if (!ids.add(id)) throw new IllegalArgumentException("Duplicate class mechanic id: " + id);
    }

    private static void validateForest(Map<String, ClassFormDefinition> forms,
                                       Map<String, List<ClassFormDefinition>> children,
                                       List<ClassFormDefinition> roots) {
        if (roots.isEmpty())
            throw new IllegalArgumentException("Experimental Combat requires at least one root class");

        for (ClassFormDefinition form : forms.values()) {
            int childCount = children.get(form.id()).size();
            if (form.band() == ClassBand.STARTER) {
                if (childCount == 0)
                    throw new IllegalArgumentException("Starter " + form.id() + " must lead to a root class");
                continue;
            }
            int expectedChildren = form.band().isTerminal() ? 0 : 2;
            if (childCount != expectedChildren)
                throw new IllegalArgumentException(form.id() + " requires exactly " + expectedChildren
                        + " children, got " + childCount);
        }

        Set<String> reachable = new HashSet<>();
        for (ClassFormDefinition root : roots)
            if (root.parentId() == null) collectReachable(root, children, reachable);
        if (reachable.size() != forms.size())
            throw new IllegalArgumentException("Every class form must be reachable from exactly one root");
    }

    private static void collectReachable(ClassFormDefinition form,
                                         Map<String, List<ClassFormDefinition>> children,
                                         Set<String> reachable) {
        if (!reachable.add(form.id())) throw new IllegalArgumentException("Cycle or repeated class form: " + form.id());
        for (ClassFormDefinition child : children.get(form.id())) collectReachable(child, children, reachable);
    }
}
