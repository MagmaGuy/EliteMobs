package com.magmaguy.elitemobs.pathfinding.patrol;

import java.util.Locale;

public enum PatrolMode {
    LOOP,
    REVERSE;

    public static PatrolMode parse(String value) {
        if (value == null || value.isBlank()) return LOOP;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("patrol.mode must be LOOP or REVERSE", exception);
        }
    }
}
