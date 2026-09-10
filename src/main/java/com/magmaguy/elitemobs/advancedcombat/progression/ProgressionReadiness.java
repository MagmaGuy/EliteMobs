package com.magmaguy.elitemobs.advancedcombat.progression;

/** Per-player cache lifecycle state. */
public enum ProgressionReadiness {
    UNLOADED,
    LOADING,
    READY,
    FAILED,
    CLOSED
}
