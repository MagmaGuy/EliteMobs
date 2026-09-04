package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Owns the literal, short-lived horse summoned by the Paladin mobility ability. */
final class DivineSteedManager implements Listener, AutoCloseable {
    private static final double MAXIMUM_HEALTH = 30D;
    private static final double MOVEMENT_SPEED = .20D;
    private static final double JUMP_STRENGTH = .72D;
    private static final int LANDING_GRACE_TICKS = 20;

    private final Map<UUID, Ride> ridesByOwner = new LinkedHashMap<>();
    private final Map<UUID, Ride> ridesBySteed = new LinkedHashMap<>();
    private final Attribute maximumHealthAttribute;
    private final Attribute movementSpeedAttribute;
    private final Attribute jumpStrengthAttribute;
    private final Logger logger;
    private final BukkitTask maintenanceTask;
    private boolean closed;

    DivineSteedManager(Plugin plugin) {
        Plugin owner = Objects.requireNonNull(plugin, "plugin");
        logger = owner.getLogger();
        Registry<Attribute> attributes = owner.getServer().getRegistry(Attribute.class);
        maximumHealthAttribute = attribute(attributes, "max_health");
        movementSpeedAttribute = attribute(attributes, "movement_speed");
        jumpStrengthAttribute = attribute(attributes, "jump_strength");
        owner.getServer().getPluginManager().registerEvents(this, owner);
        maintenanceTask = owner.getServer().getScheduler().runTaskTimer(owner, this::tick, 1L, 1L);
    }

    Optional<Horse> summon(Player player, int durationTicks, RideListener listener) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(listener, "listener");
        if (closed || durationTicks < 1 || !player.isOnline() || !player.isValid() || player.isDead())
            return Optional.empty();

        Ride existing = ridesByOwner.get(player.getUniqueId());
        Entity vehicle = player.getVehicle();
        if (vehicle != null && (existing == null || !vehicle.getUniqueId().equals(existing.steed().getUniqueId())))
            return Optional.empty();
        if (existing != null) terminate(existing, true);

