package com.magmaguy.elitemobs.presentation.experience;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Exclusive, client-only ownership of the vanilla experience HUD.
 *
 * <p>The server's real experience values are never mutated. Closing the current lease immediately
 * restores the authoritative Bukkit XP view, and generation tokens prevent stale sessions from
 * clearing a newer owner.</p>
 */
public final class ExperienceBarLease implements AutoCloseable {

    private static final AtomicLong generations = new AtomicLong();
    private static final Map<UUID, RenderState> states = new HashMap<>();

    private final UUID playerId;
    private final String owner;
    private final long generation;
    private boolean closed;

    private ExperienceBarLease(UUID playerId, String owner, long generation) {
        this.playerId = playerId;
        this.owner = owner;
        this.generation = generation;
    }

    public static ExperienceBarLease acquire(Player player, String owner) {
        requireMainThread();
        Objects.requireNonNull(player, "player");
        if (owner == null || owner.isBlank()) throw new IllegalArgumentException("owner must not be blank");
        long generation = generations.incrementAndGet();
        states.put(player.getUniqueId(), new RenderState(owner, generation, 0F, 0));
        ExperienceBarLease lease = new ExperienceBarLease(player.getUniqueId(), owner, generation);
        lease.render(0D, 0);
        return lease;
    }

    public void render(double progress, int levelText) {
        requireMainThread();
        if (closed) return;
        RenderState current = states.get(playerId);
        if (current == null || current.generation() != generation || !current.owner().equals(owner)) return;
        float boundedProgress = (float) Math.max(0D, Math.min(1D, progress));
        int boundedLevel = Math.max(0, levelText);
        states.put(playerId, new RenderState(owner, generation, boundedProgress, boundedLevel));
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) player.sendExperienceChange(boundedProgress, boundedLevel);
    }

    public static void rerender(Player player) {
        requireMainThread();
        RenderState state = states.get(player.getUniqueId());
        if (state == null) {
            restore(player);
            return;
        }
        player.sendExperienceChange(state.progress(), state.levelText());
    }

    public static boolean isLeased(Player player) {
        return states.containsKey(player.getUniqueId());
    }

    public static void discard(Player player) {
        requireMainThread();
        states.remove(player.getUniqueId());
    }

    public static void shutdown() {
        requireMainThread();
        for (UUID playerId : states.keySet().toArray(UUID[]::new)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) restore(player);
        }
        states.clear();
    }

    @Override
    public void close() {
        requireMainThread();
        if (closed) return;
        closed = true;
        RenderState current = states.get(playerId);
        if (current == null || current.generation() != generation || !current.owner().equals(owner)) return;
        states.remove(playerId);
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) restore(player);
    }

    private static void restore(Player player) {
        player.sendExperienceChange(player.getExp(), player.getLevel());
    }

    private static void requireMainThread() {
        if (!Bukkit.isPrimaryThread())
            throw new IllegalStateException("Experience HUD leases must be used on the server thread.");
    }

    private record RenderState(String owner, long generation, float progress, int levelText) {
    }
}
