package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.Set;

/**
 * Collision-shape-aware primitives with explicit movement-family policies.
 *
 * <p>There is intentionally no generic teleport policy here. Blink owns its forward-ground scan
 * in {@link BlinkPlanner}; flight and charge retain their separate movement contracts.</p>
 */
final class SafeMovement {
    private static final double ROUTE_STEP = .125D;
    private static final double BODY_EPSILON = 1.0E-4D;
    private static final double CONTACT_EPSILON = 1.0E-7D;
    private static final double SUPPORT_DEPTH = .075D;
    private static final double SUPPORT_HALF_WIDTH = .12D;
    private static final double STANDING_HALF_WIDTH = .3D;
    private static final double STANDING_HEIGHT = 1.8D;
    private static final Set<Material> UNSAFE_FOOTING = EnumSet.of(
            Material.LAVA,
            Material.FIRE,
            Material.SOUL_FIRE,
            Material.MAGMA_BLOCK,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.CACTUS,
            Material.SWEET_BERRY_BUSH,
            Material.WITHER_ROSE,
            Material.POWDER_SNOW,
            Material.POINTED_DRIPSTONE,
            Material.COBWEB);

    private SafeMovement() {
    }

    /**
     * Returns the unobstructed horizontal distance available for a vanilla-physics dash impulse.
     * It deliberately makes no landing promise: gravity and Bukkit collision resolve the actual
     * movement, so a dash can leave a ledge but cannot be aimed through a wall.
     */
    static double physicsDashClearDistance(Player player, double maximumDistance) {
        if (!validDistance(maximumDistance)) return 0D;
        Vector direction = horizontalFacing(player);
        if (direction == null) return 0D;
        Location start = player.getLocation();
        Location previous = start.clone();
        double lastClearDistance = 0D;
        int steps = Math.max(1, (int) Math.ceil(maximumDistance / ROUTE_STEP));
        for (int step = 1; step <= steps; step++) {
            double distance = Math.min(step * ROUTE_STEP, maximumDistance);
            Location candidate = start.clone().add(direction.clone().multiply(distance));
            if (!bodyPathClear(player, previous, candidate)) break;
            previous = candidate;
            lastClearDistance = distance;
        }
        return lastClearDistance;
    }

    /**
     * Uses at least the normal standing-player volume even if the caster is currently crouching,
     * swimming or gliding. Blink uses this conservative shape so a pose change after teleport
     * cannot place the player's head inside collision geometry.
     */
    static boolean safeStandingPlayerVolume(Player player, Location location) {
        BoundingBox body = standingBodyAt(player, location);
        return safeStanding(
                player,
                location,
                body,
                Math.max(SUPPORT_HALF_WIDTH, body.getWidthX() * .5D),
                Math.max(SUPPORT_HALF_WIDTH, body.getWidthZ() * .5D));
    }

    private static boolean safeStanding(
            Player player,
            Location location,
            BoundingBox body,
            double supportHalfWidthX,
            double supportHalfWidthZ) {
        World world = location.getWorld();
        if (world == null || !world.equals(player.getWorld())
                || !volumeAvailable(player, world, body)
                || intersectsCollision(world, body))
            return false;
        BoundingBox supportProbe = new BoundingBox(
                body.getCenterX() - supportHalfWidthX,
                location.getY() - SUPPORT_DEPTH,
                body.getCenterZ() - supportHalfWidthZ,
                body.getCenterX() + supportHalfWidthX,
                location.getY() + BODY_EPSILON,
                body.getCenterZ() + supportHalfWidthZ);
        return intersectsCollision(world, supportProbe)
                && !containsUnsafeFooting(world, union(body, supportProbe));
    }

    /**
     * Checks the continuously swept player volume, not merely the center line or destination.
     * This deliberately rejects diagonal corner clipping and routes through thin collision shapes.
     */
    static boolean bodyPathClear(Player player, Location from, Location to) {
        return bodyPathClear(player, from, to, bodyAt(player, from), bodyAt(player, to));
    }

