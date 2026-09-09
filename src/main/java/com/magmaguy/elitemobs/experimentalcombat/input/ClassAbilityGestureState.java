package com.magmaguy.elitemobs.experimentalcombat.input;

import java.util.Objects;

/**
 * Pure state machine for the Java F-key ability layer.
 *
 * <p>F opens a short selection window. A second plain F inside that window selects mobility.
 * Hotbar keys 1, 2 and 3 select mobility, signature and utility. Keys 7, 8 and 9 mirror those
 * bindings so a player can still activate the ability assigned to their already-selected slot,
 * which a vanilla client does not report as a slot change. Left click selects signature;
 * right click selects utility inside the window. Jumping does not change this state.
 * The state contains no
 * Bukkit objects, which keeps timing and precedence testable without a server.</p>
 */
public final class ClassAbilityGestureState {

    /** 600 ms at the normal server tick rate. */
    public static final long CHORD_WINDOW_TICKS = 12L;
    private static final long CLOSED_TICK = Long.MIN_VALUE;
    private static final ClassAbilityGestureState CLOSED = new ClassAbilityGestureState(CLOSED_TICK);

    private final long openedAtTick;

    private ClassAbilityGestureState(long openedAtTick) {
        this.openedAtTick = openedAtTick;
    }

    public static ClassAbilityGestureState closed() {
        return CLOSED;
    }

    public Transition pressF(long currentTick, boolean sneaking) {
        if (!sneaking && isOpenAt(currentTick))
            return new Transition(CLOSED, Outcome.MOBILITY);
        return new Transition(new ClassAbilityGestureState(currentTick), Outcome.CHORD_OPENED);
    }

    /** Resolves a left click while the F chord is open: it selects signature. */
    public Transition leftClick(long currentTick) {
        return inWindowSelection(currentTick, Outcome.SIGNATURE);
    }

    /** Resolves a right click while the F chord is open: it selects utility. */
    public Transition rightClick(long currentTick) {
        return inWindowSelection(currentTick, Outcome.UTILITY);
    }

    private Transition inWindowSelection(long currentTick, Outcome outcome) {
        if (isOpenAt(currentTick)) return new Transition(CLOSED, outcome);
        return new Transition(CLOSED, Outcome.PASS_THROUGH);
    }

    /**
     * Resolves a zero-based vanilla hotbar selection while the F chord is open.
     * Unsupported or late selections pass through and close the temporary layer.
     */
    public Transition selectHotbar(long currentTick, int zeroBasedHotbarSlot) {
        if (!isOpenAt(currentTick)) return new Transition(CLOSED, Outcome.PASS_THROUGH);
        Outcome outcome = switch (zeroBasedHotbarSlot) {
            case 0, 6 -> Outcome.MOBILITY;
            case 1, 7 -> Outcome.SIGNATURE;
            case 2, 8 -> Outcome.UTILITY;
            default -> Outcome.PASS_THROUGH;
        };
        return new Transition(CLOSED, outcome);
    }

    public boolean isOpenAt(long currentTick) {
        if (openedAtTick == CLOSED_TICK || currentTick < openedAtTick) return false;
        return currentTick - openedAtTick <= CHORD_WINDOW_TICKS;
    }

    public enum Outcome {
        CHORD_OPENED,
        MOBILITY,
        SIGNATURE,
        UTILITY,
        PASS_THROUGH
    }

    public record Transition(ClassAbilityGestureState next, Outcome outcome) {
        public Transition {
            Objects.requireNonNull(next, "next");
            Objects.requireNonNull(outcome, "outcome");
        }

        public boolean consumesInput() {
            return outcome == Outcome.MOBILITY
                    || outcome == Outcome.SIGNATURE
                    || outcome == Outcome.UTILITY;
        }
    }
}
