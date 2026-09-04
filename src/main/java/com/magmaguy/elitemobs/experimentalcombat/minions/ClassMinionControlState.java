package com.magmaguy.elitemobs.experimentalcombat.minions;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Main-thread transient owner and combat commands captured by one native Mind program. */
final class ClassMinionControlState {
    private static final double MAX_COMBAT_DISTANCE_SQUARED = 32D * 32D;

    private final UUID playerOwnerId;
    private final int attackPeriodTicks;
    private UUID targetId;
    private long targetExpiresAtNanos;

    ClassMinionControlState(UUID playerOwnerId, int attackPeriodTicks) {
        this.playerOwnerId = Objects.requireNonNull(playerOwnerId, "playerOwnerId");
        if (attackPeriodTicks < 1) throw new IllegalArgumentException("attackPeriodTicks must be positive");
        this.attackPeriodTicks = attackPeriodTicks;
    }

    UUID playerOwnerId() {
        return playerOwnerId;
    }

    int attackPeriodTicks() {
        return attackPeriodTicks;
    }

    Optional<Player> owner() {
        Player owner = Bukkit.getPlayer(playerOwnerId);
        return owner != null && owner.isOnline() && owner.isValid() && !owner.isDead()
                ? Optional.of(owner)
                : Optional.empty();
    }

    void commandTarget(LivingEntity target, int durationTicks) {
        Objects.requireNonNull(target, "target");
        if (durationTicks < 1) throw new IllegalArgumentException("durationTicks must be positive");
        targetId = target.getUniqueId();
        long duration = durationTicks * 50_000_000L;
        long now = System.nanoTime();
        targetExpiresAtNanos = Long.MAX_VALUE - now < duration ? Long.MAX_VALUE : now + duration;
    }

    Optional<LivingEntity> target(LivingEntity minion) {
        if (targetId == null || System.nanoTime() >= targetExpiresAtNanos) {
            clearTarget();
            return Optional.empty();
        }
        Entity resolved = Bukkit.getEntity(targetId);
        Player owner = owner().orElse(null);
        if (!(resolved instanceof LivingEntity target)
                || target instanceof Player
                || owner == null
                || target.isDead()
                || !target.isValid()
                || !target.getWorld().equals(minion.getWorld())
                || !owner.getWorld().equals(minion.getWorld())
                || target.getUniqueId().equals(minion.getUniqueId())
                || target.getLocation().distanceSquared(owner.getLocation()) > MAX_COMBAT_DISTANCE_SQUARED) {
            clearTarget();
            return Optional.empty();
        }
        return Optional.of(target);
    }

    double ownerDistance(LivingEntity minion) {
        Player owner = owner().orElse(null);
        if (owner == null || !sameWorld(owner.getWorld(), minion.getWorld())) {
            return Double.POSITIVE_INFINITY;
        }
        return Math.sqrt(owner.getLocation().distanceSquared(minion.getLocation()));
    }

    void clearTarget() {
        targetId = null;
        targetExpiresAtNanos = 0L;
    }

    private static boolean sameWorld(World first, World second) {
        return first.getUID().equals(second.getUID());
    }
}
