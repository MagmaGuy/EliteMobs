package com.magmaguy.elitemobs.advancedcombat;

/** Decides whether the next runtime shutdown is a terminal cleanup or a soft-reload handoff. */
final class AdvancedCombatShutdownPolicy {

    private boolean softReloadPrepared;

    enum HealthCleanup {
        RESTORE_ORDINARY_HEALTH,
        PRESERVE_FOR_SOFT_RELOAD
    }

    void prepareSoftReload() {
        softReloadPrepared = true;
    }

    HealthCleanup consumeHealthCleanup() {
        HealthCleanup cleanup = softReloadPrepared
                ? HealthCleanup.PRESERVE_FOR_SOFT_RELOAD
                : HealthCleanup.RESTORE_ORDINARY_HEALTH;
        softReloadPrepared = false;
        return cleanup;
    }
}
