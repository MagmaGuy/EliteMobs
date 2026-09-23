package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.utils.GameClock;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/** Confirms the native event and resulting state, including effects deferred during entity ticking. */
final class ObservedPotionApplications implements Listener, AutoCloseable {
    private final Plugin plugin;
    private final Map<Key, Pending> pending = new HashMap<>();
    private boolean closed;

    ObservedPotionApplications(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    boolean apply(UUID source, LivingEntity target, PotionEffect effect,
                  Consumer<PotionEffect> accepted, Runnable unconfirmed) {
        if (closed) return false;
        Key key = new Key(target.getUniqueId(), effect.getType());
        Pending previous = pending.get(key);
        if (previous != null) finish(previous, false, null);
        Pending request = new Pending(key, source, target, effect, accepted, unconfirmed);
        pending.put(key, request);
        try {
            target.addPotionEffect(effect);
            PotionEffect actual = target.getPotionEffect(effect.getType());
            if (confirmed(request, actual)) {
                finish(request, true, actual);
                return true;
            }
            if (request.ambiguous || request.event != null && request.event.isCancelled()) {
                finish(request, false, null);
                return false;
            }
            request.task = Bukkit.getScheduler().runTask(plugin, () -> {
                if (pending.get(key) != request) return;
                PotionEffect observed = target.isValid() && !target.isDead()
                        ? target.getPotionEffect(effect.getType()) : null;
                finish(request, confirmed(request, observed), observed);
            });
            return false;
        } catch (RuntimeException failure) {
            pending.remove(key, request);
            if (request.task != null) request.task.cancel();
            throw failure;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPotionChange(EntityPotionEffectEvent event) {
        if (pending.isEmpty()) return;
        Pending request = pending.get(new Key(event.getEntity().getUniqueId(), event.getModifiedType()));
        if (request == null) return;
        if (request.event != null || event.getCause() != EntityPotionEffectEvent.Cause.PLUGIN
                || !request.effect.equals(event.getNewEffect())) request.ambiguous = true;
        request.event = event;
    }

    private static boolean confirmed(Pending request, PotionEffect actual) {
        if (request.ambiguous || request.event == null || request.event.isCancelled()
                || request.event.getOldEffect() != null && !request.event.isOverride() || actual == null) return false;
        long remaining = request.effect.getDuration() - (GameClock.getCurrentTick() - request.startedTick);
        return actual.getAmplifier() == request.effect.getAmplifier()
                && actual.isAmbient() == request.effect.isAmbient()
                && actual.hasParticles() == request.effect.hasParticles()
                && actual.hasIcon() == request.effect.hasIcon()
                && Math.abs(actual.getDuration() - remaining) <= 1;
    }

    private void finish(Pending request, boolean applied, PotionEffect actual) {
        if (!pending.remove(request.key, request)) return;
        if (request.task != null) request.task.cancel();
        if (applied) request.accepted.accept(actual);
        else request.unconfirmed.run();
    }

    void clearSource(UUID source) {
        for (Pending request : List.copyOf(pending.values()))
            if (request.source.equals(source)) finish(request, false, null);
    }

    @Override
    public void close() {
        closed = true;
        for (Pending request : List.copyOf(pending.values())) finish(request, false, null);
        HandlerList.unregisterAll(this);
    }

    private record Key(UUID target, PotionEffectType type) { }

    private static final class Pending {
        private final Key key;
        private final UUID source;
        private final PotionEffect effect;
        private final Consumer<PotionEffect> accepted;
        private final Runnable unconfirmed;
        private final long startedTick = GameClock.getCurrentTick();
        private EntityPotionEffectEvent event;
        private boolean ambiguous;
        private BukkitTask task;

        private Pending(Key key, UUID source, LivingEntity target, PotionEffect effect,
                        Consumer<PotionEffect> accepted, Runnable unconfirmed) {
            this.key = key;
            this.source = source;
            this.effect = effect;
            this.accepted = accepted;
            this.unconfirmed = unconfirmed;
        }
    }
}
