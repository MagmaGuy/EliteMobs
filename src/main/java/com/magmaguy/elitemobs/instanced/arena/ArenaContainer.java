package com.magmaguy.elitemobs.instanced.arena;

import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfigFields;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.magmacore.scripting.zones.Cylinder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** One physical arena. Run definitions share its geometry and exclusive occupancy. */
public final class ArenaContainer {
    private final Location corner1;
    private final Location corner2;
    private final Location start;
    private final Location exit;
    private final Location lobby;
    private final Cylinder cylinder;
    private final Map<String, Location> spawnPoints;
    private MatchInstance occupant;

    ArenaContainer(CustomArenasConfigFields fields, Location corner1, Location corner2,
                   Location start, Location exit) {
        this.corner1 = corner1.clone();
        this.corner2 = corner2.clone();
        this.start = start.clone();
        this.exit = exit.clone();
        this.lobby = fields.getTeleportLocation() == null ? null : fields.getTeleportLocation().clone();
        if (!Objects.equals(corner1.getWorld(), corner2.getWorld())
                || !Objects.equals(corner1.getWorld(), start.getWorld()))
            throw new IllegalArgumentException("Arena corners and start must belong to the same world");
        double minY = Math.min(corner1.getY(), corner2.getY());
        cylinder = fields.isCylindricalArena() ? new Cylinder(
                new Vector((corner1.getX() + corner2.getX()) / 2, minY,
                        (corner1.getZ() + corner2.getZ()) / 2),
                Math.abs(corner1.getX() - corner2.getX()) / 2,
                Math.abs(corner1.getY() - corner2.getY())) : null;
        Map<String, Location> points = new LinkedHashMap<>();
        for (String entry : fields.getSpawnPoints()) {
            String name = null;
            Location location = null;
            for (String part : entry.split(":")) {
                String[] pair = part.split("=", 2);
                if (pair.length != 2) continue;
                if (pair[0].equalsIgnoreCase("name")) name = pair[1];
                if (pair[0].equalsIgnoreCase("location")) location = ConfigurationLocation.serialize(pair[1]);
            }
            if (name == null || location == null || !Objects.equals(start.getWorld(), location.getWorld()))
                throw new IllegalArgumentException("Invalid arena spawn point: " + entry);
            points.put(name, location);
        }
        spawnPoints = Map.copyOf(points);
    }

    public Location start() { return start.clone(); }
    public Location exit() { return exit.clone(); }
    public Location lobby() { return lobby == null ? null : lobby.clone(); }
    public Location spawnPoint(String name) {
        Location point = spawnPoints.get(name);
        return point == null ? null : point.clone();
    }

    public boolean contains(Location location) {
        if (location == null || !Objects.equals(start.getWorld(), location.getWorld())) return false;
        if (cylinder != null) return cylinder.contains(location);
        return between(location.getX(), corner1.getX(), corner2.getX())
                && between(location.getY(), corner1.getY(), corner2.getY())
                && between(location.getZ(), corner1.getZ(), corner2.getZ());
    }

    public boolean availableTo(MatchInstance run) { return occupant == null || occupant == run; }
    public boolean occupied() { return occupant != null; }

    public boolean acquire(MatchInstance run) {
        requireMainThread();
        if (!availableTo(run)) return false;
        occupant = Objects.requireNonNull(run);
        return true;
    }

    public void release(MatchInstance run) {
        requireMainThread();
        if (occupant == run) occupant = null;
    }

    private static boolean between(double value, double a, double b) {
        return value >= Math.min(a, b) && value <= Math.max(a, b);
    }

    private static void requireMainThread() {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Arena admission requires the server thread");
    }
}
