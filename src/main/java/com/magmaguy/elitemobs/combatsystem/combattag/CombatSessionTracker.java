package com.magmaguy.elitemobs.combatsystem.combattag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class CombatSessionTracker {

    enum DamageResult {
        STARTED_COMBAT,
        REFRESHED_COMBAT
    }

    private final long timeoutTicks;
    private final Map<UUID, Long> combatDeadlines = new HashMap<>();

    CombatSessionTracker(long timeoutTicks) {
        if (timeoutTicks < 1L)
            throw new IllegalArgumentException("Combat timeout must be at least one tick.");
        this.timeoutTicks = timeoutTicks;
    }

    DamageResult recordDamage(UUID playerId, long currentTick) {
        Long previousDeadline = combatDeadlines.put(playerId, currentTick + timeoutTicks);
        return previousDeadline == null || previousDeadline <= currentTick
                ? DamageResult.STARTED_COMBAT
                : DamageResult.REFRESHED_COMBAT;
    }

    List<UUID> expire(long currentTick) {
        List<UUID> expiredPlayers = new ArrayList<>();
        combatDeadlines.entrySet().removeIf(entry -> {
            if (entry.getValue() > currentTick) return false;
            expiredPlayers.add(entry.getKey());
            return true;
        });
        return expiredPlayers;
    }

    boolean isInCombat(UUID playerId) {
        return combatDeadlines.containsKey(playerId);
    }

}
