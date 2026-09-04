package com.magmaguy.elitemobs.mobconstructor;

import java.util.EnumSet;
import java.util.Objects;

/** Small reason-aware state machine shared by managed and unmanaged Lua runtimes. */
public final class ElitePowerPauseState {
    private final EnumSet<ElitePowerPauseReason> activeReasons =
            EnumSet.noneOf(ElitePowerPauseReason.class);

    public boolean set(ElitePowerPauseReason reason, boolean paused) {
        Objects.requireNonNull(reason, "reason");
        boolean before = isPaused();
        if (paused) activeReasons.add(reason);
        else activeReasons.remove(reason);
        return before != isPaused();
    }

    public boolean isPaused() {
        return !activeReasons.isEmpty();
    }

    public boolean has(ElitePowerPauseReason reason) {
        return activeReasons.contains(Objects.requireNonNull(reason, "reason"));
    }
}
