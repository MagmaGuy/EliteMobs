package com.magmaguy.elitemobs.advancedcombat.presentation;

/** Semantic presentation moments shared by every class ability. */
public enum AbilityPresentationCue {
    CAST(false),
    IMPACT(true),
    HEAL(true),
    BUFF(true),
    CONTROL(true),
    MOBILITY(false);

    private final boolean targetScoped;

    AbilityPresentationCue(boolean targetScoped) {
        this.targetScoped = targetScoped;
    }

    boolean targetScoped() {
        return targetScoped;
    }
}
