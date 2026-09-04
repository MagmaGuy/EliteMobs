package com.magmaguy.elitemobs.experimentalcombat.progression;

/** Per-player cache lifecycle state. */
public enum ProgressionReadiness {
    UNLOADED,
    LOADING,
    READY,
    FAILED,
    CLOSED
}
