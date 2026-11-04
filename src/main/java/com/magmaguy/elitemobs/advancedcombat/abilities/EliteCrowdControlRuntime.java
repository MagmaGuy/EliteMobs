package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.easyminecraftgoals.NMSAdapter;
import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.TransientMovementOverride;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.advancedcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.ElitePowerPauseReason;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
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
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Owns source-scoped Elite control leases without changing permanent AI state. */
final class EliteCrowdControlRuntime implements Listener, AutoCloseable {
    private static final double FEAR_SPEED_MODIFIER = 1.2D;
    private static final String SLOW_POTENCY_KEY = "advanced_control_potency";
    private static final String SCALE_KEY = "advanced_control_scale";
    /** A launched foe roots on landing, or after this many ticks if it never lands. */
    private static final int MAXIMUM_LANDING_WAIT_TICKS = 30;
    private static final int MINIMUM_AIRBORNE_TICKS = 3;

    private final FearLeaseRegistry<UUID, Location> fearLeases = new FearLeaseRegistry<>();
    private final SourceSuppressionLeaseRegistry<UUID, UUID> interruptLeases =
            new SourceSuppressionLeaseRegistry<>();
    private final SourceSuppressionLeaseRegistry<UUID, UUID> rootLeases = new SourceSuppressionLeaseRegistry<>();
    private final Map<UUID, Map<UUID, SlowPotencyLease>> slowPotencyLeases = new HashMap<>();
    private final Map<UUID, Double> appliedSlowAdjustments = new HashMap<>();
    private final Map<UUID, Map<UUID, ScaleLease>> scaleLeases = new HashMap<>();
    private final Map<UUID, Double> appliedScaleFactors = new HashMap<>();
    private final Map<UUID, Map<UUID, PendingRoot>> pendingRoots = new HashMap<>();
    private final NamespacedKey slowPotencyKey;
    private final NamespacedKey scaleKey;
    private final BukkitTask tickTask;
    private long currentTick;
    private boolean closed;

