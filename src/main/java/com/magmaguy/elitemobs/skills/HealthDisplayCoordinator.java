package com.magmaguy.elitemobs.skills;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.EnumSet;

/** Coordinates EliteMobs features that intentionally render a fixed ten-heart health HUD. */
public final class HealthDisplayCoordinator {

    public enum Owner {
        ARMOR_SKILL,
        EXPERIMENTAL_COMBAT
    }

    private static final double DISPLAY_HEALTH = 20D;
    private static final Map<UUID, OwnershipState> states = new ConcurrentHashMap<>();

    private HealthDisplayCoordinator() {
    }

    public static void acquire(Player player, Owner owner) {
        if (player == null || owner == null) return;
        states.compute(player.getUniqueId(), (ignored, currentState) -> {
            DisplayBaseline baseline = currentState == null
                    ? new DisplayBaseline(player.isHealthScaled(), player.getHealthScale())
                    : currentState.baseline();
            EnumSet<Owner> updated = currentState == null
                    ? EnumSet.noneOf(Owner.class)
                    : EnumSet.copyOf(currentState.owners());
            updated.add(owner);
            return new OwnershipState(updated, baseline);
        });
        player.setHealthScale(DISPLAY_HEALTH);
        player.setHealthScaled(true);
    }

    public static void release(Player player, Owner owner) {
        if (player == null || owner == null) return;
        OwnershipState current = states.get(player.getUniqueId());
        if (current == null || !current.owners().contains(owner)) return;
        EnumSet<Owner> updated = EnumSet.copyOf(current.owners());
        updated.remove(owner);
        if (!updated.isEmpty()) {
            states.put(player.getUniqueId(), new OwnershipState(updated, current.baseline()));
            return;
        }
        if (!states.remove(player.getUniqueId(), current)) {
            release(player, owner);
            return;
        }
        restore(player, current.baseline());
    }

    public static void clear(Player player) {
        if (player == null) return;
        OwnershipState state = states.remove(player.getUniqueId());
        if (state != null) restore(player, state.baseline());
    }

    /** Restores every online player's pre-EliteMobs display and releases retained ownership. */
    public static void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) clear(player);
        states.clear();
    }

    /**
     * Recovers the ten-heart display after an unclean stop when no in-memory owner survived.
     * This is intentionally narrow and is only called when the matching persisted experimental
     * max-health modifier proves that EliteMobs owned the abandoned display.
     */
    public static void clearStaleExperimentalDisplay(Player player) {
        if (player == null || states.containsKey(player.getUniqueId())) return;
        if (player.isHealthScaled() && Math.abs(player.getHealthScale() - DISPLAY_HEALTH) < 1.0E-6)
            player.setHealthScaled(false);
    }

    private static void restore(Player player, DisplayBaseline baseline) {
        if (baseline.scaled()) {
            player.setHealthScale(baseline.scale());
            player.setHealthScaled(true);
        } else {
            player.setHealthScaled(false);
        }
    }

    private record DisplayBaseline(boolean scaled, double scale) {
    }

    private record OwnershipState(EnumSet<Owner> owners, DisplayBaseline baseline) {
        private OwnershipState {
            owners = EnumSet.copyOf(owners);
        }
    }
}
