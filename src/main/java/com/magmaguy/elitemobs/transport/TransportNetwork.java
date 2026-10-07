package com.magmaguy.elitemobs.transport;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.function.Consumer;

/** Nodes joined by two-way links. Flights search the graph when requested; nothing is precomputed. */
public record TransportNetwork(String id, String world, String transportEntity, double speed, double acceleration,
                               int countdown, Map<String, TransportNode> nodes) {
    public TransportNetwork {
        if (id == null || !id.matches("[a-z0-9_-]{1,64}")) throw new IllegalArgumentException("Invalid network id: " + id);
        if (world == null || world.isBlank()) throw new IllegalArgumentException("Network " + id + " needs a world");
        if (transportEntity == null || !transportEntity.endsWith(".yml"))
            throw new IllegalArgumentException("transportEntity must name a customboss .yml file");
        if (!Double.isFinite(speed) || speed <= 0 || !Double.isFinite(acceleration) || acceleration <= 0 || countdown < 0)
            throw new IllegalArgumentException("Network " + id + " has an invalid speed, acceleration or countdown");
        nodes = Collections.unmodifiableMap(new LinkedHashMap<>(nodes));
    }

    /** Builds a network whose links are symmetric, dropping links that cannot be flown. */
    static TransportNetwork linked(String id, String world, String transportEntity, double speed, double acceleration,
                                   int countdown, Collection<TransportNode> raw, Consumer<String> warn) {
        Map<String, TransportNode> byId = new LinkedHashMap<>();
        for (TransportNode node : raw) byId.put(node.id(), node);
        Map<String, Set<String>> links = new LinkedHashMap<>();
        for (String nodeId : byId.keySet()) links.put(nodeId, new TreeSet<>());
        for (TransportNode node : byId.values())
            for (String neighbor : node.neighbors()) {
                TransportNode other = byId.get(neighbor);
                if (other == null) { warn.accept("node " + node.id() + " links to missing node " + neighbor); continue; }
                if (other == node || other.location().distanceSquared(node.location()) == 0) {
                    warn.accept("node " + node.id() + " cannot link to " + neighbor + " at the same position"); continue;
                }
                links.get(node.id()).add(neighbor);
                links.get(neighbor).add(node.id());
            }
        Map<String, TransportNode> result = new LinkedHashMap<>();
        for (TransportNode node : byId.values()) {
            if (links.get(node.id()).isEmpty()) warn.accept("node " + node.id() + " has no neighbors");
            result.put(node.id(), node.withNeighbors(links.get(node.id())));
        }
        TransportNetwork network = new TransportNetwork(id, world, transportEntity, speed, acceleration, countdown, result);
        if (result.values().stream().filter(TransportNode::destination).count() > 1)
            for (TransportNode node : result.values())
                if (node.destination() && network.reachableDestinations(node.id()).isEmpty())
                    warn.accept("destination " + node.id() + " cannot reach any other destination");
        return network;
    }

    /** A* over straight distances between nodes. Empty when unreachable. */
    public List<TransportNode> path(String from, String to) {
        TransportNode start = nodes.get(from), goal = nodes.get(to);
        if (start == null || goal == null || from.equals(to)) return List.of();
        Map<String, Double> cost = new HashMap<>(Map.of(from, 0D));
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<Map.Entry<String, Double>> open = new PriorityQueue<>(Map.Entry.comparingByValue());
        open.add(Map.entry(from, start.location().distance(goal.location())));
        Set<String> closed = new HashSet<>();
        while (!open.isEmpty()) {
            String current = open.poll().getKey();
            if (current.equals(to)) {
                LinkedList<TransportNode> result = new LinkedList<>();
                for (String step = to; step != null; step = previous.get(step)) result.addFirst(nodes.get(step));
                return List.copyOf(result);
            }
            if (!closed.add(current)) continue;
            TransportNode node = nodes.get(current);
            for (String next : node.neighbors()) {
                if (closed.contains(next)) continue;
                double tentative = cost.get(current) + node.location().distance(nodes.get(next).location());
                if (tentative >= cost.getOrDefault(next, Double.POSITIVE_INFINITY)) continue;
                cost.put(next, tentative);
                previous.put(next, current);
                open.add(Map.entry(next, tentative + nodes.get(next).location().distance(goal.location())));
            }
        }
        return List.of();
    }

    /** Destinations reachable from a node, nearest first. */
    public List<TransportNode> reachableDestinations(String from) {
        Map<String, Double> distance = new HashMap<>();
        if (!nodes.containsKey(from)) return List.of();
        PriorityQueue<Map.Entry<String, Double>> open = new PriorityQueue<>(Map.Entry.comparingByValue());
        distance.put(from, 0D);
        open.add(Map.entry(from, 0D));
        while (!open.isEmpty()) {
            var current = open.poll();
            if (current.getValue() > distance.get(current.getKey())) continue;
            TransportNode node = nodes.get(current.getKey());
            for (String next : node.neighbors()) {
                double candidate = current.getValue() + node.location().distance(nodes.get(next).location());
                if (candidate < distance.getOrDefault(next, Double.POSITIVE_INFINITY)) {
                    distance.put(next, candidate);
                    open.add(Map.entry(next, candidate));
                }
            }
        }
        return distance.keySet().stream().filter(nodeId -> !nodeId.equals(from)).map(nodes::get)
                .filter(TransportNode::destination)
                .sorted(Comparator.comparingDouble(node -> distance.get(node.id()))).toList();
    }

    /** The flight between two nodes as an ordinary route, so journeys need no network-specific code. */
    public TransportRoute route(String from, String to) {
        List<TransportNode> path = path(from, to);
        if (path.size() < 2) throw new IllegalArgumentException("No flight path connects " + from + " and " + to + ".");
        TransportNode destination = path.getLast();
        return new TransportRoute(id, destination.destination() ? destination.name() : destination.id(), world,
                transportEntity, speed, acceleration, countdown, destination.arrivalYaw(),
                path.stream().map(TransportNode::location).toList());
    }

    public static TransportNetwork load(String id, ConfigurationSection yaml, Consumer<String> warn) { return read(id, yaml, warn); }

    static TransportNetwork read(String id, ConfigurationSection yaml, Consumer<String> warn) {
        List<TransportNode> raw = new ArrayList<>();
        ConfigurationSection section = yaml.getConfigurationSection("nodes");
        if (section != null) for (String nodeId : section.getKeys(false)) {
            ConfigurationSection node = section.getConfigurationSection(nodeId);
            if (node == null) { warn.accept("node " + nodeId + " is not a section"); continue; }
            try {
                raw.add(new TransportNode(nodeId, parse(node.getString("location")), node.getString("name"),
                        node.getString("permission"), node.getString("lockedHint"), (float) node.getDouble("arrivalYaw", 0),
                        new LinkedHashSet<>(node.getStringList("neighbors")), node.getBoolean("placeholder", false)));
            } catch (IllegalArgumentException invalid) { warn.accept(invalid.getMessage()); }
        }
        return linked(id, yaml.getString("world"), yaml.getString("transportEntity", "transport_cannon_seat.yml"),
                yaml.getDouble("speed", 24), yaml.getDouble("acceleration", 16), yaml.getInt("countdownTicks", 60), raw, warn);
    }

    void write(YamlConfiguration yaml, boolean create) {
        if (create) {
            yaml.set("isEnabled", true);
            yaml.set("world", world); yaml.set("transportEntity", transportEntity);
            yaml.set("speed", speed); yaml.set("acceleration", acceleration); yaml.set("countdownTicks", countdown);
        }
        yaml.set("nodes", null);
        for (TransportNode node : nodes.values()) {
            String path = "nodes." + node.id() + ".";
            yaml.set(path + "location", format(node.location()));
            if (node.destination()) { yaml.set(path + "name", node.name()); yaml.set(path + "arrivalYaw", node.arrivalYaw()); }
            if (node.permission() != null) yaml.set(path + "permission", node.permission());
            if (node.lockedHint() != null) yaml.set(path + "lockedHint", node.lockedHint());
            if (node.placeholder()) yaml.set(path + "placeholder", true);
            yaml.set(path + "neighbors", node.neighbors().stream().sorted().toList());
        }
    }

    static Vector parse(String text) {
        if (text == null) throw new IllegalArgumentException("Missing node location");
        String[] values = text.split(",");
        if (values.length != 3) throw new IllegalArgumentException("Node location must be x,y,z: " + text);
        return new Vector(Double.parseDouble(values[0].trim()), Double.parseDouble(values[1].trim()), Double.parseDouble(values[2].trim()));
    }

    static String format(Vector vector) {
        return round(vector.getX()) + "," + round(vector.getY()) + "," + round(vector.getZ());
    }

    private static double round(double value) { return Math.round(value * 100) / 100D; }
}