    EliteCrowdControlRuntime(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        slowPotencyKey = new NamespacedKey(plugin, SLOW_POTENCY_KEY);
        scaleKey = new NamespacedKey(plugin, SCALE_KEY);
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

    boolean interrupt(Player caster, EliteEntity elite, int durationTicks) {
        if (closed || caster == null || elite == null || durationTicks <= 0) return false;
        LivingEntity target = elite.getLivingEntity();
        if (!validPlayer(caster) || !valid(target)) return false;
        long expiresAt = saturatedAdd(currentTick, durationTicks);
        return interruptLeases.apply(
                target.getUniqueId(),
                caster.getUniqueId(),
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

    /**
     * Roots a foe this cast also displaced once it lands, so the root never cancels the launch or
     * knockback it follows. Returns whether the root was queued.
     */
    boolean rootAfterLanding(Player caster, EliteEntity elite, int durationTicks, Runnable onRooted) {
        if (closed || caster == null || elite == null || durationTicks <= 0) return false;
        LivingEntity target = elite.getLivingEntity();
        if (!validPlayer(caster) || !valid(target)) return false;
        pendingRoots.computeIfAbsent(target.getUniqueId(), ignored -> new HashMap<>())
                .put(caster.getUniqueId(), new PendingRoot(durationTicks, currentTick,
                        Objects.requireNonNull(onRooted, "onRooted")));
        return true;
    }

    /**
     * Leases a temporary size change. Shrinks always apply; an enlargement applies only when the
     * larger body fits where the foe stands. Returns whether the size change was leased.
     */
    boolean scale(Player caster, EliteEntity elite, double factor, int durationTicks) {
        if (closed || caster == null || elite == null || durationTicks <= 0
                || !Double.isFinite(factor) || factor <= 0D || Double.compare(factor, 1D) == 0) return false;
        LivingEntity target = elite.getLivingEntity();
        if (!validPlayer(caster) || !valid(target)) return false;
        if (target.getAttribute(Attribute.SCALE) == null) return false;
        if (factor > 1D && !roomToGrow(target, factor / currentScaleFactor(target))) return false;
        scaleLeases.computeIfAbsent(target.getUniqueId(), ignored -> new HashMap<>())
                .put(caster.getUniqueId(), new ScaleLease(saturatedAdd(currentTick, durationTicks), factor));
        refreshScale(target);
        return true;
    }

    int scaleRemainingTicks(Player caster, LivingEntity target) {
        Map<UUID, ScaleLease> leases = scaleLeases.get(target.getUniqueId());
        ScaleLease lease = leases == null ? null : leases.get(caster.getUniqueId());
        return lease == null ? 0 : (int) Math.max(0L, lease.expiresAtTick() - currentTick);
    }

    boolean extendRoot(Player caster, EliteEntity elite, int additionalTicks) {
        if (closed || caster == null || elite == null || additionalTicks <= 0) return false;
        LivingEntity target = elite.getLivingEntity();
        return validPlayer(caster) && valid(target) && rootLeases.extend(
                target.getUniqueId(), caster.getUniqueId(), additionalTicks, currentTick);
    }

    int remainingTicks(Player caster, LivingEntity target, AbilityEffect effect) {
        return switch (effect) {
            case ROOT -> rootLeases.remainingTicks(target.getUniqueId(), caster.getUniqueId(), currentTick);
            case INTERRUPT -> interruptLeases.remainingTicks(target.getUniqueId(), caster.getUniqueId(), currentTick);
            case FEAR -> fearLeases.remainingTicks(target.getUniqueId(), caster.getUniqueId(), currentTick);
            default -> throw new IllegalArgumentException("Not a leased control: " + effect);
        };
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
                || plan.durationTicks() <= 0) return;
        LivingEntity target = elite.getLivingEntity();
        if (!validPlayer(caster) || !valid(target)) return;
        slowPotencyLeases.computeIfAbsent(target.getUniqueId(), ignored -> new HashMap<>())
                .put(caster.getUniqueId(), new SlowPotencyLease(
                        saturatedAdd(currentTick, plan.durationTicks()),
                        plan.potionAmplifier(),
                        plan.additionalMovementSpeedAdjustment()));
        refreshSlowPotency(target);
    }

    int ownedSlowRemainingTicks(Player caster, LivingEntity target, int amplifier) {
        Map<UUID, SlowPotencyLease> leases = slowPotencyLeases.get(target.getUniqueId());
        if (closed || leases == null) return 0;
        SlowPotencyLease lease = leases.get(caster.getUniqueId());
        PotionEffect actual = target.getPotionEffect(PotionEffectType.SLOWNESS);
        if (lease == null || actual == null || lease.potionAmplifier() != amplifier
                || actual.getAmplifier() != amplifier) return 0;
        return (int) Math.max(0L, Math.min(actual.getDuration(), lease.expiresAtTick() - currentTick));
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR, ignoreCancelled = true)
    public void onSlowChanged(org.bukkit.event.entity.EntityPotionEffectEvent event) {
        if (event.getModifiedType() != PotionEffectType.SLOWNESS
                || !slowPotencyLeases.containsKey(event.getEntity().getUniqueId())) return;
        // A confirmed application installs its lease after native dispatch. Any accepted
        // replacement first revokes the previous source, even when its visible values match.
        if (event.getNewEffect() == null || event.getOldEffect() == null || event.isOverride())
            clearSlowPotency(event.getEntity().getUniqueId(),
                    event.getEntity() instanceof LivingEntity living ? living : null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEliteRemoved(EliteMobRemoveEvent event) {
        Entity entity = event.getEntity();
        if (entity != null) {
            fearLeases.remove(entity.getUniqueId());
            interruptLeases.remove(entity.getUniqueId());
            rootLeases.remove(entity.getUniqueId());
            pendingRoots.remove(entity.getUniqueId());
            clearSlowPotency(entity.getUniqueId(), entity instanceof LivingEntity living ? living : null);
            clearScale(entity.getUniqueId(), entity instanceof LivingEntity living ? living : null);
        }
    }

    void clearSource(UUID sourceId) {
        fearLeases.clearSource(sourceId);
        rootLeases.clearSource(sourceId);
        interruptLeases.clearSource(sourceId);
        for (UUID targetId : slowPotencyLeases.keySet().toArray(UUID[]::new)) {
            if (slowPotencyLeases.get(targetId).remove(sourceId) == null) continue;
            Entity entity = Bukkit.getEntity(targetId);
            if (slowPotencyLeases.get(targetId).isEmpty() || !(entity instanceof LivingEntity))
                clearSlowPotency(targetId, entity instanceof LivingEntity living ? living : null);
            else refreshSlowPotency((LivingEntity) entity);
        }
        for (UUID targetId : pendingRoots.keySet().toArray(UUID[]::new)) {
            Map<UUID, PendingRoot> pending = pendingRoots.get(targetId);
            pending.remove(sourceId);
            if (pending.isEmpty()) pendingRoots.remove(targetId);
        }
        for (UUID targetId : scaleLeases.keySet().toArray(UUID[]::new)) {
            if (scaleLeases.get(targetId).remove(sourceId) == null) continue;
            Entity entity = Bukkit.getEntity(targetId);
            if (scaleLeases.get(targetId).isEmpty() || !(entity instanceof LivingEntity))
                clearScale(targetId, entity instanceof LivingEntity living ? living : null);
            else refreshScale((LivingEntity) entity);
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
        pendingRoots.clear();
        for (UUID targetId : scaleLeases.keySet().toArray(UUID[]::new)) {
            Entity entity = Bukkit.getEntity(targetId);
            clearScale(targetId, entity instanceof LivingEntity living ? living : null);
        }
        scaleLeases.clear();
        appliedScaleFactors.clear();
    }

    private void tick() {
        if (closed) return;
        currentTick = saturatedAdd(currentTick, 1L);
        fearLeases.maintain(currentTick, this::validElite, this::currentThreat);
        interruptLeases.maintain(currentTick, this::validElite);
        rootLeases.maintain(currentTick, this::validElite);
        maintainPendingRoots();
        maintainSlowPotency();
        maintainScale();
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

    private void maintainPendingRoots() {
        for (UUID targetId : pendingRoots.keySet().toArray(UUID[]::new)) {
            Entity entity = Bukkit.getEntity(targetId);
            Map<UUID, PendingRoot> pending = pendingRoots.get(targetId);
            if (!(entity instanceof LivingEntity target) || !validElite(targetId)) {
                pendingRoots.remove(targetId);
                continue;
            }
            boolean onGround = target.isOnGround();
            pending.entrySet().removeIf(entry -> {
                long waited = currentTick - entry.getValue().queuedAtTick();
                boolean landed = onGround && waited >= MINIMUM_AIRBORNE_TICKS;
                if (!landed && waited < MAXIMUM_LANDING_WAIT_TICKS) return false;
                Player caster = Bukkit.getPlayer(entry.getKey());
                EliteEntity elite = EntityTracker.getEliteMobEntity(target);
                if (validSource(entry.getKey()) && elite != null
                        && root(caster, elite, entry.getValue().durationTicks()))
                    entry.getValue().onRooted().run();
                return true;
            });
            if (pending.isEmpty()) pendingRoots.remove(targetId);
        }
    }

    private void maintainScale() {
        for (UUID targetId : scaleLeases.keySet().toArray(UUID[]::new)) {
            Entity entity = Bukkit.getEntity(targetId);
            if (!(entity instanceof LivingEntity target) || !valid(target)
                    || EntityTracker.getEliteMobEntity(target) == null) {
                clearScale(targetId, entity instanceof LivingEntity living ? living : null);
                continue;
            }
            Map<UUID, ScaleLease> leases = scaleLeases.get(targetId);
            leases.entrySet().removeIf(entry -> entry.getValue().expiresAtTick() <= currentTick
                    || !validSource(entry.getKey()));
            if (leases.isEmpty()) {
                clearScale(targetId, target);
                continue;
            }
            refreshScale(target);
        }
    }

    /** The smallest active shrink and the largest active enlargement combine. */
    private void refreshScale(LivingEntity target) {
        Map<UUID, ScaleLease> leases = scaleLeases.get(target.getUniqueId());
        if (leases == null || leases.isEmpty()) {
            applyScaleFactor(target, 1D);
            return;
        }
        double shrink = leases.values().stream().mapToDouble(ScaleLease::factor)
                .filter(factor -> factor < 1D).min().orElse(1D);
        double grow = leases.values().stream().mapToDouble(ScaleLease::factor)
                .filter(factor -> factor > 1D).max().orElse(1D);
        applyScaleFactor(target, shrink * grow);
    }

    private double currentScaleFactor(LivingEntity target) {
        return appliedScaleFactors.getOrDefault(target.getUniqueId(), 1D);
    }

    private void applyScaleFactor(LivingEntity target, double factor) {
        UUID targetId = target.getUniqueId();
        double previous = appliedScaleFactors.getOrDefault(targetId, 1D);
        if (Math.abs(previous - factor) < 1.0E-9D) return;
        AttributeInstance scale = target.getAttribute(Attribute.SCALE);
        if (scale == null) return;
        scale.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(scaleKey))
                .toList()
                .forEach(scale::removeModifier);
        if (Math.abs(factor - 1D) >= 1.0E-9D) {
            scale.addModifier(new AttributeModifier(
                    scaleKey,
                    factor - 1D,
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                    EquipmentSlotGroup.ANY));
            appliedScaleFactors.put(targetId, factor);
        } else {
            appliedScaleFactors.remove(targetId);
        }
    }

    private void clearScale(UUID targetId, LivingEntity target) {
        scaleLeases.remove(targetId);
        if (target != null) applyScaleFactor(target, 1D);
        appliedScaleFactors.remove(targetId);
    }

    /** Every block the grown body would overlap must be passable. */
    private static boolean roomToGrow(LivingEntity target, double growth) {
        BoundingBox box = target.getBoundingBox();
        double halfWidthX = box.getWidthX() * growth * .5D;
        double halfWidthZ = box.getWidthZ() * growth * .5D;
        BoundingBox grown = new BoundingBox(
                box.getCenterX() - halfWidthX, box.getMinY(), box.getCenterZ() - halfWidthZ,
                box.getCenterX() + halfWidthX, box.getMinY() + box.getHeight() * growth,
                box.getCenterZ() + halfWidthZ);
        World world = target.getWorld();
        for (int x = (int) Math.floor(grown.getMinX()); x <= (int) Math.floor(grown.getMaxX()); x++)
            for (int y = (int) Math.floor(grown.getMinY() + 1.0E-3D); y <= (int) Math.floor(grown.getMaxY()); y++)
                for (int z = (int) Math.floor(grown.getMinZ()); z <= (int) Math.floor(grown.getMaxZ()); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!block.isPassable() && block.getBoundingBox().overlaps(grown)) return false;
                }
        return true;
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

    private record ScaleLease(long expiresAtTick, double factor) {
    }

    private record PendingRoot(int durationTicks, long queuedAtTick, Runnable onRooted) {
    }

    private record SlowPotencyLease(
            long expiresAtTick,
            int potionAmplifier,
            double movementSpeedAdjustment) {
    }
}