        Horse horse = null;
        try {
            horse = player.getWorld().spawn(player.getLocation(), Horse.class);
            if (horse == null) return Optional.empty();
            configure(player, horse);
            if (!horse.addPassenger(player) || !horse.getPassengers().contains(player)) {
                discard(horse);
                return Optional.empty();
            }
            Ride ride = new Ride(player, horse, durationTicks, listener);
            ridesByOwner.put(player.getUniqueId(), ride);
            ridesBySteed.put(horse.getUniqueId(), ride);
            return Optional.of(horse);
        } catch (RuntimeException exception) {
            if (horse != null) discard(horse);
            return Optional.empty();
        }
    }

    void deactivate(Player player) {
        Objects.requireNonNull(player, "player");
        Ride ride = ridesByOwner.get(player.getUniqueId());
        if (ride != null) terminate(ride, true);
    }

    private void configure(Player player, Horse horse) {
        horse.setPersistent(false);
        horse.setRemoveWhenFarAway(true);
        horse.setAdult();
        horse.setAgeLock(true);
        horse.setTamed(true);
        horse.setOwner(player);
        horse.setDomestication(horse.getMaxDomestication());
        horse.setColor(Horse.Color.WHITE);
        horse.setStyle(Horse.Style.NONE);
        horse.setCustomName("Divine Steed");
        horse.setCustomNameVisible(false);
        horse.setCanPickupItems(false);
        horse.setLootTable(null);
        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        horse.getInventory().setArmor(new ItemStack(Material.GOLDEN_HORSE_ARMOR));
        setBaseAttribute(horse, maximumHealthAttribute, MAXIMUM_HEALTH);
        setBaseAttribute(horse, movementSpeedAttribute, MOVEMENT_SPEED);
        if (jumpStrengthAttribute == null) horse.setJumpStrength(JUMP_STRENGTH);
        else setBaseAttribute(horse, jumpStrengthAttribute, JUMP_STRENGTH);
        if (maximumHealthAttribute != null) horse.setHealth(MAXIMUM_HEALTH);
    }

    private static Attribute attribute(Registry<Attribute> registry, String key) {
        return registry == null ? null : registry.get(NamespacedKey.minecraft(key));
    }

    private static void setBaseAttribute(Horse horse, Attribute attribute, double value) {
        if (attribute == null) return;
        AttributeInstance instance = horse.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(value);
    }

    private void tick() {
        for (Ride ride : java.util.List.copyOf(ridesByOwner.values())) {
            if (!active(ride)) {
                terminate(ride, true);
                continue;
            }
            if (ride.remainingTicks() > 0) {
                try {
                    ride.listener().onTick(ride.steed());
                } catch (RuntimeException exception) {
                    terminate(ride, true);
                    continue;
                }
                ride.elapseTick();
            }
            if (ride.remainingTicks() > 0) continue;
            ride.elapseLandingGrace();
            if (ride.steed().isOnGround() || ride.landingGraceTicks() >= LANDING_GRACE_TICKS)
                terminate(ride, true);
        }
    }

    private static boolean active(Ride ride) {
        Player owner = ride.owner();
        Horse steed = ride.steed();
        Entity vehicle = owner.getVehicle();
        return owner.isOnline()
                && owner.isValid()
                && !owner.isDead()
                && steed.isValid()
                && !steed.isDead()
                && owner.getWorld().equals(steed.getWorld())
                && vehicle != null
                && vehicle.getUniqueId().equals(steed.getUniqueId())
                && steed.getPassengers().stream()
                .anyMatch(passenger -> passenger.getUniqueId().equals(owner.getUniqueId()));
    }

    private void terminate(Ride ride, boolean removeHorse) {
        if (!ridesByOwner.remove(ride.owner().getUniqueId(), ride)) return;
        ridesBySteed.remove(ride.steed().getUniqueId(), ride);
        try {
            ride.listener().onEnd(ride.steed());
        } catch (RuntimeException exception) {
            warn("Divine Steed end callback failed", exception);
        }
        if (removeHorse) discard(ride.steed());
    }

    private void discard(Horse horse) {
        attempt("clear temporary equipment", () -> clearEquipment(horse));
        attempt("eject riders", horse::eject);
        attempt("remove temporary horse", () -> {
            if (horse.isValid()) horse.remove();
        });
    }

    private static void clearEquipment(Horse horse) {
        horse.getInventory().setSaddle(new ItemStack(Material.AIR));
        horse.getInventory().setArmor(new ItemStack(Material.AIR));
    }

    private void attempt(String operation, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            warn("Could not " + operation, exception);
        }
    }

    private void warn(String message, RuntimeException exception) {
        if (logger != null) logger.log(Level.WARNING, message, exception);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {
        Ride ride = ridesBySteed.get(event.getEntity().getUniqueId());
        if (ride == null) return;
        event.getDrops().clear();
        event.setDroppedExp(0);
        terminate(ride, false);
        attempt("clear dead steed equipment", () -> clearEquipment(ride.steed()));
        attempt("eject riders from dead steed", ride.steed()::eject);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRemoved(EntityRemoveEvent event) {
        Ride ride = ridesBySteed.get(event.getEntity().getUniqueId());
        if (ride == null) return;
        terminate(ride, false);
        attempt("clear removed steed equipment", () -> clearEquipment(ride.steed()));
        attempt("eject riders from removed steed", ride.steed()::eject);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof Horse horse
                && ridesBySteed.containsKey(horse.getUniqueId())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleExit(VehicleExitEvent event) {
        Ride ride = ridesBySteed.get(event.getVehicle().getUniqueId());
        if (ride == null
                || !event.getExited().getUniqueId().equals(ride.owner().getUniqueId())) return;
        terminate(ride, true);
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        attempt("cancel steed maintenance", maintenanceTask::cancel);
        for (Ride ride : java.util.List.copyOf(ridesByOwner.values())) terminate(ride, true);
        ridesByOwner.clear();
        ridesBySteed.clear();
        HandlerList.unregisterAll(this);
    }

    interface RideListener {
        RideListener NOOP = horse -> {
        };

        void onTick(Horse horse);

        default void onEnd(Horse horse) {
        }
    }

    private static final class Ride {
        private final Player owner;
        private final Horse steed;
        private final RideListener listener;
        private int remainingTicks;
        private int landingGraceTicks;

        private Ride(Player owner, Horse steed, int remainingTicks, RideListener listener) {
            this.owner = owner;
            this.steed = steed;
            this.remainingTicks = remainingTicks;
            this.listener = listener;
        }

        Player owner() {
            return owner;
        }

        Horse steed() {
            return steed;
        }

        RideListener listener() {
            return listener;
        }

        int remainingTicks() {
            return remainingTicks;
        }

        void elapseTick() {
            remainingTicks--;
        }

        int landingGraceTicks() {
            return landingGraceTicks;
        }

        void elapseLandingGrace() {
            landingGraceTicks++;
        }
    }
}
