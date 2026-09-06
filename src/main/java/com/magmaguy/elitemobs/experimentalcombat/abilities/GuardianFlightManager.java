package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.experimentalcombat.ClassAbilityEligibility;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Owns velocity-driven Guardian Flight sessions without changing native flight, gravity,
 * collision or other plugins' movement flags.
 */
final class GuardianFlightManager implements Listener, AutoCloseable {
    private static final double ALLY_ARRIVAL_DISTANCE = 2.1D;
    private static final double ANCHOR_ARRIVAL_DISTANCE = 1.15D;
    private static final double RANGE_TOLERANCE = 2D;
    private static final double MINIMUM_PROGRESS_PER_TICK = .025D;
    private static final double COLLISION_LOOKAHEAD = .28D;

    private final Map<UUID, Session> sessions = new LinkedHashMap<>();
    private final Logger logger;
    private final BukkitTask maintenanceTask;
    private boolean closed;

    GuardianFlightManager(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        this.logger = plugin.getLogger();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        maintenanceTask = plugin.getServer().getScheduler().runTaskTimer(
                plugin, (Runnable) this::tick, 1L, 1L);
    }

    boolean startToAlly(
            Player caster,
            Player ally,
            double maximumRange,
            int maximumTicks,
            BiPredicate<Player, Player> remainsAnAlly,
            FlightListener listener) {
        Objects.requireNonNull(ally, "ally");
        Objects.requireNonNull(remainsAnAlly, "remainsAnAlly");
        return start(
                caster,
                new AllyDestination(ally, remainsAnAlly),
                maximumRange,
                maximumTicks,
                ALLY_ARRIVAL_DISTANCE,
                listener);
    }

    boolean startToAnchor(
            Player caster,
            Location anchor,
            double maximumRange,
            int maximumTicks,
            FlightListener listener) {
        Objects.requireNonNull(anchor, "anchor");
        return start(
                caster,
                new AnchorDestination(anchor),
                maximumRange,
                maximumTicks,
                ANCHOR_ARRIVAL_DISTANCE,
                listener);
    }

    private boolean start(
            Player caster,
            Destination destination,
            double maximumRange,
            int maximumTicks,
            double arrivalDistance,
            FlightListener listener) {
        Objects.requireNonNull(caster, "caster");
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(listener, "listener");
        if (closed || !Double.isFinite(maximumRange) || maximumRange <= 0D || maximumTicks < 1)
            return false;

        DestinationSnapshot target = destination.resolve(caster).orElse(null);
        if (!casterValid(caster) || target == null
                || !sameWorld(caster.getLocation(), target.location())) return false;
        double targetDistance = center(caster).distance(target.location());
        if (targetDistance > maximumRange + RANGE_TOLERANCE) return false;
        if (targetDistance > arrivalDistance) {
            Vector initial = GuardianFlightTrajectory.steer(
                    flightMomentum(caster), target.location().toVector().subtract(center(caster).toVector()));
            if (!collisionClear(caster, initial)) return false;
        }

        // Finish the previous session before publishing its replacement. Combined with
        // terminate's conditional Map.remove, stale cleanup can never remove the new session.
        Session previous = sessions.get(caster.getUniqueId());
        if (previous != null) terminate(previous, EndReason.REPLACED, null);
        Session session = new Session(
                caster,
                destination,
                maximumRange + RANGE_TOLERANCE,
                maximumTicks,
                arrivalDistance,
                listener);
        sessions.put(caster.getUniqueId(), session);
        caster.setFallDistance(0F);
        Location start = caster.getLocation();
        start.getWorld().spawnParticle(
                Particle.END_ROD, start.clone().add(0D, .9D, 0D), 12, .3D, .45D, .3D, .025D);
        start.getWorld().playSound(start, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, .6F, 1.45F);
        return true;
    }

    void cancel(Player caster) {
        Objects.requireNonNull(caster, "caster");
        Session session = sessions.get(caster.getUniqueId());
        if (session != null) terminate(session, EndReason.CANCELLED, null);
    }

    boolean active(Player caster) {
        return caster != null && sessions.containsKey(caster.getUniqueId());
    }

    private void tick() {
        for (Session session : List.copyOf(sessions.values())) {
            try {
                tick(session);
            } catch (RuntimeException exception) {
                logger.log(Level.WARNING, "Guardian Flight session failed safely", exception);
                terminate(session, EndReason.TARGET_INVALID, null);
            }
        }
    }

