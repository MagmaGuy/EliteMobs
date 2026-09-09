package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.experimentalcombat.ClassAbilityEligibility;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Owns Frenzy's short-lived, continuously health-scaled damage and movement bonuses. */
final class FrenzyRuntime implements Listener, AutoCloseable {
    private static final String MOVEMENT_KEY = "experimental_frenzy_speed";

    private final NamespacedKey movementKey;
    private final Map<UUID, ActiveFrenzy> activeByPlayer = new HashMap<>();
    private final BukkitTask refreshTask;
    private boolean closed;

    FrenzyRuntime(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        this.movementKey = new NamespacedKey(plugin, MOVEMENT_KEY);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, this::refreshMovement, 1L, 1L);
    }

    void activate(
            Player player,
            double maximumDamageBonus,
            double maximumSpeedAdjustment,
            int durationTicks,
            String abilityId) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(abilityId, "abilityId");
        if (closed || durationTicks <= 0) return;
        FrenzyScalingPolicy.scale(
                player.getHealth(), maximumHealth(player), maximumDamageBonus, maximumSpeedAdjustment);
        activeByPlayer.put(player.getUniqueId(), new ActiveFrenzy(
                abilityId,
                maximumDamageBonus,
                maximumSpeedAdjustment,
                expiresAt(durationTicks)));
        refreshMovement(player, activeByPlayer.get(player.getUniqueId()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamagesElite(EliteMobDamagedByPlayerEvent event) {
        if (com.magmaguy.elitemobs.combatsystem.CombatDamageContext.isDamageTransferActive()) return;
        if (closed || event.getDamage() <= 0D) return;
        Player player = event.getPlayer();
        ActiveFrenzy frenzy = live(player);
        if (frenzy == null) return;
        FrenzyScalingPolicy.Scaling scaling = scaling(player, frenzy);
        if (scaling.damageMultiplier() > 1D)
            event.applyClassAbilityDamageMultiplier(frenzy.abilityId, scaling.damageMultiplier());
    }

    void clear(Player player) {
        Objects.requireNonNull(player, "player");
        activeByPlayer.remove(player.getUniqueId());
        applyMovement(player, 0D);
    }

    private void refreshMovement() {
        for (UUID playerId : activeByPlayer.keySet().toArray(UUID[]::new)) {
            Player player = Bukkit.getPlayer(playerId);
            ActiveFrenzy frenzy = player == null ? null : live(player);
            if (frenzy == null) {
                activeByPlayer.remove(playerId);
                if (player != null) applyMovement(player, 0D);
                continue;
            }
            refreshMovement(player, frenzy);
        }
    }

    private void refreshMovement(Player player, ActiveFrenzy frenzy) {
        double adjustment = scaling(player, frenzy).movementSpeedAdjustment();
        if (Double.isFinite(frenzy.appliedSpeed)
                && Math.abs(frenzy.appliedSpeed - adjustment) < 1.0E-4D) return;
        applyMovement(player, adjustment);
        frenzy.appliedSpeed = adjustment;
    }

    private ActiveFrenzy live(Player player) {
        if (player == null) return null;
        ActiveFrenzy frenzy = activeByPlayer.get(player.getUniqueId());
        if (frenzy == null) return null;
        if (frenzy.expiresAtNanos <= System.nanoTime()
                || !player.isOnline()
                || !player.isValid()
                || player.isDead()
                || !ClassAbilityEligibility.isEligible(player)) {
            activeByPlayer.remove(player.getUniqueId());
            applyMovement(player, 0D);
            return null;
        }
        return frenzy;
    }

    private static FrenzyScalingPolicy.Scaling scaling(Player player, ActiveFrenzy frenzy) {
        return FrenzyScalingPolicy.scale(
                player.getHealth(),
                maximumHealth(player),
                frenzy.maximumDamageBonus,
                frenzy.maximumSpeedAdjustment);
    }

    private void applyMovement(Player player, double adjustment) {
        AttributeInstance movement = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (movement == null) return;
        movement.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(movementKey))
                .toList()
                .forEach(movement::removeModifier);
        if (adjustment <= 0D) return;
        movement.addModifier(new AttributeModifier(
                movementKey,
                Math.min(.5D, adjustment),
                AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                EquipmentSlotGroup.ANY));
    }

    private static double maximumHealth(Player player) {
        AttributeInstance maximum = player.getAttribute(Attribute.MAX_HEALTH);
        return maximum == null ? Math.max(1D, player.getHealth()) : Math.max(1D, maximum.getValue());
    }

    private static long expiresAt(int durationTicks) {
        long now = System.nanoTime();
        long duration = Math.max(1, durationTicks) * 50_000_000L;
        return Long.MAX_VALUE - now < duration ? Long.MAX_VALUE : now + duration;
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        refreshTask.cancel();
        HandlerList.unregisterAll(this);
        for (UUID playerId : activeByPlayer.keySet().toArray(UUID[]::new)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) applyMovement(player, 0D);
        }
        activeByPlayer.clear();
    }

    private static final class ActiveFrenzy {
        private final String abilityId;
        private final double maximumDamageBonus;
        private final double maximumSpeedAdjustment;
        private final long expiresAtNanos;
        private double appliedSpeed = Double.NaN;

        private ActiveFrenzy(
                String abilityId,
                double maximumDamageBonus,
                double maximumSpeedAdjustment,
                long expiresAtNanos) {
            this.abilityId = abilityId;
            this.maximumDamageBonus = maximumDamageBonus;
            this.maximumSpeedAdjustment = maximumSpeedAdjustment;
            this.expiresAtNanos = expiresAtNanos;
        }
    }
}
