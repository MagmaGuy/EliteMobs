package com.magmaguy.elitemobs.experimentalcombat.progression;

/** Why additional XP cannot currently raise a form's local level. */
public enum ProgressionCapReason {
    /** No resolved or unlocked form exists, so no progression cap applies. */
    NOT_APPLICABLE,
    FOUNDATION_SKILLS,
    BAND_COMPLETE
}
