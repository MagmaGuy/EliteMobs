package com.magmaguy.elitemobs.skills.bonuses;

/**
 * Defines the different types of skill bonuses and how they trigger.
 */
public enum SkillBonusType {
    /**
     * Chance-based trigger on specific events.
     * Example: "X% chance to bleed on hit"
     */
    PROC,

    /**
     * Always active when equipped, no trigger needed.
     * Example: "Increased attack range"
     */
    PASSIVE,

    /**
     * Active only when specific conditions are met.
     * Example: "Bonus damage when target below 30% HP"
     */
    CONDITIONAL,

    /**
     * Builds up over time/actions, resets on condition.
     * Example: "Consecutive hits increase damage"
     */
    STACKING,

    /**
     * Can trigger but has internal cooldown.
     * Example: "Survive fatal damage (60s cooldown)"
     */
    COOLDOWN,

    /**
     * Player manually activates/deactivates.
     * Example: "Deal more damage but take more damage"
     */
    TOGGLE
}
