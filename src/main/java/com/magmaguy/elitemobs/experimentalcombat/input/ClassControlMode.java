package com.magmaguy.elitemobs.experimentalcombat.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Session-scoped policy for the optional class-control layer outside EliteMobs combat content.
 *
 * <p>This toggle governs ability input only. The experimental health pool, fixed hunger, and
 * out-of-combat regeneration belong to {@code ExperimentalCombatRuntime} and stay reserved for
 * EliteMobs managed/protected worlds regardless of this toggle.</p>
 */
final class ClassControlMode {

    /** Same rhythm as the dash double-tap so players only learn one "quick succession" window. */
    static final long TOGGLE_TAP_WINDOW_TICKS = ClassAbilityGestureState.CHORD_WINDOW_TICKS;

    private final Set<UUID> outsideEnabled = new HashSet<>();
    private final Map<UUID, Long> pendingToggleTaps = new HashMap<>();

    boolean enabled(UUID playerId, boolean alwaysAvailable, boolean outsideAllowed) {
        Objects.requireNonNull(playerId, "playerId");
        if (alwaysAvailable) return true;
        if (!outsideAllowed) {
            outsideEnabled.remove(playerId);
            return false;
        }
        return outsideEnabled.contains(playerId);
    }

    /**
     * Resolves one F press. Inside EliteMobs combat content F always opens abilities. Outside it,
     * a plain F opens abilities only while the layer is enabled, a single sneak-held F stays a
     * vanilla hand swap, and a sneak-held double-F in quick succession is the session toggle.
     * Neither toggle tap is consumed: the pair of vanilla swaps restores the hands by itself.
     */
    FAction pressF(
            UUID playerId,
            long currentTick,
            boolean alreadySneaking,
            boolean alwaysAvailable,
            boolean outsideAllowed,
            boolean abilityInputReady) {
        Objects.requireNonNull(playerId, "playerId");
        if (!abilityInputReady) {
            pendingToggleTaps.remove(playerId);
            return FAction.PASS_THROUGH;
        }
        if (alwaysAvailable) {
            pendingToggleTaps.remove(playerId);
            return FAction.OPEN_ABILITIES;
        }
        if (!alreadySneaking) {
            pendingToggleTaps.remove(playerId);
            return enabled(playerId, false, outsideAllowed)
                    ? FAction.OPEN_ABILITIES
                    : FAction.PASS_THROUGH;
        }
        Long firstTap = pendingToggleTaps.remove(playerId);
        boolean completesDoubleTap = firstTap != null
                && currentTick >= firstTap
                && currentTick - firstTap <= TOGGLE_TAP_WINDOW_TICKS;
        if (!completesDoubleTap) {
            pendingToggleTaps.put(playerId, currentTick);
            return FAction.PASS_THROUGH;
        }
        if (!outsideAllowed) {
            outsideEnabled.remove(playerId);
            return FAction.TOGGLE_BLOCKED;
        }
        if (outsideEnabled.remove(playerId)) return FAction.TOGGLED_OFF;
        outsideEnabled.add(playerId);
        return FAction.TOGGLED_ON;
    }

    void clear(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        outsideEnabled.remove(playerId);
        pendingToggleTaps.remove(playerId);
    }

    void clearAll() {
        outsideEnabled.clear();
        pendingToggleTaps.clear();
    }

    enum FAction {
        PASS_THROUGH,
        OPEN_ABILITIES,
        TOGGLED_ON,
        TOGGLED_OFF,
        TOGGLE_BLOCKED
    }
}
