package com.magmaguy.elitemobs.advancedcombat.menu;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * Issues short-lived, single-use actions which are bound to one player.
 *
 * <p>The client receives only an opaque token. Form identifiers and mutations are resolved on
 * the server after the token is consumed, so stale dialogs cannot smuggle arbitrary class ids.</p>
 */
final class PlayerActionTokenRegistry {
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(3);

    private final long ttlMillis;
    private final LongSupplier clock;
    private final Map<String, Entry> entries = new HashMap<>();
    private final Map<UUID, Set<String>> playerTokens = new HashMap<>();

    PlayerActionTokenRegistry() {
        this(DEFAULT_TTL, System::currentTimeMillis);
    }

    PlayerActionTokenRegistry(Duration ttl, LongSupplier clock) {
        Objects.requireNonNull(ttl, "ttl");
        if (ttl.isZero() || ttl.isNegative()) throw new IllegalArgumentException("ttl must be positive");
        this.ttlMillis = ttl.toMillis();
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    synchronized void beginPage(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        invalidate(playerId);
        removeExpired(clock.getAsLong());
    }

    synchronized String issue(UUID playerId, ClassMenuAction action) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(action, "action");
        long now = clock.getAsLong();
        removeExpired(now);
        String token;
        do token = UUID.randomUUID().toString().replace("-", "");
        while (entries.containsKey(token));
        entries.put(token, new Entry(playerId, action, now + ttlMillis));
        playerTokens.computeIfAbsent(playerId, ignored -> new HashSet<>()).add(token);
        return token;
    }

    synchronized Optional<ClassMenuAction> consume(UUID playerId, String token) {
        Objects.requireNonNull(playerId, "playerId");
        if (token == null || token.isBlank()) return Optional.empty();
        Entry entry = entries.get(token);
        if (entry == null) return Optional.empty();
        if (entry.expiresAtMillis() < clock.getAsLong()) {
            remove(token);
            return Optional.empty();
        }
        if (!entry.playerId().equals(playerId)) return Optional.empty();
        remove(token);
        return Optional.of(entry.action());
    }

    synchronized void invalidate(UUID playerId) {
        Set<String> tokens = playerTokens.remove(playerId);
        if (tokens == null) return;
        for (String token : tokens) entries.remove(token);
    }

    synchronized void clear() {
        entries.clear();
        playerTokens.clear();
    }

    synchronized int size() {
        removeExpired(clock.getAsLong());
        return entries.size();
    }

    private Entry remove(String token) {
        Entry entry = entries.remove(token);
        if (entry == null) return null;
        Set<String> tokens = playerTokens.get(entry.playerId());
        if (tokens != null) {
            tokens.remove(token);
            if (tokens.isEmpty()) playerTokens.remove(entry.playerId());
        }
        return entry;
    }

    private void removeExpired(long now) {
        for (String token : Set.copyOf(entries.keySet())) {
            Entry entry = entries.get(token);
            if (entry != null && entry.expiresAtMillis() < now) remove(token);
        }
    }

    private record Entry(UUID playerId, ClassMenuAction action, long expiresAtMillis) {
    }
}
