package com.magmaguy.elitemobs.mobconstructor.custombosses;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

final class PlayerTaskRegistry {
    enum Role {
        PLAYBACK_LOOP,
        DELAYED_START
    }

    private final Map<UUID, EnumMap<Role, Runnable>> cancellationsByPlayer = new HashMap<>();

    void replace(UUID playerId, Role role, Runnable cancellation) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(cancellation, "cancellation");
        cancel(playerId, role);
        cancellationsByPlayer
                .computeIfAbsent(playerId, ignored -> new EnumMap<>(Role.class))
                .put(role, cancellation);
    }

    void cancel(UUID playerId, Role role) {
        EnumMap<Role, Runnable> playerTasks = cancellationsByPlayer.get(playerId);
        if (playerTasks == null) return;
        Runnable cancellation = playerTasks.remove(role);
        if (playerTasks.isEmpty()) cancellationsByPlayer.remove(playerId);
        if (cancellation != null) cancellation.run();
    }

    void release(UUID playerId, Role role) {
        EnumMap<Role, Runnable> playerTasks = cancellationsByPlayer.get(playerId);
        if (playerTasks == null) return;
        playerTasks.remove(role);
        if (playerTasks.isEmpty()) cancellationsByPlayer.remove(playerId);
    }

    void cancelPlayer(UUID playerId) {
        EnumMap<Role, Runnable> playerTasks = cancellationsByPlayer.remove(playerId);
        if (playerTasks == null) return;
        new ArrayList<>(playerTasks.values()).forEach(Runnable::run);
    }

    void cancelAll() {
        ArrayList<Runnable> cancellations = new ArrayList<>();
        cancellationsByPlayer.values().forEach(tasks -> cancellations.addAll(tasks.values()));
        cancellationsByPlayer.clear();
        cancellations.forEach(Runnable::run);
    }

    boolean contains(UUID playerId, Role role) {
        EnumMap<Role, Runnable> playerTasks = cancellationsByPlayer.get(playerId);
        return playerTasks != null && playerTasks.containsKey(role);
    }

    Set<UUID> playerIds() {
        return new HashSet<>(cancellationsByPlayer.keySet());
    }

    Set<UUID> playerIds(Role role) {
        Set<UUID> playerIds = new HashSet<>();
        for (Map.Entry<UUID, EnumMap<Role, Runnable>> entry : cancellationsByPlayer.entrySet())
            if (entry.getValue().containsKey(role)) playerIds.add(entry.getKey());
        return playerIds;
    }

    boolean isEmpty() {
        return cancellationsByPlayer.isEmpty();
    }
}
