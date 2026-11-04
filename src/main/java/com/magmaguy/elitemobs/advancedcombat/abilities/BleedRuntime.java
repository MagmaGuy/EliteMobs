package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.advancedcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.DoubleConsumer;

/**
 * Source-scoped damage over time. Each bleed deals {@link #PULSES} pulses, one per second, each
 * worth {@link #PULSE_SHARE} of the hit that applied it. A new bleed from the same caster replaces
 * the old one on that target instead of stacking.
 */
final class BleedRuntime implements AutoCloseable {
    static final int PULSES = 3;
    static final int PERIOD_TICKS = 20;
    static final double PULSE_SHARE = .15D;
    private static final CombatDamageContext.ClassAbilityDamageDomain DOMAIN =
            new CombatDamageContext.ClassAbilityDamageDomain(
                    CombatDamageContext.ClassAbilityTargetShape.SINGLE_TARGET,
                    CombatDamageContext.ClassAbilityDelivery.PERIODIC,
                    CombatDamageContext.ClassAbilityArchetype.OTHER,
                    CombatDamageContext.ClassAbilityStrikeQuality.NORMAL);

    private final Plugin plugin;
    private final ClassAbilityDamage damage;
    private final Map<UUID, Map<UUID, BukkitTask>> bleeds = new HashMap<>();
    private boolean closed;

    BleedRuntime(Plugin plugin, ClassAbilityDamage damage) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.damage = Objects.requireNonNull(damage, "damage");
    }

    /** Starts a bleed worth a share of {@code hitAmount}; returns whether it started. */
    boolean bleed(Player caster, LivingEntity target, double hitAmount, DoubleConsumer onPulseDealt) {
        if (closed || caster == null || target == null || !Double.isFinite(hitAmount) || hitAmount <= 0D)
            return false;
        double pulseAmount = hitAmount * PULSE_SHARE;
        UUID casterId = caster.getUniqueId();
        UUID targetId = target.getUniqueId();
        cancel(targetId, casterId);
        BukkitTask task = new org.bukkit.scheduler.BukkitRunnable() {
            private int pulses;

            @Override
            public void run() {
                Player source = Bukkit.getPlayer(casterId);
                if (pulses >= PULSES || source == null || !source.isOnline() || source.isDead()
                        || !ClassAbilityEligibility.isEligible(source)
                        || !target.isValid() || target.isDead()
                        || EntityTracker.getEliteMobEntity(target) == null) {
                    finish(targetId, casterId, this);
                    return;
                }
                pulses++;
                double dealt = damage.apply(source, null, target, pulseAmount, DOMAIN);
                Location center = target.getLocation().add(0D, target.getHeight() * .6D, 0D);
                if (center.getWorld() != null) {
                    center.getWorld().spawnParticle(Particle.BLOCK, center, 10,
                            target.getWidth() * .35D, target.getHeight() * .25D, target.getWidth() * .35D,
                            0D, Material.REDSTONE_BLOCK.createBlockData());
                    center.getWorld().playSound(center, Sound.BLOCK_HONEY_BLOCK_SLIDE, .45F, .7F);
                }
                if (dealt > 0D && onPulseDealt != null) onPulseDealt.accept(dealt);
                if (pulses >= PULSES) finish(targetId, casterId, this);
            }
        }.runTaskTimer(plugin, PERIOD_TICKS, PERIOD_TICKS);
        bleeds.computeIfAbsent(targetId, ignored -> new HashMap<>()).put(casterId, task);
        return true;
    }

    void clearSource(UUID sourceId) {
        for (UUID targetId : bleeds.keySet().toArray(UUID[]::new)) cancel(targetId, sourceId);
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        bleeds.values().forEach(tasks -> tasks.values().forEach(BukkitTask::cancel));
        bleeds.clear();
    }

    private void cancel(UUID targetId, UUID casterId) {
        Map<UUID, BukkitTask> tasks = bleeds.get(targetId);
        if (tasks == null) return;
        BukkitTask previous = tasks.remove(casterId);
        if (previous != null) previous.cancel();
        if (tasks.isEmpty()) bleeds.remove(targetId);
    }

    private void finish(UUID targetId, UUID casterId, org.bukkit.scheduler.BukkitRunnable runnable) {
        runnable.cancel();
        Map<UUID, BukkitTask> tasks = bleeds.get(targetId);
        if (tasks == null) return;
        BukkitTask current = tasks.get(casterId);
        if (current != null && current.getTaskId() == runnable.getTaskId()) tasks.remove(casterId);
        if (tasks.isEmpty()) bleeds.remove(targetId);
    }
}
