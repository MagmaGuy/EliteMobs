package com.magmaguy.elitemobs.advancedcombat.classes;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable presentation and identity data for one active class ability.
 */
public record AbilityDefinition(String id, String displayName, AbilitySlot slot, String description) {
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9_.-]*");

    public AbilityDefinition {
        id = requireId(id);
        displayName = requireText(displayName, "displayName");
        slot = Objects.requireNonNull(slot, "slot");
        description = requireText(description, "description");
    }

    private static String requireId(String id) {
        String value = requireText(id, "id");
        if (!ID_PATTERN.matcher(value).matches())
            throw new IllegalArgumentException("Ability id must be a stable lowercase identifier: " + value);
        return value;
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        if (!value.equals(value.strip())) throw new IllegalArgumentException(field + " must not have surrounding whitespace");
        return value;
    }
}
