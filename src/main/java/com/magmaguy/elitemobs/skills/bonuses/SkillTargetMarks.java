package com.magmaguy.elitemobs.skills.bonuses;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Target marks with one expiry per current mark and direct cleanup by the applying player. */
public final class SkillTargetMarks {
    private final Map<UUID, Mark> byTarget = new HashMap<>();
    private final Map<UUID, Set<UUID>> targetsBySource = new HashMap<>();

    public void mark(UUID source, UUID target, long durationMillis) {
        Mark next = new Mark(source, target, System.nanoTime() + durationMillis * 1_000_000L);
        next.runTaskLater(MetadataHandler.PLUGIN, Math.max(1L, (durationMillis + 49L) / 50L));
        Mark previous = byTarget.put(target, next);
        if (previous != null) retire(previous);
        targetsBySource.computeIfAbsent(source, ignored -> new HashSet<>()).add(target);
    }

    public boolean isMarkedBy(UUID target, UUID source) {
        Mark mark = byTarget.get(target);
        if (mark == null) return false;
        if (System.nanoTime() >= mark.expiresAtNanos) {
            retire(mark);
            return false;
        }
        return mark.source.equals(source);
    }

    public void clearSource(UUID source) {
        Set<UUID> targets = targetsBySource.get(source);
        if (targets == null) return;
        for (UUID target : List.copyOf(targets)) {
            Mark mark = byTarget.get(target);
            if (mark != null && mark.source.equals(source)) retire(mark);
        }
    }

    public void clear() {
        byTarget.values().forEach(BukkitRunnable::cancel);
        byTarget.clear();
        targetsBySource.clear();
    }

    private void retire(Mark mark) {
        byTarget.remove(mark.target, mark);
        mark.cancel();
        Set<UUID> targets = targetsBySource.get(mark.source);
        if (targets != null) {
            targets.remove(mark.target);
            if (targets.isEmpty()) targetsBySource.remove(mark.source);
        }
    }

    private final class Mark extends BukkitRunnable {
        private final UUID source;
        private final UUID target;
        private final long expiresAtNanos;

        private Mark(UUID source, UUID target, long expiresAtNanos) {
            this.source = source;
            this.target = target;
            this.expiresAtNanos = expiresAtNanos;
        }

        @Override
        public void run() {
            if (byTarget.get(target) == this) retire(this);
        }
    }
}