    private void tick(Session session) {
        if (sessions.get(session.caster().getUniqueId()) != session) return;
        Player caster = session.caster();
        Location current = caster.getLocation();
        session.captureProgress(current);
        DestinationSnapshot target = session.destination().resolve(caster).orElse(null);
        double targetDistance = target == null || !sameWorld(current, target.location())
                ? Double.NaN
                : center(caster).distance(target.location());
        GuardianFlightSessionPolicy.Decision decision = GuardianFlightSessionPolicy.evaluate(
                new GuardianFlightSessionPolicy.Frame(
                        casterValid(caster),
                        target != null && sameWorld(current, target.location()),
                        session.elapsedTicks(),
                        session.maximumTicks(),
                        targetDistance,
                        session.maximumTargetDistance(),
                        session.arrivalDistance(),
                        false,
                        session.stalledTicks()));
        if (decision != GuardianFlightSessionPolicy.Decision.CONTINUE) {
            terminate(session, endReason(decision), null);
            return;
        }

        Vector displacement = target.location().toVector().subtract(center(caster).toVector());
        Vector velocity = GuardianFlightTrajectory.steer(flightMomentum(caster), displacement);
        if (!collisionClear(caster, velocity)) {
            terminate(session, EndReason.OBSTRUCTED, null);
            return;
        }
        Location predicted = current.clone().add(velocity);
        if (!loaded(predicted)) {
            terminate(session, EndReason.OBSTRUCTED, null);
            return;
        }

        caster.setVelocity(velocity);
        caster.setFallDistance(0F);
        Location trail = center(caster);
        trail.getWorld().spawnParticle(Particle.END_ROD, trail, 2, .08D, .12D, .08D, .01D);
        trail.getWorld().spawnParticle(Particle.CLOUD, trail, 1, .04D, .06D, .04D, .005D);
        session.elapseTick();
    }

