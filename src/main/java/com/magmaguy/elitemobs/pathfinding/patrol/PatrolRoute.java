package com.magmaguy.elitemobs.pathfinding.patrol;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable route geometry and traversal policy in an actor's authored world. */
public final class PatrolRoute {
    public static final String ROOT = "patrol";

    private final List<Vector> nodes;
    private final boolean relative;
    private final PatrolMode mode;
    private final double speedModifier;
    private final Double virtualSpeed;
    private final double startNodeWaitSeconds;

    private PatrolRoute(
            List<Vector> nodes,
            boolean relative,
            PatrolMode mode,
            double speedModifier,
            Double virtualSpeed,
            double startNodeWaitSeconds) {
        this.nodes = Collections.unmodifiableList(nodes.stream().map(Vector::clone).toList());
        this.relative = relative;
        this.mode = Objects.requireNonNull(mode, "mode");
        this.speedModifier = requirePositiveFinite(speedModifier, "patrol.speed");
        this.virtualSpeed = virtualSpeed == null
                ? null
                : requirePositiveFinite(virtualSpeed, "patrol.virtualSpeed");
        if (!Double.isFinite(startNodeWaitSeconds) || startNodeWaitSeconds < 0D) {
            throw new IllegalArgumentException("patrol.startNodeWaitSeconds must be finite and not negative");
        }
        this.startNodeWaitSeconds = startNodeWaitSeconds;
        validateGeometry();
    }

    /** Returns null when no patrol is configured. Invalid configured routes throw with a user-facing reason. */
    public static PatrolRoute parse(FileConfiguration configuration) {
        Objects.requireNonNull(configuration, "configuration");
        if (!configuration.contains(ROOT + ".nodes")) return null;
        if (Boolean.FALSE.equals(setting(configuration, "enabled"))) return null;

        List<?> rawNodes = configuration.getList(ROOT + ".nodes");
        if (rawNodes == null) throw new IllegalArgumentException("patrol.nodes must be a list");
        List<Vector> nodes = new ArrayList<>();
        for (int index = 0; index < rawNodes.size(); index++) {
            nodes.add(parseNode(rawNodes.get(index), index));
        }

        if (nodes.size() > 2 && nodes.getFirst().distanceSquared(nodes.getLast()) < 1.0E-8D) {
            nodes.removeLast();
        }

        if (configuration.contains(ROOT + ".relative") && !configuration.isBoolean(ROOT + ".relative")) {
            throw new IllegalArgumentException("patrol.relative must be true or false");
        }
        boolean relative = !Boolean.FALSE.equals(setting(configuration, "relative"));
        Object rawMode = setting(configuration, "mode");
        PatrolMode mode = PatrolMode.parse(rawMode == null ? PatrolMode.LOOP.name() : rawMode.toString());
        double speed = numberSetting(setting(configuration, "speed"), 1D);
        Double virtualSpeed = configuration.contains(ROOT + ".virtualSpeed")
                ? configuration.getDouble(ROOT + ".virtualSpeed")
                : null;
        double startNodeWaitSeconds = numberSetting(setting(configuration, "startNodeWaitSeconds"), 0D);
        return new PatrolRoute(nodes, relative, mode, speed, virtualSpeed, startNodeWaitSeconds);
    }

    /** Authored coordinates: spawn offsets when relative, otherwise absolute world coordinates. */
    public List<Vector> nodes() {
        return nodes.stream().map(Vector::clone).toList();
    }

    public boolean relative() {
        return relative;
    }

    public PatrolMode mode() {
        return mode;
    }

    public double speedModifier() {
        return speedModifier;
    }

    public Double virtualSpeed() {
        return virtualSpeed;
    }

    /** How long the actor rests at the first node each time it gets back there, once per lap. */
    public double startNodeWaitSeconds() {
        return startNodeWaitSeconds;
    }

    public int startNodeWaitTicks() {
        return (int) Math.min(Integer.MAX_VALUE, Math.round(startNodeWaitSeconds * 20D));
    }

    public int size() {
        return nodes.size();
    }

    public Location node(PatrolOrigin origin, int index) {
        Location base = requireResolvedOrigin(origin);
        Vector node = nodes.get(requireNode(index));
        if (relative) return base.add(node);
        base.setX(node.getX());
        base.setY(node.getY());
        base.setZ(node.getZ());
        return base;
    }

    public Step next(int currentNode, int direction) {
        requireNode(currentNode);
        if (mode == PatrolMode.LOOP) {
            return new Step((currentNode + 1) % nodes.size(), 1);
        }

        int resolvedDirection = direction < 0 ? -1 : 1;
        if (currentNode == 0) resolvedDirection = 1;
        else if (currentNode == nodes.size() - 1) resolvedDirection = -1;
        return new Step(currentNode + resolvedDirection, resolvedDirection);
    }

