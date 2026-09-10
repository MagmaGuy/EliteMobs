package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;

/** Pure ownership policy for the one-tick Blink potion flourish. */
public final class BlinkPotionFlashPolicy {
    private static final double MINIMUM_DISTANCE_SQUARED = 1.0E-6D;

    private BlinkPotionFlashPolicy() {
    }

    public static boolean shouldFlash(boolean successful, double distanceSquared) {
        return successful && Double.isFinite(distanceSquared)
                && distanceSquared > MINIMUM_DISTANCE_SQUARED;
    }

    public static CleanupAction cleanup(
            long scheduledToken,
            long currentToken,
            boolean externallyChanged,
            boolean flashInstalled,
            EffectState flash,
            EffectState current,
            EffectState previous) {
        Objects.requireNonNull(flash, "flash");
        if (scheduledToken != currentToken || externallyChanged || !flashInstalled) {
            return CleanupAction.NOOP;
        }
        if (current != null && !current.sameIdentity(flash)) return CleanupAction.NOOP;
        if (previous != null) return CleanupAction.RESTORE_PREVIOUS;
        return current == null ? CleanupAction.NOOP : CleanupAction.REMOVE_FLASH;
    }

    /** Zero means the displaced finite effect expired while the flash owned its visible slot. */
    public static int restoredDuration(int originalDuration, boolean infinite, long elapsedTicks) {
        if (infinite) return originalDuration;
        long remaining = (long) originalDuration - Math.max(0L, elapsedTicks);
        return remaining <= 0L ? 0 : (int) Math.min(Integer.MAX_VALUE, remaining);
    }

    public enum CleanupAction {
        NOOP,
        REMOVE_FLASH,
        RESTORE_PREVIOUS
    }

    /** Duration is deliberately excluded from identity because the server decrements it each tick. */
    public record EffectState(int amplifier, int durationTicks, boolean ambient,
                              boolean particles, boolean icon) {
        boolean sameIdentity(EffectState other) {
            return other != null
                    && amplifier == other.amplifier
                    && ambient == other.ambient
                    && particles == other.particles
                    && icon == other.icon;
        }
    }
}
