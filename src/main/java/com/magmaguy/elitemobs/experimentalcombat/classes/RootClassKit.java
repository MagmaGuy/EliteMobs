package com.magmaguy.elitemobs.experimentalcombat.classes;

import java.util.Objects;

/**
 * Root-owned mechanics inherited unchanged throughout one class tree.
 */
public record RootClassKit(ClassResourceType resourceType, AbilityDefinition mobility) {
    public RootClassKit {
        resourceType = Objects.requireNonNull(resourceType, "resourceType");
        mobility = Objects.requireNonNull(mobility, "mobility");
        if (mobility.slot() != AbilitySlot.MOBILITY)
            throw new IllegalArgumentException("A root class kit must contain a Mobility ability");
    }
}
