package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.attribute.*;
import org.bukkit.potion.*;

import java.util.*;

/** Short, owned trial effects. Cleanup never removes a stronger or subsequently replaced effect. */
final class TrialEffects implements Listener, AutoCloseable {
    private record Key(LivingEntity entity, PotionEffectType type) {}
    private record Applied(PotionEffect effect, PotionEffect previous, long start) {}
    private final Map<Key, Applied> applied = new HashMap<>();
    private final Map<LivingEntity, Long> protection = new HashMap<>();
    private record Slow(double fraction, long expires) {}
    private final Map<LivingEntity, Slow> slows = new HashMap<>();
    private final org.bukkit.NamespacedKey slowKey = new org.bukkit.NamespacedKey(MetadataHandler.PLUGIN, "trial_slow");
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
        slows.entrySet().removeIf(entry -> {
            if (entry.getKey().isValid() && now < entry.getValue().expires) return false;
            clearSlow(entry.getKey()); return true;
        });
    }

    void slow(LivingEntity entity, double fraction, int duration) {
        if (!Double.isFinite(fraction) || fraction<=0 || fraction>1 || duration<1 || duration>120)
            throw new IllegalArgumentException("Invalid trial movement effect");
        AttributeInstance movement=entity.getAttribute(Attribute.MOVEMENT_SPEED);
        if (movement==null) return;
        Slow previous=slows.get(entity);
        if (previous!=null && previous.fraction>fraction && previous.expires>tick) return;
        clearSlow(entity);
        movement.addModifier(new AttributeModifier(slowKey,-fraction,AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                org.bukkit.inventory.EquipmentSlotGroup.ANY));
        slows.put(entity,new Slow(fraction,tick+duration));
    }

    void clearMovement(LivingEntity entity) {
        slows.remove(entity);
        clearSlow(entity);
    }

    private void clearSlow(LivingEntity entity) {
        AttributeInstance movement=entity.getAttribute(Attribute.MOVEMENT_SPEED);
        if (movement!=null) for (AttributeModifier modifier : movement.getModifiers())
            if (modifier.getKey().equals(slowKey)) movement.removeModifier(modifier);
    }

    @EventHandler(priority=EventPriority.HIGH,ignoreCancelled=true)
    public void rootedMovement(PlayerMoveEvent event) {
        Slow slow=slows.get(event.getPlayer());
        if (event instanceof PlayerTeleportEvent || slow==null || slow.fraction<1 || tick>=slow.expires || event.getTo()==null) return;
        // A half-second snare stops horizontal travel while preserving look direction and vertical physics.
        var destination=event.getTo().clone(); destination.setX(event.getFrom().getX()); destination.setZ(event.getFrom().getZ());
        event.setTo(destination);
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
        slows.keySet().forEach(this::clearSlow); slows.clear();
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
