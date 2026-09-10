package com.magmaguy.elitemobs.advancedcombat.abilities;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.OptionalDouble;

/** Plans Blink along the walkable ground in the direction the player is looking. */
final class BlinkPlanner {
    private static final double MINIMUM_DISTANCE = GroundBlinkPath.MINIMUM_DISTANCE;
    private static final double HEIGHT_INCREMENT = 1D / 16D;
    private static final int HEIGHT_STEPS = 16;
    private static final double POSITION_EPSILON_SQUARED = 1.0E-8D;

    private BlinkPlanner() {
    }

    static Optional<Plan> plan(Player player, double maximumDistance) {
        if (!Double.isFinite(maximumDistance) || maximumDistance < MINIMUM_DISTANCE)
            return Optional.empty();

        Location start = player.getLocation().clone();
        if (!SafeMovement.safeStandingPlayerVolume(player, start)) return Optional.empty();

        return GroundBlinkPath.scan(
                        start.toVector(),
                        player.getEyeLocation().getDirection(),
                        maximumDistance,
                        (x, z, previousY) -> standingHeight(player, start, x, z, previousY))
                .map(destination -> new Plan(
                        start,
                        new Location(
                                start.getWorld(),
                                destination.x(),
                                destination.y(),
                                destination.z(),
                                start.getYaw(),
                                start.getPitch()),
                        destination.distance(),
                        maximumDistance));
    }

    /** Re-runs the ground scan immediately before the teleport side effect. */
    static boolean stillValid(Player player, Plan accepted) {
        Location current = player.getLocation();
        if (!samePosition(current, accepted.start())) return false;
        Optional<Plan> currentPlan = plan(player, accepted.maximumDistance());
        return currentPlan.isPresent()
                && samePosition(currentPlan.get().destination(), accepted.destination());
    }

    private static OptionalDouble standingHeight(
            Player player,
            Location origin,
            double x,
            double z,
            double previousY) {
        Location candidate = new Location(
                origin.getWorld(), x, previousY, z, origin.getYaw(), origin.getPitch());
        if (SafeMovement.safeStandingPlayerVolume(player, candidate))
            return OptionalDouble.of(previousY);

        for (int step = 1; step <= HEIGHT_STEPS; step++) {
            double offset = step * HEIGHT_INCREMENT;
            candidate.setY(previousY + offset);
            if (SafeMovement.safeStandingPlayerVolume(player, candidate))
                return OptionalDouble.of(candidate.getY());

            candidate.setY(previousY - offset);
            if (SafeMovement.safeStandingPlayerVolume(player, candidate))
                return OptionalDouble.of(candidate.getY());
        }
        return OptionalDouble.empty();
    }

    private static boolean samePosition(Location first, Location second) {
        return first.getWorld() != null
                && second.getWorld() != null
                && first.getWorld().equals(second.getWorld())
                && first.distanceSquared(second) <= POSITION_EPSILON_SQUARED;
    }

    record Plan(Location start, Location destination, double distance, double maximumDistance) {
        Plan {
            start = start.clone();
            destination = destination.clone();
            if (!Double.isFinite(distance) || distance < 0D
                    || !Double.isFinite(maximumDistance) || maximumDistance < MINIMUM_DISTANCE)
                throw new IllegalArgumentException("Blink distances must be finite and valid");
        }

        @Override
        public Location start() {
            return start.clone();
        }

        @Override
        public Location destination() {
            return destination.clone();
        }
    }
}
