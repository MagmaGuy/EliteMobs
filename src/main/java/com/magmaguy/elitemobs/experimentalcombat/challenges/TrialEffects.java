package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.*;

import java.util.*;

/** Short, owned trial effects. Cleanup never removes a stronger or subsequently replaced effect. */
final class TrialEffects implements Listener, AutoCloseable {
    private record Key(LivingEntity entity, PotionEffectType type) {}
    private record Applied(PotionEffect effect, PotionEffect previous, long start) {}
    private final Map<Key, Applied> applied = new HashMap<>();
    private final Map<LivingEntity, Long> protection = new HashMap<>();
    private long tick;
    private static final Set<PotionEffectType> HARMFUL = Set.of(PotionEffectType.SLOWNESS,
            PotionEffectType.WEAKNESS, PotionEffectType.POISON, PotionEffectType.WITHER,
            PotionEffectType.BLINDNESS, PotionEffectType.DARKNESS, PotionEffectType.MINING_FATIGUE,
            PotionEffectType.NAUSEA, PotionEffectType.HUNGER, PotionEffectType.LEVITATION);

    TrialEffects() { Bukkit.getPluginManager().registerEvents(this, MetadataHandler.PLUGIN); }

    void tick(long now) {
        tick = now;
        protection.entrySet().removeIf(entry -> !entry.getKey().isValid() || now >= entry.getValue());
        applied.entrySet().removeIf(entry -> !entry.getKey().entity.isValid()
                || now >= entry.getValue().start + entry.getValue().effect.getDuration());
    }

    void apply(LivingEntity entity, PotionEffectType type, int duration, int amplifier) {
        if (duration < 1 || duration > 240 || amplifier < 0 || amplifier > 2)
            throw new IllegalArgumentException("Invalid trial status strength or duration");
        PotionEffect before = entity.getPotionEffect(type);
        PotionEffect effect = new PotionEffect(type, duration, amplifier, false, true, true);
        if (before != null && (before.getAmplifier() > amplifier
                || (before.getAmplifier() == amplifier && before.getDuration() >= duration))) return;
        if (entity.addPotionEffect(effect)) applied.put(new Key(entity, type), new Applied(effect, before, tick));
    }

    void cleanse(LivingEntity entity, boolean slowOnly, int protectTicks) {
        for (PotionEffect effect : entity.getActivePotionEffects())
            if (slowOnly ? effect.getType() == PotionEffectType.SLOWNESS : HARMFUL.contains(effect.getType()))
                entity.removePotionEffect(effect.getType());
        if (protectTicks > 0) protection.put(entity, tick + Math.min(240, protectTicks));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void potion(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (event.getNewEffect() != null && HARMFUL.contains(event.getNewEffect().getType())
                && protection.getOrDefault(event.getEntity(), 0L) > tick) event.setCancelled(true);
        // A subsequent plugin or player application owns its replacement, even when values happen to match.
        if (event.getAction() == EntityPotionEffectEvent.Action.CHANGED
                || event.getAction() == EntityPotionEffectEvent.Action.REMOVED
                || event.getAction() == EntityPotionEffectEvent.Action.CLEARED)
            applied.remove(new Key(living, event.getModifiedType()));
    }

    @Override public void close() {
        HandlerList.unregisterAll(this);
        for (var entry : applied.entrySet()) {
            Key key = entry.getKey(); Applied owned = entry.getValue();
            PotionEffect current = key.entity.getPotionEffect(key.type);
            int elapsed = (int) (tick - owned.start);
            if (current == null || current.getAmplifier() != owned.effect.getAmplifier()
                    || Math.abs(current.getDuration() - (owned.effect.getDuration() - elapsed)) > 2) continue;
            key.entity.removePotionEffect(key.type);
            if (owned.previous != null && owned.previous.getDuration() > elapsed)
                key.entity.addPotionEffect(new PotionEffect(key.type, owned.previous.getDuration() - elapsed,
                        owned.previous.getAmplifier(), owned.previous.isAmbient(), owned.previous.hasParticles(), owned.previous.hasIcon()));
        }
        applied.clear(); protection.clear();
    }
}