    public int nearestNode(PatrolOrigin origin, Location location) {
        if (!sameWorld(origin, location)) return 0;
        int nearest = 0;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (int index = 0; index < nodes.size(); index++) {
            double distance = node(origin, index).distanceSquared(location);
            if (distance < nearestDistance) {
                nearest = index;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    /** Returns the closest point on the route polyline, including LOOP's closing segment. */
    public Location closestPoint(PatrolOrigin origin, Location location) {
        if (!sameWorld(origin, location)) return node(origin, 0);
        Location closest = node(origin, 0);
        double closestDistance = closest.distanceSquared(location);
        int segmentCount = mode == PatrolMode.LOOP ? nodes.size() : nodes.size() - 1;
        for (int startIndex = 0; startIndex < segmentCount; startIndex++) {
            int endIndex = (startIndex + 1) % nodes.size();
            Location candidate = project(location, node(origin, startIndex), node(origin, endIndex));
            double distance = candidate.distanceSquared(location);
            if (distance < closestDistance) {
                closest = candidate;
                closestDistance = distance;
            }
        }
        return closest;
    }

    public double projectedFraction(PatrolOrigin origin, Location location, int startNode, int targetNode) {
        if (!sameWorld(origin, location)) return 0D;
        Location start = node(origin, startNode);
        Location end = node(origin, targetNode);
        Vector leg = end.toVector().subtract(start.toVector());
        double lengthSquared = leg.lengthSquared();
        if (lengthSquared <= 1.0E-8D) return 1D;
        double projection = location.toVector().subtract(start.toVector()).dot(leg) / lengthSquared;
        return Math.max(0D, Math.min(1D, projection));
    }

    public double legLength(PatrolOrigin origin, int startNode, int targetNode) {
        return node(origin, startNode).distance(node(origin, targetNode));
    }

    public Location interpolate(PatrolOrigin origin, int startNode, int targetNode, double fraction) {
        Location start = node(origin, startNode);
        Vector delta = node(origin, targetNode).toVector().subtract(start.toVector());
        return start.add(delta.multiply(Math.max(0D, Math.min(1D, fraction))));
    }

    /** Identifies the geometry that saved progress was recorded against. */
    public String geometryKey() {
        return Integer.toHexString((relative + "|" + String.join(";", serializeNodes())).hashCode());
    }

    public List<String> serializeNodes() {
        return nodes.stream()
                .map(node -> format(node.getX()) + ',' + format(node.getY()) + ',' + format(node.getZ()))
                .toList();
    }

    private void validateGeometry() {
        if (nodes.size() < 2) throw new IllegalArgumentException("patrol.nodes requires at least two distinct nodes");
        for (int index = 0; index < nodes.size(); index++) {
            Vector node = nodes.get(index);
            if (!Double.isFinite(node.getX()) || !Double.isFinite(node.getY()) || !Double.isFinite(node.getZ())) {
                throw new IllegalArgumentException("patrol.nodes[" + index + "] contains a non-finite coordinate");
            }
        }

        int segmentCount = mode == PatrolMode.LOOP ? nodes.size() : nodes.size() - 1;
        for (int start = 0; start < segmentCount; start++) {
            int end = (start + 1) % nodes.size();
            double length = nodes.get(start).distance(nodes.get(end));
            if (length <= 1.0E-4D) {
                throw new IllegalArgumentException("patrol leg " + start + " -> " + end + " has duplicate nodes");
            }
        }
    }

    private int requireNode(int index) {
        if (index < 0 || index >= nodes.size()) throw new IllegalArgumentException("Invalid patrol node " + index);
        return index;
    }

    private static Vector parseNode(Object rawNode, int index) {
        if (rawNode instanceof String value) {
            String[] parts = value.split(",");
            if (parts.length != 3) {
                throw new IllegalArgumentException("patrol.nodes[" + index + "] must be x,y,z");
            }
            try {
                return new Vector(
                        Double.parseDouble(parts[0].trim()),
                        Double.parseDouble(parts[1].trim()),
                        Double.parseDouble(parts[2].trim()));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("patrol.nodes[" + index + "] contains an invalid number", exception);
            }
        }
        if (rawNode instanceof ConfigurationSection section) {
            return new Vector(section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
        }
        if (rawNode instanceof Map<?, ?> map) {
            return new Vector(number(map.get("x"), index), number(map.get("y"), index), number(map.get("z"), index));
        }
        throw new IllegalArgumentException("patrol.nodes[" + index + "] must be x,y,z or an x/y/z map");
    }

    /**
     * Premade files register patrol keys as configuration defaults before their first save. Bukkit's
     * getX(path, fallback) ignores those defaults and read an absolute premade route as relative, so read
     * through get(path), which falls back to them.
     */
    private static Object setting(FileConfiguration configuration, String key) {
        return configuration.get(ROOT + "." + key);
    }

    private static double numberSetting(Object value, double fallback) {
        return value instanceof Number number ? number.doubleValue() : fallback;
    }

    private static double number(Object value, int index) {
        if (value instanceof Number number) return number.doubleValue();
        if (value instanceof String string) {
            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException ignored) {
            }
        }
        throw new IllegalArgumentException("patrol.nodes[" + index + "] has a missing or invalid coordinate");
    }

    private static double requirePositiveFinite(double value, String path) {
        if (!Double.isFinite(value) || value <= 0D) {
            throw new IllegalArgumentException(path + " must be finite and positive");
        }
        return value;
    }

    private static boolean sameWorld(PatrolOrigin origin, Location location) {
        return location != null && location.getWorld() != null
                && origin.worldName().equals(location.getWorld().getName());
    }

    private static Location requireResolvedOrigin(PatrolOrigin origin) {
        Location location = origin.resolve();
        if (location == null) throw new IllegalStateException("Patrol world is not loaded: " + origin.worldName());
        return location;
    }

    private static Location project(Location point, Location start, Location end) {
        Vector segment = end.toVector().subtract(start.toVector());
        double lengthSquared = segment.lengthSquared();
        if (lengthSquared <= 1.0E-8D) return start.clone();
        double fraction = point.toVector().subtract(start.toVector()).dot(segment) / lengthSquared;
        return start.clone().add(segment.multiply(Math.max(0D, Math.min(1D, fraction))));
    }

    private static String format(double value) {
        return Double.toString(value);
    }

    public record Step(int targetNode, int direction) {
    }
}
