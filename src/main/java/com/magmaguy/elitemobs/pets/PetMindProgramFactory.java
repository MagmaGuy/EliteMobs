package com.magmaguy.elitemobs.pets;

import com.magmaguy.magmacore.ai.MindBehavior;
import com.magmaguy.magmacore.ai.MindContext;
import com.magmaguy.magmacore.ai.MindControl;
import com.magmaguy.magmacore.ai.MindProgram;
import com.magmaguy.magmacore.ai.MindStopReason;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The Mind program every floating pet runs: stay where told, keep up with the owner on a leash, drift around the
 * owner or the home spot while settled, and hover in place between drifts. Each behavior leases MOVE and LOOK, so
 * the lowest priority number that can start owns the body. The body is steered directly: a flying mob's navigation
 * accelerates by the square of a tiny attribute and crawls, while a velocity per tick gives the glide a jellyfish
 * should have. Speeds are blocks per tick scaled by the species' value.
 */
final class PetMindProgramFactory {
    private PetMindProgramFactory() {
    }

    static MindProgram create(Plugin plugin, PetControl control) {
        String owner = control.isWild() ? "wild" : "owned";
        return MindProgram.builder(new NamespacedKey(plugin, "pet/" + control.species().id() + "/" + owner).toString(), 1L)
                .behavior(new StayBehavior(control))
                .behavior(new FollowOwnerBehavior(control))
                .behavior(new DriftBehavior(control))
                .behavior(new HoverBehavior(control))
                .build();
    }

    private static final Set<MindControl> MOVE_AND_LOOK = EnumSet.of(MindControl.MOVE, MindControl.LOOK);
    // Blocks per tick at species speed 1: a brisk follow (5 blocks/s), a lazy drift, a barely visible bob.
    private static final double FOLLOW_SPEED = 0.25D, LEASH_URGENCY = 1.6D, DRIFT_SPEED = 0.06D, BOB_SPEED = 0.03D;

    /** A point near {@code center}, between the species' hover band above the ground under it. */
    static Location hoverPoint(PetControl control, Location center, double radius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double angle = random.nextDouble() * Math.PI * 2D;
        double distance = radius <= 0D ? 0D : random.nextDouble() * radius;
        Location point = center.clone().add(Math.cos(angle) * distance, 0D, Math.sin(angle) * distance);
        double ground = groundBelow(point, 12);
        double height = control.species().hoverHeight() + (random.nextDouble() * 2D - 1D) * control.species().hoverVariance();
        point.setY(Math.max(ground + Math.max(0.5D, height), center.getY() - 3D));
        return point;
    }

    /** The top of the highest solid block under the point, searched a bounded number of blocks down. */
    static double groundBelow(Location point, int depth) {
        World world = point.getWorld();
        int x = point.getBlockX(), z = point.getBlockZ();
        for (int y = point.getBlockY(); y > point.getBlockY() - depth && y > world.getMinHeight(); y--) {
            Block block = world.getBlockAt(x, y, z);
            if (block.getType().isSolid()) return y + 1D;
        }
        return point.getY() - depth;
    }