    private static boolean bodyPathClear(
            Player player,
            Location from,
            Location to,
            BoundingBox start,
            BoundingBox end) {
        World world = from.getWorld();
        if (world == null || to.getWorld() == null || !world.equals(to.getWorld())
                || !world.equals(player.getWorld())) return false;

        BoundingBox swept = union(start, end);
        if (!volumeAvailable(player, world, swept)) return false;
        if (intersectsCollision(world, start) || intersectsCollision(world, end)) return false;

        Vector movement = to.toVector().subtract(from.toVector());
        if (movement.lengthSquared() < 1.0E-12D) return true;
        for (int x = minimumBlock(swept.getMinX()); x <= maximumBlock(swept.getMaxX()); x++) {
            for (int y = minimumBlock(swept.getMinY()); y <= maximumBlock(swept.getMaxY()); y++) {
                for (int z = minimumBlock(swept.getMinZ()); z <= maximumBlock(swept.getMaxZ()); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    for (BoundingBox local : block.getCollisionShape().getBoundingBoxes()) {
                        BoundingBox obstacle = absolute(local, x, y, z);
                        if (swept.overlaps(obstacle) && sweptAabbIntersects(start, movement, obstacle))
                            return false;
                    }
                }
            }
        }
        return true;
    }

    private static BoundingBox bodyAt(Player player, Location location) {
        Location current = player.getLocation();
        BoundingBox currentBox = player.getBoundingBox();
        double x = location.getX() - current.getX();
        double y = location.getY() - current.getY();
        double z = location.getZ() - current.getZ();
        return new BoundingBox(
                currentBox.getMinX() + x + BODY_EPSILON,
                currentBox.getMinY() + y + BODY_EPSILON,
                currentBox.getMinZ() + z + BODY_EPSILON,
                currentBox.getMaxX() + x - BODY_EPSILON,
                currentBox.getMaxY() + y - BODY_EPSILON,
                currentBox.getMaxZ() + z - BODY_EPSILON);
    }

    private static BoundingBox standingBodyAt(Player player, Location location) {
        BoundingBox currentBox = player.getBoundingBox();
        double halfWidthX = Math.max(STANDING_HALF_WIDTH, currentBox.getWidthX() * .5D);
        double halfWidthZ = Math.max(STANDING_HALF_WIDTH, currentBox.getWidthZ() * .5D);
        double scaleFromWidth = Math.max(halfWidthX, halfWidthZ) / STANDING_HALF_WIDTH;
        double height = Math.max(
                Math.max(STANDING_HEIGHT, currentBox.getHeight()),
                STANDING_HEIGHT * scaleFromWidth);
        return new BoundingBox(
                location.getX() - halfWidthX + BODY_EPSILON,
                location.getY() + BODY_EPSILON,
                location.getZ() - halfWidthZ + BODY_EPSILON,
                location.getX() + halfWidthX - BODY_EPSILON,
                location.getY() + height - BODY_EPSILON,
                location.getZ() + halfWidthZ - BODY_EPSILON);
    }

    private static boolean volumeAvailable(Player player, World world, BoundingBox volume) {
        if (volume.getMinY() < world.getMinHeight() || volume.getMaxY() >= world.getMaxHeight()) return false;
        for (int chunkX = minimumBlock(volume.getMinX()) >> 4;
             chunkX <= maximumBlock(volume.getMaxX()) >> 4; chunkX++) {
            for (int chunkZ = minimumBlock(volume.getMinZ()) >> 4;
                 chunkZ <= maximumBlock(volume.getMaxZ()) >> 4; chunkZ++) {
                if (!world.isChunkLoaded(chunkX, chunkZ)) return false;
            }
        }

        WorldBorder border = player.getWorldBorder();
        if (border == null) border = world.getWorldBorder();
        double y = Math.max(world.getMinHeight(), Math.min(world.getMaxHeight() - 1D, volume.getMinY()));
        return border.isInside(new Location(world, volume.getMinX(), y, volume.getMinZ()))
                && border.isInside(new Location(world, volume.getMinX(), y, volume.getMaxZ()))
                && border.isInside(new Location(world, volume.getMaxX(), y, volume.getMinZ()))
                && border.isInside(new Location(world, volume.getMaxX(), y, volume.getMaxZ()));
    }

