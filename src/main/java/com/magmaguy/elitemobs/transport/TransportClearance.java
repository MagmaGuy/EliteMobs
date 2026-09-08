package com.magmaguy.elitemobs.transport;

import com.magmaguy.magmacore.ai.route.CurvedRoute;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

/** Conservative passenger clearance, evaluated against the curve rather than just its waypoints. */
final class TransportClearance {
    static String point(World world, Vector point) {
        if (point.getY() < world.getMinHeight() || point.getY() + 3.5 >= world.getMaxHeight()) return "world height";
        if (!world.getWorldBorder().isInside(point.toLocation(world))) return "world border";
        for (int x = (int) Math.floor(point.getX() - .65); x <= Math.floor(point.getX() + .65); x++)
            for (int z = (int) Math.floor(point.getZ() - .65); z <= Math.floor(point.getZ() + .65); z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) return "unloaded terrain";
                for (int y = (int) Math.floor(point.getY() + .05); y <= Math.floor(point.getY() + 3.3); y++)
                    if (!world.getBlockAt(x, y, z).isPassable()) return "blocked at " + x + "," + y + "," + z;
            }
        return null;
    }
    static String route(World world, CurvedRoute curve) {
        for (double distance = 0; distance < curve.length(); distance += .5) {
            String reason = point(world, curve.at(distance));
            if (reason != null) return reason;
        }
        return point(world, curve.at(curve.length()));
    }
    static boolean landing(Location location) {
        World world = location.getWorld();
        return world != null && point(world, location.toVector()) == null
                && !world.getBlockAt(location.clone().subtract(0, .1, 0)).isPassable();
    }
}
