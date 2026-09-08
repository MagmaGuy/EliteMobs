package com.magmaguy.elitemobs.transport;

import com.magmaguy.magmacore.ai.route.CurvedRoute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import java.util.List;

/** World names refer to authored worlds; the journey binds them to the player's current instance. */
public record TransportRoute(String id, String name, String world, String carrier, String model,
                             String animation, double speed, double acceleration, int countdown,
                             float arrivalYaw, List<Vector> points) {
    public TransportRoute {
        if (!id.matches("[a-z0-9_-]{1,64}")) throw new IllegalArgumentException("Invalid route id");
        if (name == null || name.isBlank() || world == null || world.isBlank())
            throw new IllegalArgumentException("Route needs a name and authored world");
        if (carrier == null || org.bukkit.NamespacedKey.fromString(carrier) == null || model == null || animation == null)
            throw new IllegalArgumentException("Invalid carrier, model or animation");
        if (!Double.isFinite(speed) || speed < 1 || speed > 32
                || !Double.isFinite(acceleration) || acceleration < 1 || acceleration > 32)
            throw new IllegalArgumentException("Speed and acceleration must be 1 to 32");
        if (countdown < 0 || countdown > 200 || !Float.isFinite(arrivalYaw))
            throw new IllegalArgumentException("Invalid countdown or arrival yaw");
        points = points.stream().map(Vector::clone).toList();
        new CurvedRoute(points);
    }
    @Override public List<Vector> points() { return points.stream().map(Vector::clone).toList(); }
    public CurvedRoute curve() { return new CurvedRoute(points); }

    static TransportRoute read(String id, YamlConfiguration yaml) {
        List<Vector> nodes = yaml.getStringList("waypoints").stream().map(line -> {
            String[] values = line.split(",");
            if (values.length != 3) throw new IllegalArgumentException("Waypoint must be x,y,z");
            return new Vector(Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]));
        }).toList();
        return new TransportRoute(id, yaml.getString("name", id), yaml.getString("world"),
                yaml.getString("carrier", "minecraft:pig"), yaml.getString("customModel", ""),
                yaml.getString("flightAnimation", "fly"), yaml.getDouble("speed", 12),
                yaml.getDouble("acceleration", 8), yaml.getInt("countdownTicks", 60),
                (float) yaml.getDouble("arrivalYaw", 0), nodes);
    }

    YamlConfiguration write() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("name", name); yaml.set("world", world); yaml.set("carrier", carrier);
        yaml.set("customModel", model); yaml.set("flightAnimation", animation);
        yaml.set("speed", speed); yaml.set("acceleration", acceleration);
        yaml.set("countdownTicks", countdown); yaml.set("arrivalYaw", arrivalYaw);
        yaml.set("waypoints", points.stream().map(p -> p.getX() + "," + p.getY() + "," + p.getZ()).toList());
        return yaml;
    }
}
