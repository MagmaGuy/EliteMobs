package com.magmaguy.elitemobs.pathfinding.patrol;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

/** World-name based route origin which does not pin an unloaded Bukkit world. */
public record PatrolOrigin(
        String worldName,
        double x,
        double y,
        double z,
        float yaw,
        float pitch) {

    public PatrolOrigin {
        Objects.requireNonNull(worldName, "worldName");
        if (worldName.isBlank()) throw new IllegalArgumentException("worldName cannot be blank");
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("Patrol origin coordinates must be finite");
        }
    }

    public static PatrolOrigin from(Location location, String fallbackWorldName) {
        Objects.requireNonNull(location, "location");
        String worldName = location.getWorld() == null ? fallbackWorldName : location.getWorld().getName();
        return new PatrolOrigin(
                Objects.requireNonNull(worldName, "Patrol origin has no world"),
                location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public Location resolve() {
        World world = Bukkit.getWorld(worldName);
        return world == null ? null : new Location(world, x, y, z, yaw, pitch);
    }

    public String identityFragment() {
        return worldName + '|' + Double.toHexString(x) + '|' + Double.toHexString(y) + '|'
                + Double.toHexString(z);
    }
}
