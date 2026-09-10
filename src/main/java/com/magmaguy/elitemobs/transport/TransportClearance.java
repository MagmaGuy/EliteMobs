package com.magmaguy.elitemobs.transport;

import com.magmaguy.magmacore.ai.route.CurvedRoute;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/** Conservative passenger clearance, evaluated against the curve rather than just its waypoints. */
final class TransportClearance {
    /** Actual collision dimensions, including entity scale; no transport-specific size ceiling. */
    record Footprint(double radius, double height) {
        Footprint {
            if (!Double.isFinite(radius) || !Double.isFinite(height) || radius <= 0 || height <= 0)
                throw new IllegalArgumentException("Transport collision dimensions must be finite and positive");
        }
        boolean contains(Footprint other) {
            // Bounding boxes lose tiny amounts of precision as world coordinates change.
            return radius + 1.0e-6 >= other.radius && height + 1.0e-6 >= other.height;
        }
    }

    static Footprint player(Player player) {
        var bounds = player.getBoundingBox();
        // Eye height also covers a rider who stood up after starting while crouched.
        return new Footprint(Math.max(bounds.getWidthX(), bounds.getWidthZ()) / 2,
                Math.max(bounds.getHeight(), player.getEyeHeight(true) + bounds.getHeight() - player.getEyeHeight()));
    }

    static Footprint mounted(LivingEntity mount, Player player) {
        var body = mount.getBoundingBox();
        Footprint rider = player(player);
        double radius = Math.max(Math.max(body.getWidthX(), body.getWidthZ()) / 2, rider.radius());
        double height = body.getHeight() + rider.height();
        return new Footprint(radius, height);
    }

    static String point(World world, Vector point, Footprint footprint) {
        if (point.getY() < world.getMinHeight() || point.getY() + footprint.height() >= world.getMaxHeight()) return "world height";
        double halfWidth = footprint.radius();
        for (double x : new double[]{point.getX() - halfWidth, point.getX() + halfWidth})
            for (double z : new double[]{point.getZ() - halfWidth, point.getZ() + halfWidth})
                if (!world.getWorldBorder().isInside(new Location(world, x, point.getY(), z))) return "world border";
        for (int x = (int) Math.floor(point.getX() - halfWidth); x <= Math.floor(point.getX() + halfWidth); x++)
            for (int z = (int) Math.floor(point.getZ() - halfWidth); z <= Math.floor(point.getZ() + halfWidth); z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) return "unloaded terrain";
                for (int y = (int) Math.floor(point.getY() + .05); y <= Math.floor(point.getY() + footprint.height()); y++)
                    if (!world.getBlockAt(x, y, z).isPassable()) return "blocked at " + x + "," + y + "," + z;
            }
        return null;
    }
    static String route(World world, CurvedRoute curve, Footprint footprint) {
        for (double distance = 0; distance < curve.length(); distance += .5) {
            String reason = point(world, curve.at(distance), footprint);
            if (reason != null) return reason;
        }
        return point(world, curve.at(curve.length()), footprint);
    }
    static boolean landing(Location location, Player player) {
        World world = location.getWorld();
        return world != null && point(world, location.toVector(), player(player)) == null
                && !world.getBlockAt(location.clone().subtract(0, .1, 0)).isPassable();
    }
}
