package com.magmaguy.elitemobs.utils;

import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Keeps the boss bars EliteMobs shows to a player stacked in a stable order.
 * <p>
 * The client stacks boss bars purely in the order their ADD packets arrive, so a bar
 * that gets removed and re-added drops to the bottom of the stack and every bar below
 * it shifts. To keep the stack deterministic, each bar shown through this manager
 * carries a permanent numeric sort key (derive it from the owning boss UUID via
 * {@link #sortKeyFor(UUID)} so it never changes for the lifetime of the boss). When a
 * bar is shown, the manager re-sends it and every bar sorted after it, reproducing the
 * sorted sequence on the client no matter when each bar appeared.
 */
public class BossBarOrderManager {

    private static final Map<UUID, TreeMap<SortKey, BossBar>> playerBars = new HashMap<>();

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
     * player already sees is a no-op, so callers don't need their own dedup guards.
     */
    public static void show(Player player, BossBar bossBar, long sortKey) {
        if (player == null || bossBar == null || !player.isOnline()) return;
        TreeMap<SortKey, BossBar> bars = playerBars.computeIfAbsent(player.getUniqueId(), key -> new TreeMap<>());
        SortKey key = new SortKey(sortKey, System.identityHashCode(bossBar));
        if (bars.get(key) == bossBar) return;
        bars.values().remove(bossBar);
        bars.put(key, bossBar);
        // Re-send this bar and every bar sorted after it so the client's insertion order
        // matches the sorted order. Bars sorted before it keep their existing slots.
        for (BossBar tailBar : bars.tailMap(key, true).values()) {
            tailBar.removePlayer(player);
            tailBar.addPlayer(player);
        }
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

    public static void clearPlayer(UUID playerUUID) {
        playerBars.remove(playerUUID);
    }

    public static void shutdown() {
        playerBars.clear();
    }

    public static class BossBarOrderManagerEvents implements Listener {
        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            BossBarOrderManager.clearPlayer(event.getPlayer().getUniqueId());
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
