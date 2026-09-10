package com.magmaguy.elitemobs.advancedcombat;

/**
 * Vendor-neutral monotonic clock expressed in Minecraft tick units.
 *
 * <p>This deliberately measures elapsed real time rather than depending on a Paper-only server
 * tick accessor. It is suitable for input windows and same-tick input de-duplication; it is
 * not a source of world age or persisted time.</p>
 */
public final class MonotonicTickClock {

    private static final long NANOS_PER_TICK = 50_000_000L;

    private MonotonicTickClock() {
    }

    public static long currentTick() {
        return System.nanoTime() / NANOS_PER_TICK;
    }
}
