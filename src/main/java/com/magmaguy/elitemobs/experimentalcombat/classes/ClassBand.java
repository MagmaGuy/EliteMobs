package com.magmaguy.elitemobs.experimentalcombat.classes;

import java.util.Arrays;
import java.util.Optional;

/**
 * A form's position in progression: an introductory ten-level starter followed
 * by the established 30-level cadence within each main class kit.
 *
 * <p>The final band intentionally has no hard upper bound. Level 100 is a soft cap,
 * matching foundation skills, rather than a terminal progression limit.</p>
 */
public enum ClassBand {
    ROOT(0, 1, 30, 10),
    LEVEL_31(1, 31, 60, 30),
    LEVEL_61(2, 61, 90, 60),
    LEVEL_91(3, 91, Integer.MAX_VALUE, 90),
    STARTER(-1, 1, 10, 1);

    private final int depth;
    private final int effectiveStart;
    private final int effectiveEnd;
    private final int skillUnlockLevel;

    ClassBand(int depth, int effectiveStart, int effectiveEnd, int skillUnlockLevel) {
        this.depth = depth;
        this.effectiveStart = effectiveStart;
        this.effectiveEnd = effectiveEnd;
        this.skillUnlockLevel = skillUnlockLevel;
    }

    public int depth() {
        return depth;
    }

    public int effectiveStart() {
        return effectiveStart;
    }

    public int effectiveEnd() {
        return effectiveEnd;
    }

    public int skillUnlockLevel() {
        return skillUnlockLevel;
    }

    public boolean isRoot() {
        return this == ROOT || this == STARTER;
    }

    public boolean isTerminal() {
        return this == LEVEL_91;
    }

    public boolean containsEffectiveLevel(int effectiveLevel) {
        return effectiveLevel >= effectiveStart && effectiveLevel <= effectiveEnd;
    }

    public int toEffectiveLevel(int localLevel) {
        if (localLevel < 1) throw new IllegalArgumentException("Local class level must be positive");
        long effectiveLevel = (long) effectiveStart + localLevel - 1L;
        if (effectiveLevel > effectiveEnd)
            throw new IllegalArgumentException("Local level " + localLevel + " exceeds the " + this + " band");
        return Math.toIntExact(effectiveLevel);
    }

    public int toLocalLevel(int effectiveLevel) {
        if (!containsEffectiveLevel(effectiveLevel))
            throw new IllegalArgumentException("Effective level " + effectiveLevel + " is outside the " + this + " band");
        return effectiveLevel - effectiveStart + 1;
    }

    public Optional<ClassBand> next() {
        if (isTerminal()) return Optional.empty();
        return Arrays.stream(values()).filter(candidate -> candidate.depth == depth + 1).findFirst();
    }

    public static ClassBand atDepth(int depth) {
        return Arrays.stream(values())
                .filter(candidate -> candidate.depth == depth)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported class-tree depth: " + depth));
    }
}
