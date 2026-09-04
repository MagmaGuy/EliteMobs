package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.easyminecraftgoals.NMSAdapter;
import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.TransientMovementOverride;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.experimentalcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.ElitePowerPauseReason;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Owns source-scoped Elite control leases without changing permanent AI state. */
final class EliteCrowdControlRuntime implements Listener, AutoCloseable {
    private static final double FEAR_SPEED_MODIFIER = 1.2D;
    private static final String SLOW_POTENCY_KEY = "experimental_control_potency";

    private final FearLeaseRegistry<UUID, Location> fearLeases = new FearLeaseRegistry<>();
    private final TimedSuppressionLeaseRegistry<UUID> interruptLeases =
            new TimedSuppressionLeaseRegistry<>();
    private final RootLeaseRegistry<UUID, UUID> rootLeases = new RootLeaseRegistry<>();
    private final Map<UUID, Map<UUID, SlowPotencyLease>> slowPotencyLeases = new HashMap<>();
    private final Map<UUID, Double> appliedSlowAdjustments = new HashMap<>();
    private final NamespacedKey slowPotencyKey;
    private final BukkitTask tickTask;
    private long currentTick;
    private boolean closed;

    EliteCrowdControlRuntime(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        slowPotencyKey = new NamespacedKey(plugin, SLOW_POTENCY_KEY);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    boolean fear(Player caster, EliteEntity elite, int durationTicks) {
        if (closed || caster == null || elite == null || durationTicks <= 0) return false;
        LivingEntity target = elite.getLivingEntity();
        if (!validPlayer(caster) || !valid(target) || caster.getWorld() != target.getWorld()) return false;
        NMSAdapter adapter = NMSManager.getAdapter();
        if (adapter == null) return false;

        Location threat = caster.getLocation().clone();
        long expiresAt = saturatedAdd(currentTick, durationTicks);
        return fearLeases.apply(
                target.getUniqueId(),
                caster.getUniqueId(),
                threat,
                expiresAt,
                () -> open(adapter, target, threat));
    }

    boolean interrupt(EliteEntity elite, int durationTicks) {
        if (closed || elite == null || durationTicks <= 0) return false;
        LivingEntity target = elite.getLivingEntity();
        if (!valid(target)) return false;
        long expiresAt = saturatedAdd(currentTick, durationTicks);
        return interruptLeases.apply(
                target.getUniqueId(),
                expiresAt,
                () -> elite.getPowerSuppression().acquire(ElitePowerPauseReason.INTERRUPT));
    }

    boolean root(Player caster, EliteEntity elite, int durationTicks) {
        if (closed || caster == null || elite == null || durationTicks <= 0) return false;
        LivingEntity target = elite.getLivingEntity();
        if (!validPlayer(caster) || !valid(target)) return false;
        long expiresAt = saturatedAdd(currentTick, durationTicks);
        if (!rootLeases.apply(
                target.getUniqueId(),
                caster.getUniqueId(),
                expiresAt,
                () -> elite.getPowerSuppression().acquire(ElitePowerPauseReason.ROOT))) return false;
        immobilize(target);
        return true;
    }

    boolean extendRoot(Player caster, EliteEntity elite, int additionalTicks) {
        if (closed || caster == null || elite == null || additionalTicks <= 0) return false;
        LivingEntity target = elite.getLivingEntity();
        return validPlayer(caster) && valid(target) && rootLeases.extend(
                target.getUniqueId(), caster.getUniqueId(), additionalTicks, currentTick);
    }

    boolean controlledBy(Player caster, LivingEntity target) {
        if (closed || caster == null || target == null) return false;
        UUID targetId = target.getUniqueId();
        UUID casterId = caster.getUniqueId();
        return rootLeases.ownedBy(targetId, casterId)
                || fearLeases.ownedBy(targetId, casterId);
    }

    void applySlowPotency(
            Player caster,
            EliteEntity elite,
            EliteControlEffectPlan plan) {
        if (closed || caster == null || elite == null || plan == null
                || plan.durationTicks() <= 0
                || plan.additionalMovementSpeedAdjustment() >= 0D) return;
        LivingEntity target = elite.getLivingEntity();
        if (!validPlayer(caster) || !valid(target)) return;
        slowPotencyLeases.computeIfAbsent(target.getUniqueId(), ignored -> new HashMap<>())
                .put(caster.getUniqueId(), new SlowPotencyLease(
                        saturatedAdd(currentTick, plan.durationTicks()),
                        plan.potionAmplifier(),
                        plan.additionalMovementSpeedAdjustment()));
        refreshSlowPotency(target);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEliteRemoved(EliteMobRemoveEvent event) {
        Entity entity = event.getEntity();
        if (entity != null) {
            fearLeases.remove(entity.getUniqueId());
            interruptLeases.remove(entity.getUniqueId());
            rootLeases.remove(entity.getUniqueId());
            clearSlowPotency(entity.getUniqueId(), entity instanceof LivingEntity living ? living : null);
        }
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        tickTask.cancel();
        HandlerList.unregisterAll(this);
        fearLeases.close();
        interruptLeases.close();
        rootLeases.close();
        for (UUID targetId : slowPotencyLeases.keySet().toArray(UUID[]::new)) {
            Entity entity = Bukkit.getEntity(targetId);
            clearSlowPotency(targetId, entity instanceof LivingEntity living ? living : null);
        }
        slowPotencyLeases.clear();
        appliedSlowAdjustments.clear();
    }

    private void tick() {
        if (closed) return;
        currentTick = saturatedAdd(currentTick, 1L);
        fearLeases.maintain(currentTick, this::validElite, this::currentThreat);
        interruptLeases.maintain(currentTick, this::validElite);
        rootLeases.maintain(currentTick, this::validElite);
        maintainSlowPotency();
        for (UUID entityId : rootLeases.activeTargets()) {
            Entity entity = Bukkit.getEntity(entityId);
            if (!(entity instanceof LivingEntity living)) continue;
            NMSAdapter adapter = NMSManager.getAdapter();
            if (adapter != null) adapter.doNotMove(living);
            immobilize(living);
        }
    }

    private Optional<Location> currentThreat(UUID casterId) {
        Player player = Bukkit.getPlayer(casterId);
        return validPlayer(player) ? Optional.of(player.getLocation().clone()) : Optional.empty();
    }

    private boolean validElite(UUID entityId) {
        Entity entity = Bukkit.getEntity(entityId);
        return entity instanceof LivingEntity livingEntity
                && valid(livingEntity)
                && EntityTracker.getEliteMobEntity(livingEntity) != null;
    }

    private void maintainSlowPotency() {
        for (UUID targetId : slowPotencyLeases.keySet().toArray(UUID[]::new)) {
            Entity entity = Bukkit.getEntity(targetId);
            if (!(entity instanceof LivingEntity target) || !valid(target)
                    || EntityTracker.getEliteMobEntity(target) == null) {
                clearSlowPotency(targetId, entity instanceof LivingEntity living ? living : null);
                continue;
            }
            Map<UUID, SlowPotencyLease> leases = slowPotencyLeases.get(targetId);
            leases.entrySet().removeIf(entry -> entry.getValue().expiresAtTick() <= currentTick
                    || !validSource(entry.getKey()));
            if (leases.isEmpty()) {
                clearSlowPotency(targetId, target);
                continue;
            }
            refreshSlowPotency(target);
        }
    }

    private void refreshSlowPotency(LivingEntity target) {
        Map<UUID, SlowPotencyLease> leases = slowPotencyLeases.get(target.getUniqueId());
        if (leases == null || leases.isEmpty()) {
            applySlowAdjustment(target, 0D);
            return;
        }
        PotionEffect currentSlow = target.getPotionEffect(PotionEffectType.SLOWNESS);
        if (currentSlow == null) {
            applySlowAdjustment(target, 0D);
            return;
        }
        double adjustment = leases.values().stream()
                .filter(lease -> lease.potionAmplifier() >= currentSlow.getAmplifier())
                .mapToDouble(SlowPotencyLease::movementSpeedAdjustment)
                .min()
                .orElse(0D);
        applySlowAdjustment(target, adjustment);
    }

    private void applySlowAdjustment(LivingEntity target, double adjustment) {
        UUID targetId = target.getUniqueId();
        double previous = appliedSlowAdjustments.getOrDefault(targetId, 0D);
        if (Math.abs(previous - adjustment) < 1.0E-9D) return;
        AttributeInstance movement = target.getAttribute(Attribute.MOVEMENT_SPEED);
        if (movement == null) return;
        movement.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(slowPotencyKey))
                .toList()
                .forEach(movement::removeModifier);
        if (adjustment < 0D) {
            movement.addModifier(new AttributeModifier(
                    slowPotencyKey,
                    Math.max(-.95D, adjustment),
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                    EquipmentSlotGroup.ANY));
            appliedSlowAdjustments.put(targetId, adjustment);
        } else {
            appliedSlowAdjustments.remove(targetId);
        }
    }

    private void clearSlowPotency(UUID targetId, LivingEntity target) {
        slowPotencyLeases.remove(targetId);
        if (target != null) applySlowAdjustment(target, 0D);
        appliedSlowAdjustments.remove(targetId);
    }

    private static boolean validSource(UUID sourceId) {
        Player source = Bukkit.getPlayer(sourceId);
        return validPlayer(source) && ClassAbilityEligibility.isEligible(source);
    }

    private static Optional<FearLeaseRegistry.Redirectable<Location>> open(
            NMSAdapter adapter,
            LivingEntity target,
            Location threat) {
        try {
            return adapter.beginFlee(target, threat, FEAR_SPEED_MODIFIER)
                    .map(MovementRedirect::new);
        } catch (RuntimeException unsupported) {
            return Optional.empty();
        }
    }

    private static boolean valid(LivingEntity entity) {
        return entity != null && entity.isValid() && !entity.isDead() && entity.getHealth() > 0D;
    }

    private static boolean validPlayer(Player player) {
        return player != null && player.isOnline() && valid(player);
    }

    private static void immobilize(LivingEntity target) {
        target.setVelocity(new Vector());
        target.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS, 3, 255, false, false, false), true);
    }

    private static long saturatedAdd(long value, long increment) {
        return value > Long.MAX_VALUE - increment ? Long.MAX_VALUE : value + increment;
    }

    private record MovementRedirect(TransientMovementOverride delegate)
            implements FearLeaseRegistry.Redirectable<Location> {
        private MovementRedirect {
            Objects.requireNonNull(delegate, "delegate");
        }

        @Override
        public boolean isActive() {
            return delegate.isActive();
        }

        @Override
        public boolean retarget(Location sourceState) {
            return delegate.retarget(sourceState);
        }

        @Override
        public void close() {
            delegate.close();
        }
    }

    private record SlowPotencyLease(
            long expiresAtTick,
            int potionAmplifier,
            double movementSpeedAdjustment) {
    }
}