    private void terminate(Session session, EndReason reason, Vector exitVelocity) {
        if (!sessions.remove(session.caster().getUniqueId(), session)) return;
        Player caster = session.caster();
        if (caster.isValid()) {
            caster.setFallDistance(0F);
            if (exitVelocity != null) caster.setVelocity(exitVelocity);
            if (reason == EndReason.ARRIVED) {
                Location arrival = caster.getLocation();
                arrival.getWorld().spawnParticle(
                        Particle.ENCHANT, arrival.clone().add(0D, .9D, 0D), 18, .4D, .65D, .4D, .06D);
                arrival.getWorld().playSound(
                        arrival, Sound.BLOCK_AMETHYST_BLOCK_CHIME, .65F, 1.65F);
            }
        }
        Player ally = session.destination().ally().orElse(null);
        FlightResult result = new FlightResult(
                reason,
                caster,
                ally,
                session.start(),
                caster.getLocation(),
                session.travelledDistance());
        try {
            session.listener().onEnd(result);
        } catch (RuntimeException exception) {
            logger.log(Level.WARNING, "Guardian Flight completion callback failed", exception);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInput(PlayerInputEvent event) {
        Session session = sessions.get(event.getPlayer().getUniqueId());
        if (session == null) return;
        GuardianFlightInputState.Intent intent = session.updateInput(event.getInput());
        switch (intent) {
            case FORWARD_CANCEL -> terminate(session, EndReason.JUMP_CANCELLED,
                    GuardianFlightTrajectory.forwardCancel(
                            event.getPlayer().getEyeLocation().getDirection(),
                            session.travelledDistance()));
            case UPWARD_CANCEL -> terminate(session, EndReason.SNEAK_CANCELLED,
                    GuardianFlightTrajectory.upwardCancel(
                            event.getPlayer().getVelocity(),
                            session.travelledDistance()));
            case NONE -> {
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL
                || !(event.getEntity() instanceof Player player)
                || !sessions.containsKey(player.getUniqueId())) return;
        event.setCancelled(true);
        player.setFallDistance(0F);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        terminateForLifecycle(event.getPlayer(), EndReason.CASTER_INVALID);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        terminateForLifecycle(event.getEntity(), EndReason.CASTER_INVALID);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        terminateForLifecycle(event.getPlayer(), EndReason.TARGET_INVALID);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        terminateForLifecycle(event.getPlayer(), EndReason.TELEPORTED);
    }

    private void terminateForLifecycle(Player player, EndReason reason) {
        Session session = sessions.get(player.getUniqueId());
        if (session != null) terminate(session, reason, null);
    }

    private static Vector flightMomentum(Player caster) {
        Vector momentum = caster.getVelocity();
        // Native gravity leaves downward velocity on grounded players. The floor
        // already blocks that motion; carrying it into steering creates a false obstruction.
        if (caster.isOnGround() && momentum.getY() < 0D) momentum.setY(0D);
        return momentum;
    }

    private boolean collisionClear(Player caster, Vector velocity) {
        double speed = velocity.length();
        if (!Double.isFinite(speed) || speed <= 1.0E-6D) return false;
        Vector direction = velocity.clone().multiply(1D / speed);
        double distance = speed + COLLISION_LOOKAHEAD;
        Vector side = new Vector(-direction.getZ(), 0D, direction.getX());
        if (side.lengthSquared() <= 1.0E-9D) side.setX(1D);
        else side.normalize();
        side.multiply(Math.max(.22D, caster.getWidth() * .45D));
        double height = Math.max(1D, caster.getHeight());
        Location base = caster.getLocation();
        List<Location> samples = List.of(
                base.clone().add(0D, .12D, 0D),
                base.clone().add(0D, height * .5D, 0D),
                base.clone().add(0D, Math.max(.25D, height - .12D), 0D),
                base.clone().add(side).add(0D, height * .5D, 0D),
                base.clone().subtract(side).add(0D, height * .5D, 0D));
        World world = caster.getWorld();
        for (Location sample : samples) {
            RayTraceResult collision = world.rayTraceBlocks(
                    sample,
                    direction,
                    distance,
                    FluidCollisionMode.NEVER,
                    true);
            if (collision != null) return false;
        }
        return true;
    }

    private static EndReason endReason(GuardianFlightSessionPolicy.Decision decision) {
        return switch (decision) {
            case ARRIVED -> EndReason.ARRIVED;
            case OBSTRUCTED -> EndReason.OBSTRUCTED;
            case TARGET_INVALID -> EndReason.TARGET_INVALID;
            case CASTER_INVALID -> EndReason.CASTER_INVALID;
            case OUT_OF_RANGE -> EndReason.OUT_OF_RANGE;
            case EXPIRED -> EndReason.EXPIRED;
            case CONTINUE -> throw new IllegalArgumentException("A continuing flight cannot end");
        };
    }

    private static boolean casterValid(Player caster) {
        try {
            return caster.isOnline()
                    && caster.isValid()
                    && !caster.isDead()
                    && ClassAbilityEligibility.isEligible(caster);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean sameWorld(Location first, Location second) {
        return first.getWorld() != null
                && second.getWorld() != null
                && first.getWorld().equals(second.getWorld());
    }

    private static Location center(Player player) {
        return player.getLocation().add(0D, Math.max(.45D, player.getHeight() * .5D), 0D);
    }

    private static boolean loaded(Location location) {
        World world = location.getWorld();
        return world != null
                && world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        maintenanceTask.cancel();
        for (Session session : List.copyOf(sessions.values()))
            terminate(session, EndReason.SHUTDOWN, null);
        sessions.clear();
        HandlerList.unregisterAll(this);
    }

    enum EndReason {
        ARRIVED,
        JUMP_CANCELLED,
        SNEAK_CANCELLED,
        OBSTRUCTED,
        TARGET_INVALID,
        CASTER_INVALID,
        OUT_OF_RANGE,
        EXPIRED,
        TELEPORTED,
        REPLACED,
        CANCELLED,
        SHUTDOWN
    }

    record FlightResult(
            EndReason reason,
            Player caster,
            Player ally,
            Location start,
            Location end,
            double travelledDistance) {
        FlightResult {
            Objects.requireNonNull(reason, "reason");
            Objects.requireNonNull(caster, "caster");
            Objects.requireNonNull(start, "start");
            Objects.requireNonNull(end, "end");
            start = start.clone();
            end = end.clone();
            if (!Double.isFinite(travelledDistance) || travelledDistance < 0D)
                throw new IllegalArgumentException("Travelled distance must be finite and non-negative");
        }

        boolean arrived() {
            return reason == EndReason.ARRIVED;
        }

        @Override
        public Location start() {
            return start.clone();
        }

        @Override
        public Location end() {
            return end.clone();
        }
    }

    @FunctionalInterface
    interface FlightListener {
        FlightListener NOOP = ignored -> {
        };

        void onEnd(FlightResult result);
    }

    private interface Destination {
        Optional<DestinationSnapshot> resolve(Player caster);

        default Optional<Player> ally() {
            return Optional.empty();
        }
    }

    private record DestinationSnapshot(Location location) {
        private DestinationSnapshot {
            Objects.requireNonNull(location, "location");
            location = location.clone();
        }

        @Override
        public Location location() {
            return location.clone();
        }
    }

    private static final class AllyDestination implements Destination {
        private final Player ally;
        private final BiPredicate<Player, Player> validator;

        private AllyDestination(Player ally, BiPredicate<Player, Player> validator) {
            this.ally = ally;
            this.validator = validator;
        }

        @Override
        public Optional<DestinationSnapshot> resolve(Player caster) {
            if (ally.getUniqueId().equals(caster.getUniqueId())
                    || !ally.isOnline()
                    || !ally.isValid()
                    || ally.isDead()
                    || !ally.getWorld().equals(caster.getWorld())) return Optional.empty();
            boolean valid;
            try {
                valid = validator.test(caster, ally);
            } catch (RuntimeException ignored) {
                valid = false;
            }
            return valid ? Optional.of(new DestinationSnapshot(center(ally))) : Optional.empty();
        }

        @Override
        public Optional<Player> ally() {
            return Optional.of(ally);
        }
    }

    private static final class AnchorDestination implements Destination {
        private final Location anchor;

        private AnchorDestination(Location anchor) {
            this.anchor = anchor.clone();
        }

        @Override
        public Optional<DestinationSnapshot> resolve(Player caster) {
            return sameWorld(caster.getLocation(), anchor) && loaded(anchor)
                    ? Optional.of(new DestinationSnapshot(anchor))
                    : Optional.empty();
        }
    }

    private static final class Session {
        private final Player caster;
        private final Destination destination;
        private final double maximumTargetDistance;
        private final int maximumTicks;
        private final double arrivalDistance;
        private final FlightListener listener;
        private final Location start;
        private Location previousLocation;
        private double travelledDistance;
        private int elapsedTicks;
        private int stalledTicks;
        private GuardianFlightInputState inputState;

        private Session(
                Player caster,
                Destination destination,
                double maximumTargetDistance,
                int maximumTicks,
                double arrivalDistance,
                FlightListener listener) {
            this.caster = caster;
            this.destination = destination;
            this.maximumTargetDistance = maximumTargetDistance;
            this.maximumTicks = maximumTicks;
            this.arrivalDistance = arrivalDistance;
            this.listener = listener;
            this.start = caster.getLocation().clone();
            this.previousLocation = start.clone();
            Input input = caster.getCurrentInput();
            this.inputState = input == null
                    ? new GuardianFlightInputState(false, false)
                    : new GuardianFlightInputState(input.isJump(), input.isSneak());
        }

        void captureProgress(Location current) {
            double moved = sameWorld(previousLocation, current)
                    ? previousLocation.distance(current)
                    : 0D;
            if (moved >= MINIMUM_PROGRESS_PER_TICK) {
                travelledDistance += moved;
                stalledTicks = 0;
            } else if (elapsedTicks > 0) {
                stalledTicks++;
            }
            previousLocation = current.clone();
        }

        void elapseTick() {
            elapsedTicks++;
        }

        Player caster() {
            return caster;
        }

        Destination destination() {
            return destination;
        }

        double maximumTargetDistance() {
            return maximumTargetDistance;
        }

        int maximumTicks() {
            return maximumTicks;
        }

        double arrivalDistance() {
            return arrivalDistance;
        }

        FlightListener listener() {
            return listener;
        }

        Location start() {
            return start.clone();
        }

        double travelledDistance() {
            return travelledDistance;
        }

        int elapsedTicks() {
            return elapsedTicks;
        }

        int stalledTicks() {
            return stalledTicks;
        }

        GuardianFlightInputState.Intent updateInput(Input input) {
            GuardianFlightInputState.Transition transition = inputState.update(
                    input != null && input.isJump(),
                    input != null && input.isSneak());
            inputState = transition.next();
            return transition.intent();
        }
    }
}
