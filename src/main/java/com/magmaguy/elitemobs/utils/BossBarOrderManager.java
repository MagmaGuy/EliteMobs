package com.magmaguy.elitemobs.utils;

import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Owns stable ordering and visibility for every non-dialogue boss bar created by EliteMobs.
 * <p>
 * The client stacks boss bars purely in the order their ADD packets arrive, so a bar
 * that gets removed and re-added drops to the bottom of the stack and every bar below
 * it shifts. To keep the stack deterministic, each bar shown through this manager
 * carries a permanent numeric sort key (derive it from the owning boss UUID via
 * {@link #sortKeyFor(UUID)} so it never changes for the lifetime of the boss). When a
 * bar is shown, the manager re-sends it and every bar sorted after it, reproducing the
 * sorted sequence on the client no matter when each bar appeared. Exclusive displays
 * can suspend a player's bars without losing their current desired state.
 */
public class BossBarOrderManager {

    private static final Map<UUID, TreeMap<SortKey, BossBar>> playerBars = new HashMap<>();
    private static final Map<UUID, Set<Suspension>> suspensionsByPlayer = new HashMap<>();

    private BossBarOrderManager() {
    }

    /**
     * Permanent numeric sort key for a bar owned by this UUID. Stays the same for the
     * lifetime of the owner, so its bar always lands in the same slot relative to others.
     */
    public static long sortKeyFor(UUID owner) {
        return owner.getMostSignificantBits();
    }

    /**
     * Shows a bar to a player at its sorted position. Calling this again for a bar the
     * player already sees is a no-op. During a suspension it records the bar without
     * exposing it, so recurring producers do not need their own dialogue checks.
     */
    public static void show(Player player, BossBar bossBar, long sortKey) {
        if (player == null || bossBar == null || !player.isOnline()) return;
        UUID playerUUID = player.getUniqueId();
        TreeMap<SortKey, BossBar> bars = playerBars.computeIfAbsent(playerUUID, key -> new TreeMap<>());
        SortKey key = new SortKey(sortKey, System.identityHashCode(bossBar));
        boolean alreadyRegistered = bars.get(key) == bossBar;
        if (!alreadyRegistered) {
            bars.values().remove(bossBar);
            bars.put(key, bossBar);
        }
        if (suspensionsByPlayer.containsKey(playerUUID)) {
            bossBar.removePlayer(player);
            return;
        }
        if (alreadyRegistered) {
            if (!bossBar.getPlayers().contains(player)) bossBar.addPlayer(player);
            return;
        }
        // Re-send this bar and every bar sorted after it so the client's insertion order
        // matches the sorted order. Bars sorted before it keep their existing slots.
        for (BossBar tailBar : bars.tailMap(key, true).values()) {
            tailBar.removePlayer(player);
            tailBar.addPlayer(player);
        }
    }

    /** Shows a bar whose relative order has no domain-specific key. */
    public static void show(Player player, BossBar bossBar) {
        show(player, bossBar, Integer.toUnsignedLong(System.identityHashCode(bossBar)));
    }

    public static void hide(Player player, BossBar bossBar) {
        if (bossBar == null) return;
        if (player != null) {
            bossBar.removePlayer(player);
            TreeMap<SortKey, BossBar> bars = playerBars.get(player.getUniqueId());
            if (bars != null) {
                bars.values().remove(bossBar);
                if (bars.isEmpty()) playerBars.remove(player.getUniqueId());
            }
        }
    }

    /** Hides managed bars and returns an ownership token that must be released. */
    public static Suspension suspendPlayer(Player player) {
        if (player == null) return null;
        UUID playerUUID = player.getUniqueId();
        Suspension suspension = new Suspension(playerUUID);
        Set<Suspension> suspensions = suspensionsByPlayer.computeIfAbsent(playerUUID, ignored -> new HashSet<>());
        suspensions.add(suspension);
        TreeMap<SortKey, BossBar> bars = playerBars.get(playerUUID);
        if (bars == null) return suspension;
        try {
            for (BossBar bossBar : bars.values()) bossBar.removePlayer(player);
            return suspension;
        } catch (RuntimeException | Error failure) {
            suspensions.remove(suspension);
            if (suspensions.isEmpty()) {
                suspensionsByPlayer.remove(playerUUID);
                for (BossBar bossBar : bars.values()) {
                    try {
                        bossBar.addPlayer(player);
                    } catch (RuntimeException | Error restoreFailure) {
                        failure.addSuppressed(restoreFailure);
                    }
                }
            }
            throw failure;
        }
    }

    /** Releases one suspension; only the final live token restores current bars. */
    public static void resumePlayer(Player player, Suspension suspension) {
        Set<Suspension> suspensions = suspensionsFor(player, suspension);
        if (suspensions == null) return;
        if (suspensions.size() > 1 || !player.isOnline()) {
            removeSuspension(suspension, suspensions);
            return;
        }
        TreeMap<SortKey, BossBar> bars = playerBars.get(suspension.playerUUID);
        if (bars != null) {
            try {
                for (BossBar bossBar : bars.values()) {
                    bossBar.removePlayer(player);
                    bossBar.addPlayer(player);
                }
            } catch (RuntimeException failure) {
                for (BossBar bossBar : bars.values()) {
                    try {
                        bossBar.removePlayer(player);
                    } catch (RuntimeException rollbackFailure) {
                        failure.addSuppressed(rollbackFailure);
                    }
                }
                throw failure;
            }
        }
        removeSuspension(suspension, suspensions);
    }

    /** Releases a terminal suspension without sending any bars back to the player. */
    public static void discardSuspension(Player player, Suspension suspension) {
        Set<Suspension> suspensions = suspensionsFor(player, suspension);
        if (suspensions != null) removeSuspension(suspension, suspensions);
    }

    private static Set<Suspension> suspensionsFor(Player player, Suspension suspension) {
        if (player == null || suspension == null || !player.getUniqueId().equals(suspension.playerUUID)) return null;
        Set<Suspension> suspensions = suspensionsByPlayer.get(suspension.playerUUID);
        if (suspensions == null || !suspensions.contains(suspension)) return null;
        return suspensions;
    }

    private static void removeSuspension(Suspension suspension, Set<Suspension> suspensions) {
        suspensions.remove(suspension);
        if (suspensions.isEmpty()) suspensionsByPlayer.remove(suspension.playerUUID);
    }

    public static void clearPlayer(Player player) {
        if (player == null) return;
        UUID playerUUID = player.getUniqueId();
        TreeMap<SortKey, BossBar> bars = playerBars.remove(playerUUID);
        RuntimeException failure = null;
        try {
            if (bars != null) {
                for (BossBar bossBar : bars.values()) {
                    try {
                        bossBar.removePlayer(player);
                    } catch (RuntimeException exception) {
                        failure = appendFailure(failure, exception);
                    }
                }
            }
        } finally {
            suspensionsByPlayer.remove(playerUUID);
        }
        if (failure != null) throw failure;
    }

    public static void shutdown() {
        Set<BossBar> bars = Collections.newSetFromMap(new IdentityHashMap<>());
        playerBars.values().forEach(playerBossBars -> bars.addAll(playerBossBars.values()));
        RuntimeException failure = null;
        try {
            for (BossBar bossBar : bars) {
                try {
                    bossBar.removeAll();
                } catch (RuntimeException exception) {
                    failure = appendFailure(failure, exception);
                }
            }
        } finally {
            playerBars.clear();
            suspensionsByPlayer.clear();
        }
        if (failure != null) throw failure;
    }

    private static RuntimeException appendFailure(RuntimeException failure, RuntimeException exception) {
        if (failure == null) return exception;
        failure.addSuppressed(exception);
        return failure;
    }

    public static final class Suspension {
        private final UUID playerUUID;

        private Suspension(UUID playerUUID) {
            this.playerUUID = playerUUID;
        }
    }

    public static class BossBarOrderManagerEvents implements Listener {
        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            BossBarOrderManager.clearPlayer(event.getPlayer());
        }
    }

    /**
     * The tiebreak keeps two bars with colliding primary keys (or two bars for the same
     * owner, e.g. a tracking bar and a health bar) from overwriting each other.
     */
    private record SortKey(long primary, int tiebreak) implements Comparable<SortKey> {
        @Override
        public int compareTo(SortKey other) {
            int comparison = Long.compare(primary, other.primary);
            if (comparison != 0) return comparison;
            return Integer.compare(tiebreak, other.tiebreak);
        }
    }
}
