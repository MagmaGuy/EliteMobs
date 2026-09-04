package com.magmaguy.elitemobs.experimentalcombat.classes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A validated root-to-form path. It resolves inherited mechanics without copying them into descendants.
 */
public record ClassLineage(List<ClassFormDefinition> forms) {
    public ClassLineage {
        Objects.requireNonNull(forms, "forms");
        forms = List.copyOf(forms);
        if (forms.isEmpty()) throw new IllegalArgumentException("A class lineage must contain a root");
        if (!forms.getFirst().band().isRoot()) throw new IllegalArgumentException("A class lineage must start at a root");

        for (int index = 0; index < forms.size(); index++) {
            ClassFormDefinition form = forms.get(index);
            if (form.band().depth() != index)
                throw new IllegalArgumentException("Lineage skips class band at " + form.id());
            if (index == 0) continue;
            ClassFormDefinition parent = forms.get(index - 1);
            if (!form.parentId().equals(parent.id()))
                throw new IllegalArgumentException(form.id() + " is not a child of " + parent.id());
        }
    }

    public ClassFormDefinition root() {
        return forms.getFirst();
    }

    public ClassFormDefinition activeForm() {
        return forms.getLast();
    }

    public RootClassKit rootKit() {
        return root().rootKit();
    }

    public ClassResourceType resourceType() {
        return rootKit().resourceType();
    }

    public AbilityDefinition mobility() {
        return rootKit().mobility();
    }

    public AbilityDefinition signature() {
        return activeForm().signature();
    }

    public AbilityDefinition utility() {
        return activeForm().utility();
    }

    public List<PassiveDefinition> passives() {
        return forms.stream().map(ClassFormDefinition::passive).toList();
    }

    public List<String> formIds() {
        return forms.stream().map(ClassFormDefinition::id).toList();
    }

    public boolean contains(String formId) {
        return forms.stream().anyMatch(form -> form.id().equals(formId));
    }

    public Optional<ClassFormDefinition> formAt(ClassBand band) {
        Objects.requireNonNull(band, "band");
        return forms.stream().filter(form -> form.band() == band).findFirst();
    }

    /**
     * Returns the effective scaling level of every passive in this lineage.
     *
     * <p>At effective level 61, for example, the root contributes at 61, its level-31
     * child contributes at 31 and the active level-61 form contributes at 1.</p>
     */
    public Map<String, Integer> passiveContributionLevels(int activeEffectiveLevel) {
        if (!activeForm().band().containsEffectiveLevel(activeEffectiveLevel))
            throw new IllegalArgumentException("Effective level " + activeEffectiveLevel
                    + " is outside active form " + activeForm().displayName());

        Map<String, Integer> contributionLevels = new LinkedHashMap<>();
        for (ClassFormDefinition form : forms) {
            int contributionLevel = activeEffectiveLevel - form.band().effectiveStart() + 1;
            contributionLevels.put(form.id(), contributionLevel);
        }
        return Collections.unmodifiableMap(contributionLevels);
    }
}
