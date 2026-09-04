package com.magmaguy.elitemobs.experimentalcombat.minions;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Per-player consumption state for shared, non-persistent corpse opportunities. */
public final class CorpseOpportunityLedger {
    private final Map<UUID, Entry> entries = new HashMap<>();

    public void offer(UUID corpseId, Set<UUID> eligiblePlayers, long expiresAtTick) {
        Objects.requireNonNull(corpseId, "corpseId");
        Objects.requireNonNull(eligiblePlayers, "eligiblePlayers");
        if (eligiblePlayers.isEmpty()) throw new IllegalArgumentException("A corpse needs an eligible player");
        if (entries.containsKey(corpseId)) throw new IllegalStateException("Corpse already exists: " + corpseId);
        entries.put(corpseId, new Entry(new LinkedHashSet<>(eligiblePlayers), expiresAtTick));
    }

    public boolean availableTo(UUID corpseId, UUID playerId, long currentTick) {
        Entry entry = liveEntry(corpseId, currentTick);
        return entry != null && entry.remainingPlayers().contains(playerId);
    }

    public boolean consume(UUID corpseId, UUID playerId, long currentTick) {
        Objects.requireNonNull(playerId, "playerId");
        Entry entry = liveEntry(corpseId, currentTick);
        if (entry == null || !entry.remainingPlayers().remove(playerId)) return false;
        if (entry.remainingPlayers().isEmpty()) entries.remove(corpseId);
        return true;
    }

    public boolean contains(UUID corpseId) {
        return entries.containsKey(corpseId);
    }

    /** For world/chunk cleanup where the matching transient marker can no longer exist. */
    public boolean discard(UUID corpseId) {
        Objects.requireNonNull(corpseId, "corpseId");
        return entries.remove(corpseId) != null;
    }

    public Set<UUID> expire(long currentTick) {
        LinkedHashSet<UUID> expired = new LinkedHashSet<>();
        entries.entrySet().removeIf(entry -> {
            boolean remove = currentTick >= entry.getValue().expiresAtTick();
            if (remove) expired.add(entry.getKey());
            return remove;
        });
        return Set.copyOf(expired);
    }

    /** Removes one departing player's claims and returns markers that no longer serve anyone. */
    public Set<UUID> removePlayer(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        LinkedHashSet<UUID> empty = new LinkedHashSet<>();
        entries.forEach((corpseId, entry) -> {
            entry.remainingPlayers().remove(playerId);
            if (entry.remainingPlayers().isEmpty()) empty.add(corpseId);
        });
        for (UUID corpseId : empty) entries.remove(corpseId);
        return Set.copyOf(empty);
    }

    private Entry liveEntry(UUID corpseId, long currentTick) {
        Objects.requireNonNull(corpseId, "corpseId");
        Entry entry = entries.get(corpseId);
        if (entry == null) return null;
        if (currentTick < entry.expiresAtTick()) return entry;
        entries.remove(corpseId);
        return null;
    }

    private record Entry(Set<UUID> remainingPlayers, long expiresAtTick) {
    }
}
