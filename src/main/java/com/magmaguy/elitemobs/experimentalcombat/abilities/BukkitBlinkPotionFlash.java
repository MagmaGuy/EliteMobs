package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.experimentalcombat.MonotonicTickClock;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Owns Blink's one-tick sensory flash without clobbering third-party potion state. */
final class BukkitBlinkPotionFlash implements Listener, AutoCloseable {
    private static final Map<FlashType, PotionEffect> FLASHES = flashEffects();

    private final Plugin plugin;
    private final Map<UUID, ActiveFlash> active = new HashMap<>();
    private long nextToken;
    private boolean mutating;
    private boolean closed;

    BukkitBlinkPotionFlash(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    void flash(Player player) {
        Objects.requireNonNull(player, "player");
        if (closed || !player.isOnline()) return;
        UUID playerId = player.getUniqueId();
        ActiveFlash previous = active.get(playerId);
        long token = ++nextToken;
        long startedAt = MonotonicTickClock.currentTick();
        EnumMap<FlashType, EffectLease> leases = new EnumMap<>(FlashType.class);

        mutating = true;
        try {
            for (FlashType type : FlashType.values()) {
                EffectLease priorLease = previous == null ? null : previous.leases().get(type);
                PotionEffect baseline = priorLease != null && !priorLease.externallyChanged()
                        ? priorLease.previous()
                        : player.getPotionEffect(type.bukkitType());
                long baselineStartedAt = priorLease != null && !priorLease.externallyChanged()
                        ? priorLease.baselineStartedAtTick() : startedAt;
                PotionEffect flash = FLASHES.get(type);
                boolean stronger = baseline != null && baseline.getAmplifier() > flash.getAmplifier();
                PotionEffect before = player.getPotionEffect(type.bukkitType());
                boolean installed = !stronger && (player.addPotionEffect(flash, true)
                        || sameIdentity(before, flash)
                        || sameIdentity(player.getPotionEffect(type.bukkitType()), flash));
                leases.put(type, new EffectLease(
                        baseline, installed, false, baselineStartedAt));
            }
        } finally {
            mutating = false;
        }

        active.put(playerId, new ActiveFlash(token, startedAt, leases));
        Bukkit.getScheduler().runTaskLater(plugin, () -> cleanup(playerId, token), 1L);
    }

    void deactivate(Player player) {
        if (player == null) return;
        ActiveFlash current = active.get(player.getUniqueId());
        if (current != null) cleanup(player.getUniqueId(), current.token());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (mutating || !(event.getEntity() instanceof Player player)) return;
        ActiveFlash current = active.get(player.getUniqueId());
        if (current == null) return;
        FlashType type = FlashType.from(event.getModifiedType());
        if (type == null) return;
        EffectLease lease = current.leases().get(type);
        PotionEffect ownedFlash = FLASHES.get(type);
        if (event.getCause() == EntityPotionEffectEvent.Cause.EXPIRATION
                && sameIdentity(event.getOldEffect(), ownedFlash)) return;
        current.leases().put(type, lease.withExternalChange());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        deactivate(event.getPlayer());
    }

    @Override
    public void close() {
        if (closed) return;
        for (Map.Entry<UUID, ActiveFlash> entry : new ArrayList<>(active.entrySet())) {
            cleanup(entry.getKey(), entry.getValue().token());
        }
        active.clear();
        closed = true;
        HandlerList.unregisterAll(this);
    }

    private void cleanup(UUID playerId, long scheduledToken) {
        ActiveFlash currentFlash = active.get(playerId);
        if (currentFlash == null || currentFlash.token() != scheduledToken) return;
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            active.remove(playerId, currentFlash);
            return;
        }

        mutating = true;
        try {
            for (FlashType type : FlashType.values()) {
                EffectLease lease = currentFlash.leases().get(type);
                PotionEffect flash = FLASHES.get(type);
                PotionEffect current = player.getPotionEffect(type.bukkitType());
                BlinkPotionFlashPolicy.CleanupAction action = BlinkPotionFlashPolicy.cleanup(
                        scheduledToken,
                        currentFlash.token(),
                        lease.externallyChanged(),
                        lease.installed(),
                        state(flash),
                        state(current),
                        state(lease.previous()));
                switch (action) {
                    case NOOP -> {
                    }
                    case REMOVE_FLASH -> player.removePotionEffect(type.bukkitType());
                    case RESTORE_PREVIOUS -> {
                        PotionEffect restored = remaining(
                                lease.previous(), MonotonicTickClock.currentTick() - lease.baselineStartedAtTick());
                        if (restored != null) player.addPotionEffect(restored, true);
                        else if (sameIdentity(current, flash)) player.removePotionEffect(type.bukkitType());
                    }
                }
            }
        } finally {
            mutating = false;
            active.remove(playerId, currentFlash);
        }
    }

    private static PotionEffect remaining(PotionEffect effect, long elapsedTicks) {
        if (effect == null) return null;
        int duration = BlinkPotionFlashPolicy.restoredDuration(
                effect.getDuration(), effect.isInfinite(), elapsedTicks);
        if (duration == 0) return null;
        return new PotionEffect(
                effect.getType(), duration, effect.getAmplifier(), effect.isAmbient(),
                effect.hasParticles(), effect.hasIcon());
    }

    private static BlinkPotionFlashPolicy.EffectState state(PotionEffect effect) {
        return effect == null ? null : new BlinkPotionFlashPolicy.EffectState(
                effect.getAmplifier(), effect.getDuration(), effect.isAmbient(),
                effect.hasParticles(), effect.hasIcon());
    }

    private static boolean sameIdentity(PotionEffect first, PotionEffect second) {
        BlinkPotionFlashPolicy.EffectState firstState = state(first);
        BlinkPotionFlashPolicy.EffectState secondState = state(second);
        return firstState != null && firstState.sameIdentity(secondState);
    }

    private static Map<FlashType, PotionEffect> flashEffects() {
        EnumMap<FlashType, PotionEffect> effects = new EnumMap<>(FlashType.class);
        effects.put(FlashType.DARKNESS, new PotionEffect(
                PotionEffectType.DARKNESS, 1, 0, false, false, false));
        effects.put(FlashType.SPEED, new PotionEffect(
                PotionEffectType.SPEED, 1, 2, false, false, false));
        return Map.copyOf(effects);
    }

    private enum FlashType {
        DARKNESS(PotionEffectType.DARKNESS),
        SPEED(PotionEffectType.SPEED);

        private final PotionEffectType bukkitType;

        FlashType(PotionEffectType bukkitType) {
            this.bukkitType = bukkitType;
        }

        PotionEffectType bukkitType() {
            return bukkitType;
        }

        static FlashType from(PotionEffectType type) {
            for (FlashType candidate : values())
                if (candidate.bukkitType.equals(type)) return candidate;
            return null;
        }
    }

    private record ActiveFlash(
            long token,
            long startedAtTick,
            EnumMap<FlashType, EffectLease> leases) {
    }

    private record EffectLease(
            PotionEffect previous,
            boolean installed,
            boolean externallyChanged,
            long baselineStartedAtTick) {
        EffectLease withExternalChange() {
            return new EffectLease(previous, installed, true, baselineStartedAtTick);
        }
    }
}
