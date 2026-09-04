package com.magmaguy.elitemobs.experimentalcombat.progression;

import java.util.Arrays;
import java.util.Optional;

/** The two supported Experimental Combat input layouts. */
public enum InputProfile {
    JAVA_HOTBAR_LAYER("java_hotbar_layer"),
    FOCUS_ITEM("focus_item");

    public static final InputProfile DEFAULT = JAVA_HOTBAR_LAYER;

    private final String storedId;

    InputProfile(String storedId) {
        this.storedId = storedId;
    }

    public String storedId() {
        return storedId;
    }

    public static Optional<InputProfile> fromStoredId(String storedId) {
        if (storedId == null) return Optional.empty();
        return Arrays.stream(values()).filter(profile -> profile.storedId.equals(storedId)).findFirst();
    }
}
