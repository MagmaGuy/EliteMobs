package com.magmaguy.elitemobs.experimentalcombat.resources;

import com.magmaguy.elitemobs.experimentalcombat.classes.ClassResourceType;

import java.util.Objects;

/**
 * Deterministic tuning contract for Fury generation.
 *
 * <p>The representative sustained-combat minute deals damage equal to five player health bars
 * and receives damage equal to half a player health bar. Those are deliberately explicit
 * assumptions, expressed in the same health-equivalent units used by the runtime, so changing
 * either the expected combat cadence or either gain coefficient makes the fill-time test fail.</p>
 */
public final class FuryCombatBudget {
    public static final double TARGET_FILL_SECONDS = 60D;
    public static final double DEALT_HEALTH_EQUIVALENTS_PER_MINUTE = 5D;
    public static final double RECEIVED_HEALTH_EQUIVALENTS_PER_MINUTE = .5D;
    public static final double DEALT_GAIN_PER_HEALTH_EQUIVALENT = 16D;
    public static final double RECEIVED_GAIN_PER_HEALTH_EQUIVALENT = 40D;

    private FuryCombatBudget() {
    }

    public static double expectedGainPerMinute(ClassResourceDefinition definition) {
        requireFury(definition);
        return DEALT_HEALTH_EQUIVALENTS_PER_MINUTE
                * definition.damageDealtHealthEquivalentGain()
                + RECEIVED_HEALTH_EQUIVALENTS_PER_MINUTE
                * definition.damageReceivedHealthEquivalentGain();
    }

    public static double expectedFillSeconds(ClassResourceDefinition definition) {
        double gainPerMinute = expectedGainPerMinute(definition);
        if (gainPerMinute <= 0D) return Double.POSITIVE_INFINITY;
        return Math.max(0D, definition.maximum() - definition.initialAmount())
                / gainPerMinute * 60D;
    }

    private static void requireFury(ClassResourceDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (definition.type() != ClassResourceType.FURY)
            throw new IllegalArgumentException("Fury budget requires the Fury resource definition");
    }
}
