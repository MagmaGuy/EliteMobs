package com.magmaguy.elitemobs.experimentalcombat.classes;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * One additive passive supplied by a form and inherited by its descendants.
 */
public record PassiveDefinition(String id, String description) {
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9_.-]*");

    public PassiveDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(description, "description");
        if (!ID_PATTERN.matcher(id).matches())
            throw new IllegalArgumentException("Passive id must be a stable lowercase identifier: " + id);
        if (description.isBlank()) throw new IllegalArgumentException("description must not be blank");
        if (!description.equals(description.strip()))
            throw new IllegalArgumentException("description must not have surrounding whitespace");
    }
}