    private static boolean intersectsCollision(World world, BoundingBox body) {
        for (int x = minimumBlock(body.getMinX()); x <= maximumBlock(body.getMaxX()); x++) {
            for (int y = minimumBlock(body.getMinY()); y <= maximumBlock(body.getMaxY()); y++) {
                for (int z = minimumBlock(body.getMinZ()); z <= maximumBlock(body.getMaxZ()); z++) {
                    BoundingBox localBody = new BoundingBox(
                            body.getMinX() - x, body.getMinY() - y, body.getMinZ() - z,
                            body.getMaxX() - x, body.getMaxY() - y, body.getMaxZ() - z);
                    if (world.getBlockAt(x, y, z).getCollisionShape().overlaps(localBody)) return true;
                }
            }
        }
        return false;
    }

    private static boolean containsUnsafeFooting(World world, BoundingBox volume) {
        for (int x = minimumBlock(volume.getMinX()); x <= maximumBlock(volume.getMaxX()); x++) {
            for (int y = minimumBlock(volume.getMinY()); y <= maximumBlock(volume.getMaxY()); y++) {
                for (int z = minimumBlock(volume.getMinZ()); z <= maximumBlock(volume.getMaxZ()); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.isLiquid() || UNSAFE_FOOTING.contains(block.getType())) return true;
                }
            }
        }
        return false;
    }

    private static boolean sweptAabbIntersects(BoundingBox moving, Vector delta, BoundingBox obstacle) {
        AxisTimes x = axisTimes(moving.getMinX(), moving.getMaxX(), delta.getX(),
                obstacle.getMinX(), obstacle.getMaxX());
        if (x == null) return false;
        AxisTimes y = axisTimes(moving.getMinY(), moving.getMaxY(), delta.getY(),
                obstacle.getMinY(), obstacle.getMaxY());
        if (y == null) return false;
        AxisTimes z = axisTimes(moving.getMinZ(), moving.getMaxZ(), delta.getZ(),
                obstacle.getMinZ(), obstacle.getMaxZ());
        if (z == null) return false;
        double entry = Math.max(x.entry(), Math.max(y.entry(), z.entry()));
        double exit = Math.min(x.exit(), Math.min(y.exit(), z.exit()));
        return entry < exit - CONTACT_EPSILON
                && exit > CONTACT_EPSILON
                && entry < 1D - CONTACT_EPSILON;
    }

    private static AxisTimes axisTimes(
            double movingMin,
            double movingMax,
            double delta,
            double obstacleMin,
            double obstacleMax) {
        if (Math.abs(delta) < 1.0E-12D) {
            if (movingMax <= obstacleMin + CONTACT_EPSILON
                    || movingMin >= obstacleMax - CONTACT_EPSILON) return null;
            return new AxisTimes(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
        double first = (obstacleMin - movingMax) / delta;
        double second = (obstacleMax - movingMin) / delta;
        return new AxisTimes(Math.min(first, second), Math.max(first, second));
    }

    private static BoundingBox absolute(BoundingBox local, int x, int y, int z) {
        return new BoundingBox(
                local.getMinX() + x, local.getMinY() + y, local.getMinZ() + z,
                local.getMaxX() + x, local.getMaxY() + y, local.getMaxZ() + z);
    }

    private static BoundingBox union(BoundingBox first, BoundingBox second) {
        return new BoundingBox(
                Math.min(first.getMinX(), second.getMinX()),
                Math.min(first.getMinY(), second.getMinY()),
                Math.min(first.getMinZ(), second.getMinZ()),
                Math.max(first.getMaxX(), second.getMaxX()),
                Math.max(first.getMaxY(), second.getMaxY()),
                Math.max(first.getMaxZ(), second.getMaxZ()));
    }

    private static Vector horizontalFacing(Player player) {
        Vector direction = player.getEyeLocation().getDirection().setY(0);
        return direction.lengthSquared() < 1.0E-6D ? null : direction.normalize();
    }

    private static boolean validDistance(double distance) {
        return Double.isFinite(distance) && distance > 0D;
    }

    private static int minimumBlock(double coordinate) {
        return (int) Math.floor(coordinate);
    }

    private static int maximumBlock(double coordinate) {
        return (int) Math.floor(coordinate - BODY_EPSILON);
    }

    private record AxisTimes(double entry, double exit) {
    }
}