    private static boolean loaded(Location location) {
        return location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    /**
     * One tick of steering toward a point: accelerate to the cruise speed, ease off over the last few blocks, and
     * hold still on arrival. The body control turns the model toward the travel direction on its own.
     */
    private static void steer(MindContext context, Location target, double cruise) {
        LivingEntity body = context.entity();
        Vector to = target.toVector().subtract(body.getLocation().toVector());
        double distance = to.length();
        Vector current = body.getVelocity();
        Vector wanted = distance < 0.05D ? new Vector() : to.multiply(Math.min(cruise, distance * 0.25D) / distance);
        Vector change = wanted.subtract(current);
        double acceleration = Math.max(cruise * 0.15D, 0.005D);
        if (change.length() > acceleration) change.multiply(acceleration / change.length());
        context.actuator().steerFlight(current.add(change));
    }

    /** Told to stay: hold the spot, with a gentle bob so it never looks parked. */
    private static final class StayBehavior implements MindBehavior {
        private final PetControl control;
        private Location target;
        private long nextBobTick;

        private StayBehavior(PetControl control) {
            this.control = control;
        }

        @Override
        public String identifier() {
            return "elitemobs:pet/stay";
        }

        @Override
        public int priority() {
            return 5;
        }

        @Override
        public Set<MindControl> controls() {
            return MOVE_AND_LOOK;
        }

        @Override
        public boolean canStart(MindContext context) {
            return control.isStaying() && control.stayAnchor() != null;
        }

        @Override
        public void tick(MindContext context) {
            Location anchor = control.stayAnchor();
            if (anchor == null) return;
            if (target == null || context.gameTick() >= nextBobTick) {
                nextBobTick = context.gameTick() + 40L;
                target = hoverPoint(control, anchor, 0.5D);
            }
            steer(context, target, BOB_SPEED * control.species().speed());
        }

        @Override
        public void stop(MindContext context, MindStopReason reason) {
            target = null;
            context.actuator().stopMoving();
        }
    }

    /** Keep up with the owner once they get further than the follow distance; teleport when they get away. */
    private static final class FollowOwnerBehavior implements MindBehavior {
        private final PetControl control;

        private FollowOwnerBehavior(PetControl control) {
            this.control = control;
        }

        @Override
        public String identifier() {
            return "elitemobs:pet/follow_owner";
        }

        @Override
        public int priority() {
            return 10;
        }

        @Override
        public Set<MindControl> controls() {
            return MOVE_AND_LOOK;
        }

        @Override
        public boolean canStart(MindContext context) {
            if (control.isStaying()) return false;
            double distance = control.ownerDistance(context.entity());
            return control.owner().isPresent() && (distance > control.species().followDistance() || Double.isInfinite(distance));
        }

        @Override
        public boolean canContinue(MindContext context) {
            if (control.isStaying() || control.owner().isEmpty()) return false;
            return control.ownerDistance(context.entity()) > control.species().followDistance() * 0.6D;
        }

        @Override
        public void tick(MindContext context) {
            Player owner = control.owner().orElse(null);
            LivingEntity body = context.entity();
            if (owner == null) return;
            double distance = control.ownerDistance(body);
            if (Double.isInfinite(distance) || distance > control.species().teleportDistance()) {
                Location beside = hoverPoint(control, owner.getLocation(), 2D);
                if (loaded(beside)) body.teleport(beside);
                return;
            }
            // Aim a little behind and above the owner, so it trails rather than sits in their face.
            Location target = owner.getLocation().clone().subtract(owner.getLocation().getDirection().setY(0).normalize().multiply(1.5D));
            target.setY(owner.getLocation().getY() + control.species().hoverHeight());
            double urgency = distance > control.species().leashDistance() ? LEASH_URGENCY : 1.0D;
            steer(context, target, FOLLOW_SPEED * urgency * control.species().speed());
        }

        @Override
        public void stop(MindContext context, MindStopReason reason) {
        }
    }

    /** Settled: a slow drift to a random point around the owner (or the home spot when wild), then rest. */
    private static final class DriftBehavior implements MindBehavior {
        private final PetControl control;
        private long nextDriftTick;
        private long stopAtTick;
        private Location destination;
        private Vector lastPosition;
        private int stuckTicks;

        private DriftBehavior(PetControl control) {
            this.control = control;
        }

        @Override
        public String identifier() {
            return "elitemobs:pet/drift";
        }

        @Override
        public int priority() {
            return 20;
        }

        @Override
        public Set<MindControl> controls() {
            return MOVE_AND_LOOK;
        }

        private Location center(MindContext context) {
            if (control.isWild()) return control.home() != null ? control.home() : context.entity().getLocation();
            return control.owner().map(Player::getLocation).orElse(null);
        }

        @Override
        public boolean canStart(MindContext context) {
            if (control.isStaying() || context.gameTick() < nextDriftTick) return false;
            Location center = center(context);
            return center != null && center.getWorld() == context.entity().getWorld();
        }

        @Override
        public boolean canContinue(MindContext context) {
            if (destination == null || context.gameTick() >= stopAtTick || control.isStaying()) return false;
            if (context.entity().getLocation().distanceSquared(destination) < 0.25D) return false;
            // Blocked by something: give up this drift rather than push against it.
            return stuckTicks < 20;
        }

        @Override
        public void start(MindContext context) {
            Location center = center(context);
            if (center == null) return;
            destination = hoverPoint(control, center, control.species().roamRadius());
            if (!loaded(destination)) {
                destination = null;
                nextDriftTick = context.gameTick() + 40L;
                return;
            }
            stopAtTick = context.gameTick() + 160L;
            lastPosition = context.entity().getLocation().toVector();
            stuckTicks = 0;
        }

        @Override
        public void tick(MindContext context) {
            if (destination == null) return;
            Vector now = context.entity().getLocation().toVector();
            if (lastPosition != null && now.distanceSquared(lastPosition) < 1.0E-4D) stuckTicks++;
            else stuckTicks = 0;
            lastPosition = now;
            steer(context, destination, DRIFT_SPEED * control.species().speed());
        }

        @Override
        public void stop(MindContext context, MindStopReason reason) {
            destination = null;
            nextDriftTick = context.gameTick() + 60L + ThreadLocalRandom.current().nextInt(80);
        }
    }

    /** Between drifts: hold position with a small bob, so the model never sits dead still. */
    private static final class HoverBehavior implements MindBehavior {
        private final PetControl control;
        private Location target;
        private long nextBobTick;

        private HoverBehavior(PetControl control) {
            this.control = control;
        }

        @Override
        public String identifier() {
            return "elitemobs:pet/hover";
        }

        @Override
        public int priority() {
            return 30;
        }

        @Override
        public Set<MindControl> controls() {
            return MOVE_AND_LOOK;
        }

        @Override
        public boolean canStart(MindContext context) {
            return !control.isStaying();
        }

        @Override
        public void tick(MindContext context) {
            Location here = context.entity().getLocation();
            if (target == null || context.gameTick() >= nextBobTick) {
                nextBobTick = context.gameTick() + 30L;
                double ground = groundBelow(here, 12);
                double wanted = ground + control.species().hoverHeight();
                double bob = (ThreadLocalRandom.current().nextDouble() - 0.5D) * control.species().hoverVariance();
                target = here.clone();
                target.setY(Math.abs(here.getY() - wanted) > 1.5D ? wanted : here.getY() + bob);
            }
            steer(context, target, BOB_SPEED * control.species().speed());
        }

        @Override
        public void stop(MindContext context, MindStopReason reason) {
            target = null;
        }
    }
}
