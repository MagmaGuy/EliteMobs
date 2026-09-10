package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.CustomProjectileData;
import com.magmaguy.elitemobs.items.ItemTagger;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns the invisible arrows used as physical collision carriers for class abilities. The arrow
 * never supplies damage itself. Its first native or FMM collision claims the flight, removes the
 * carrier and invokes the server-authoritative ability impact once.
 */
public final class ClassAbilityProjectileCarrier implements Listener, AutoCloseable {
    private static final byte MARKER_VALUE = 1;
    private static final double LAUNCH_ORIGIN_Y_OFFSET = -.3D;
    private static final Map<UUID, ClassAbilityProjectileCarrier> OWNERS = new ConcurrentHashMap<>();
    private static volatile NamespacedKey markerKey;

    private final Plugin plugin;
    private final ProjectileImpactLedger<Flight> flights = new ProjectileImpactLedger<>();
    private boolean closed;

    ClassAbilityProjectileCarrier(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        markerKey = new NamespacedKey(plugin, "class_ability_projectile");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    boolean launch(
            Player caster,
            Location destination,
            double maximumRange,
            double blocksPerTick,
            ImpactHandler impactHandler,
            FlightRenderer renderer) {
        Objects.requireNonNull(caster, "caster");
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(impactHandler, "impactHandler");
        Objects.requireNonNull(renderer, "renderer");
        if (closed || !caster.isOnline() || caster.isDead()) return false;

        Location eyeLocation = caster.getEyeLocation().clone();
        Location plannedStart = launchOrigin(eyeLocation);
        if (plannedStart.getWorld() == null || destination.getWorld() == null
                || !plannedStart.getWorld().equals(destination.getWorld())) return false;
        Vector delta = destination.toVector().subtract(plannedStart.toVector());
        if (delta.lengthSquared() < 1.0E-8D) return false;

        double safeRange = Math.max(1D, maximumRange);
        double safeSpeed = Math.max(.2D, blocksPerTick);
        Vector velocity = delta.normalize().multiply(safeSpeed);
        // launchProjectile is intentional. FMM registers arrows from ProjectileLaunchEvent so its
        // swept OBB collision path sees the carrier. World.spawn would bypass that registration.
        Arrow arrow = caster.launchProjectile(Arrow.class, velocity);
        Location start;
        try {
            Optional<Location> positionedStart = positionAtLaunchOrigin(arrow, eyeLocation);
            if (positionedStart.isEmpty()) {
                arrow.remove();
                return false;
            }
            start = positionedStart.get();
            configure(arrow, caster, velocity);
            CustomProjectileData.getCustomProjectileDataHashMap().remove(arrow);
            ItemTagger.clearArrowCombatData(arrow);
        } catch (RuntimeException | Error failure) {
            arrow.remove();
            throw failure;
        }
        if (!arrow.isValid()) {
            arrow.remove();
            return false;
        }
        Flight flight = new Flight(
                arrow,
                caster,
                start,
                safeRange,
                Math.max(4, (int) Math.ceil(safeRange / safeSpeed) + 4),
                impactHandler,
                renderer);
        UUID projectileId = arrow.getUniqueId();
        if (!flights.register(projectileId, caster.getUniqueId(), flight)) {
            arrow.remove();
            return false;
        }
        OWNERS.put(projectileId, this);
        flight.visualTask = new BukkitRunnable() {
            private int age;

            @Override
            public void run() {
                age++;
                if (closed || !caster.isOnline() || caster.isDead()
                        || !arrow.isValid() || arrow.isDead()
                        || !arrow.getWorld().equals(start.getWorld())
                        || age > flight.maximumTicks
                        || arrow.getLocation().distanceSquared(start) > safeRange * safeRange) {
                    retire(projectileId);
                    return;
                }
                double progress = Math.min(1D,
                        Math.sqrt(arrow.getLocation().distanceSquared(start)) / safeRange);
                try {
                    renderer.render(arrow.getLocation(), progress);
                } catch (RuntimeException | Error failure) {
                    retire(projectileId);
                    throw failure;
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
        return true;
    }

    static Optional<Location> positionAtLaunchOrigin(Projectile carrier, Location eyeLocation) {
        Objects.requireNonNull(carrier, "carrier");
        Location start = launchOrigin(Objects.requireNonNull(eyeLocation, "eyeLocation"));
        return carrier.teleport(start) ? Optional.of(start) : Optional.empty();
    }

    private static Location launchOrigin(Location eyeLocation) {
        return eyeLocation.clone().add(0D, LAUNCH_ORIGIN_Y_OFFSET, 0D);
    }

    /** True for current and stale carriers so neither can fall through to vanilla/FMM damage. */
    public static boolean isCarrier(Projectile projectile) {
        if (projectile == null) return false;
        NamespacedKey key = markerKey;
        return key != null && projectile.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    /** Called by the optional FMM bridge when its OBB, rather than Bukkit, owns the collision. */
    public static boolean tryModeledImpact(Projectile projectile, Entity underlyingEntity) {
        if (!isCarrier(projectile)) return false;
        ClassAbilityProjectileCarrier owner = OWNERS.get(projectile.getUniqueId());
        return owner != null && owner.impact(projectile, underlyingEntity);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void suppressVanillaCarrierDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile projectile) || !isCarrier(projectile)) return;
        // The configured impact deliberately reuses the arrow as its damage source so downstream
        // combat code sees a projectile owned by the caster. That nested event is the one allowed
        // damage event in the carrier's lifetime.
        if (CombatDamageContext.isClassAbilityDamageActive()) return;
        event.setCancelled(true);
        impact(projectile, event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void enforceSuppressedVanillaCarrierDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile projectile) || !isCarrier(projectile)) return;
        if (!CombatDamageContext.isClassAbilityDamageActive()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void retireCarrierOnProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!isCarrier(projectile)) return;
        Entity hitEntity = event.getHitEntity();
        if (hitEntity == null) {
            projectile.getWorld().spawnParticle(
                    Particle.SMOKE, projectile.getLocation(), 8, .12D, .12D, .12D, .01D);
            retire(projectile.getUniqueId());
            return;
        }
        impact(projectile, hitEntity);
    }

    void deactivate(Player caster) {
        for (Flight flight : flights.retireCaster(caster.getUniqueId())) finish(flight);
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        HandlerList.unregisterAll(this);
        for (Flight flight : flights.retireAll()) finish(flight);
    }

    private void configure(Arrow arrow, Player caster, Vector velocity) {
        NamespacedKey key = markerKey;
        if (key == null) throw new IllegalStateException("Projectile carrier marker is not initialized");
        arrow.getPersistentDataContainer().set(key, PersistentDataType.BYTE, MARKER_VALUE);
        arrow.setShooter(caster);
        arrow.setVelocity(velocity);
        arrow.setDamage(0D);
        arrow.setKnockbackStrength(0);
        arrow.setCritical(false);
        arrow.setPierceLevel(0);
        arrow.setGravity(false);
        arrow.setSilent(true);
        arrow.setPersistent(false);
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        arrow.setVisibleByDefault(false);
        for (Player viewer : arrow.getWorld().getPlayers()) viewer.hideEntity(plugin, arrow);
    }

    private boolean impact(Projectile projectile, Entity hitEntity) {
        Flight flight = flights.claim(projectile.getUniqueId()).orElse(null);
        if (flight == null) {
            if (isCarrier(projectile) && projectile.isValid()) projectile.remove();
            return false;
        }
        OWNERS.remove(projectile.getUniqueId(), this);
        boolean valid = hitEntity instanceof LivingEntity living
                && living.isValid()
                && !living.isDead()
                && flight.caster.isOnline()
                && !flight.caster.isDead()
                && living.getWorld().equals(flight.start.getWorld())
                && living.getWorld().equals(flight.caster.getWorld())
                && projectile.getLocation().distanceSquared(flight.start)
                <= flight.maximumRange * flight.maximumRange + 1D;
        try {
            if (valid) flight.impactHandler.impact(projectile, (LivingEntity) hitEntity);
        } finally {
            finish(flight);
        }
        return valid;
    }

    private void retire(UUID projectileId) {
        Flight flight = flights.retire(projectileId).orElse(null);
        if (flight == null) return;
        OWNERS.remove(projectileId, this);
        finish(flight);
    }

    private void finish(Flight flight) {
        OWNERS.remove(flight.arrow.getUniqueId(), this);
        BukkitTask visualTask = flight.visualTask;
        if (visualTask != null) visualTask.cancel();
        if (flight.arrow.isValid()) flight.arrow.remove();
    }

    @FunctionalInterface
    interface ImpactHandler {
        void impact(Projectile projectile, LivingEntity hitEntity);
    }

    @FunctionalInterface
    interface FlightRenderer {
        void render(Location point, double progress);
    }

    private static final class Flight {
        private final Arrow arrow;
        private final Player caster;
        private final Location start;
        private final double maximumRange;
        private final int maximumTicks;
        private final ImpactHandler impactHandler;
        private final FlightRenderer renderer;
        private BukkitTask visualTask;

        private Flight(
                Arrow arrow,
                Player caster,
                Location start,
                double maximumRange,
                int maximumTicks,
                ImpactHandler impactHandler,
                FlightRenderer renderer) {
            this.arrow = arrow;
            this.caster = caster;
            this.start = start.clone();
            this.maximumRange = maximumRange;
            this.maximumTicks = maximumTicks;
            this.impactHandler = impactHandler;
            this.renderer = renderer;
        }
    }
}
