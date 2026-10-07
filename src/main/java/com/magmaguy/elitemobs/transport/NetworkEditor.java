package com.magmaguy.elitemobs.transport;

import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.magmacore.DrawLine;
import com.magmaguy.magmacore.ai.route.CurvedRoute;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import java.util.*;

/** Player-local network drafts. Labels and lines are spawned for the editing player only. */
public final class NetworkEditor implements AutoCloseable {
    private static final double VIEW = 128;
    private static final float WIDTH = 0.08f;
    private final TransportModule module;
    private final Plugin plugin;
    private final Map<UUID, Session> sessions = new HashMap<>();
    private final BukkitTask refresh;

    NetworkEditor(TransportModule module, Plugin plugin) {
        this.module = module; this.plugin = plugin;
        refresh = Bukkit.getScheduler().runTaskTimer(plugin, this::refresh, 10, 10);
    }

    public void execute(Player player, String action, String[] args) {
        try {
            if (action.equals("edit")) { open(player, arg(args, 0, "network id")); return; }
            if (action.equals("list")) { TransportModule.message(player, String.join(", ", module.files.networkIds())); return; }
            Session s = sessions.get(player.getUniqueId());
            if (s == null) throw new IllegalArgumentException("Use /em transport network edit <network> first.");
            if (!player.getWorld().equals(s.world)) throw new IllegalArgumentException("Return to the network's world to edit it.");
            switch (action) {
                case "add" -> add(player, s, arg(args, 0, "node id"), Arrays.copyOfRange(args, 1, args.length));
                case "select" -> s.selected = args.length == 0 ? nearest(s, player.getLocation().toVector()) : known(s, args[0]).id();
                case "link", "unlink" -> {
                    String a = known(s, arg(args, 0, "first node")).id(), b = known(s, arg(args, 1, "second node")).id();
                    edit(s, () -> link(s, a, b, action.equals("link")));
                }
                case "move" -> {
                    TransportNode node = known(s, arg(args, 0, "node id"));
                    edit(s, () -> s.nodes.put(node.id(), node.withLocation(player.getLocation().toVector())));
                }
                case "remove" -> { String id = known(s, arg(args, 0, "node id")).id(); edit(s, () -> remove(s, id)); }
                case "destination" -> {
                    TransportNode node = known(s, arg(args, 0, "node id"));
                    String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    edit(s, () -> s.nodes.put(node.id(), node.withDestination(name.isBlank() ? null : name, node.arrivalYaw())));
                }
                case "permission" -> {
                    TransportNode node = known(s, arg(args, 0, "node id"));
                    String permission = arg(args, 1, "permission or none");
                    edit(s, () -> s.nodes.put(node.id(), node.withPermission(permission.equals("none") ? null : permission)));
                }
                case "hint" -> {
                    TransportNode node = known(s, arg(args, 0, "node id"));
                    String hint = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    edit(s, () -> s.nodes.put(node.id(), node.withHint(hint.isBlank() || hint.equals("none") ? null : hint)));
                }
                case "yaw" -> {
                    TransportNode node = known(s, arg(args, 0, "node id"));
                    edit(s, () -> s.nodes.put(node.id(), node.withDestination(node.name(), player.getLocation().getYaw())));
                }
                case "undo" -> {
                    if (s.undo.isEmpty()) throw new IllegalArgumentException("Nothing to undo.");
                    s.nodes = s.undo.pop();
                    if (s.selected != null && !s.nodes.containsKey(s.selected)) s.selected = null;
                }
                case "preview" -> {
                    s.preview = args.length == 0 ? List.of() : network(s).path(arg(args, 0, "from node"), arg(args, 1, "to node"));
                    if (args.length > 0 && s.preview.isEmpty()) throw new IllegalArgumentException("No path connects those nodes.");
                }
                case "ride" -> { module.start(player, network(s).route(arg(args, 0, "from node"), arg(args, 1, "to node"))); return; }
                case "generate" -> { generate(player, s, known(s, arg(args, 0, "first node")), known(s, arg(args, 1, "second node"))); return; }
                case "save" -> {
                    module.files.saveNetwork(network(s));
                    end(player.getUniqueId());
                    TransportModule.message(player, "Saved " + s.id + ".");
                    return;
                }
                case "cancel" -> { end(player.getUniqueId()); TransportModule.message(player, "Draft discarded."); return; }
                case "status" -> { }
                default -> throw new IllegalArgumentException("Unknown network editor action.");
            }
            TransportModule.message(player, s.id + ": " + s.nodes.size() + " nodes" + (s.selected == null ? "" : ", selected " + s.selected) + ".");
        } catch (Exception failure) { TransportModule.message(player, failure.getMessage()); }
    }

    private void open(Player player, String id) {
        if (sessions.containsKey(player.getUniqueId())) throw new IllegalArgumentException("Save or cancel your current network first.");
        if (!id.matches("[a-z0-9_-]{1,64}")) throw new IllegalArgumentException("Use a lowercase network id.");
        String authoredWorld = player.getWorld().getName();
        if (PlayerData.getMatchInstance(player) instanceof DungeonInstance dungeon)
            authoredWorld = dungeon.getContentPackagesConfigFields().getWorldName();
        TransportNetwork existing = module.files.network(id);
        if (existing == null && module.files.hasNetworkDefinition(id))
            throw new IllegalArgumentException("Network file exists but is disabled or invalid. Fix or remove it first.");
        if (existing != null && !existing.world().equals(authoredWorld))
            throw new IllegalArgumentException("Network " + id + " belongs to " + existing.world() + ".");
        sessions.put(player.getUniqueId(), new Session(id, player.getWorld(), authoredWorld, existing));
        TransportModule.message(player, "Editing " + id + ". /em transport node add <id> [neighbors] places a node where you stand.");
    }

    private void add(Player player, Session s, String id, String[] neighbors) {
        if (s.nodes.containsKey(id)) throw new IllegalArgumentException("Node " + id + " already exists.");
        List<String> links = neighbors.length > 0 ? Arrays.stream(neighbors).map(n -> known(s, n).id()).toList()
                : s.selected == null || !s.nodes.containsKey(s.selected) ? List.of() : List.of(s.selected);
        Vector at = player.getLocation().toVector();
        edit(s, () -> {
            s.nodes.put(id, new TransportNode(id, at, null, null, null, 0, Set.of(), false));
            for (String other : links) link(s, id, other, true);
        });
        s.selected = id;
    }

    private void edit(Session s, Runnable change) {
        Map<String, TransportNode> before = new LinkedHashMap<>(s.nodes);
        try { change.run(); }
        catch (RuntimeException failure) { s.nodes = before; throw failure; }
        if (s.undo.size() == 32) s.undo.removeLast();
        s.undo.push(before);
    }

    private static void link(Session s, String a, String b, boolean add) {
        if (a.equals(b)) throw new IllegalArgumentException("A node cannot link to itself.");
        for (String[] pair : new String[][]{{a, b}, {b, a}}) {
            TransportNode node = s.nodes.get(pair[0]);
            Set<String> next = new TreeSet<>(node.neighbors());
            if (add) next.add(pair[1]); else next.remove(pair[1]);
            s.nodes.put(node.id(), node.withNeighbors(next));
        }
    }

    private static void remove(Session s, String id) {
        for (String neighbor : s.nodes.get(id).neighbors()) if (s.nodes.containsKey(neighbor)) link(s, id, neighbor, false);
        s.nodes.remove(id);
        if (id.equals(s.selected)) s.selected = null;
    }

    private TransportNetwork network(Session s) {
        return TransportNetwork.linked(s.id, s.authoredWorld, s.transportEntity, s.speed, s.acceleration, s.countdown,
                s.nodes.values(), warning -> { });
    }

    private static TransportNode known(Session s, String id) {
        TransportNode node = s.nodes.get(id);
        if (node == null) throw new IllegalArgumentException("Unknown node " + id + ".");
        return node;
    }

    private static String nearest(Session s, Vector position) {
        return s.nodes.values().stream().min(Comparator.comparingDouble(node -> node.location().distanceSquared(position)))
                .map(TransportNode::id).orElseThrow(() -> new IllegalArgumentException("This network has no nodes yet."));
    }

    private static String arg(String[] args, int index, String what) {
        if (args.length <= index) throw new IllegalArgumentException("Missing " + what + ".");
        return args[index];
    }

    // ---- Placeholder generator ----

    private void generate(Player player, Session s, TransportNode a, TransportNode b) {
        Vector start = a.location(), end = b.location();
        Vector flat = end.clone().subtract(start).setY(0);
        double length = flat.length();
        if (length < 16) throw new IllegalArgumentException("Those nodes are too close to need a generated chain.");
        Vector direction = flat.clone().normalize(), side = new Vector(-direction.getZ(), 0, direction.getX());
        int samples = (int) Math.ceil(length / 4);
        double[] heights = new double[samples + 1];
        World world = s.world;
        TransportModule.message(player, "Sampling terrain between " + a.id() + " and " + b.id() + "...");
        new BukkitRunnable() {
            int index;
            @Override public void run() {
                if (sessions.get(player.getUniqueId()) != s || !player.isOnline()) { cancel(); return; }
                for (int budget = 0; budget < 32 && index <= samples; budget++, index++) {
                    Vector center = start.clone().add(direction.clone().multiply(length * index / samples));
                    double highest = world.getMinHeight();
                    for (int offset = -3; offset <= 3; offset += 3) {
                        int x = (int) Math.floor(center.getX() + side.getX() * offset), z = (int) Math.floor(center.getZ() + side.getZ() * offset);
                        // Existing terrain only: never generate chunks.
                        if (world.isChunkGenerated(x >> 4, z >> 4))
                            highest = Math.max(highest, world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING) + 1);
                    }
                    heights[index] = highest;
                }
                if (index <= samples) return;
                cancel();
                try { insertChain(player, s, a, b, start, direction, length, heights); }
                catch (RuntimeException failure) { TransportModule.message(player, failure.getMessage()); }
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    private void insertChain(Player player, Session s, TransportNode a, TransportNode b, Vector start, Vector direction,
                             double length, double[] heights) {
        if (!s.nodes.containsKey(a.id()) || !s.nodes.containsKey(b.id()))
            throw new IllegalArgumentException("A node was removed while the terrain was sampled.");
        int samples = heights.length - 1, window = 8; // 8 samples is about 32 blocks either side
        double spacing = length / samples, ceiling = s.world.getMaxHeight() - 4;
        double[] cruise = new double[heights.length];
        for (int i = 0; i <= samples; i++) {
            double highest = Double.NEGATIVE_INFINITY;
            for (int j = Math.max(0, i - window); j <= Math.min(samples, i + window); j++) highest = Math.max(highest, heights[j]);
            cruise[i] = Math.min(ceiling, highest + 12);
        }
        List<Vector> chain = new ArrayList<>();
        if (cruise[0] - a.location().getY() > 2) chain.add(new Vector(a.location().getX(), cruise[0], a.location().getZ()));
        int stride = Math.max(1, (int) Math.round(48 / spacing));
        for (int i = stride; i < samples - stride / 2; i += stride) {
            Vector flat = start.clone().add(direction.clone().multiply(length * i / samples));
            chain.add(new Vector(flat.getX(), cruise[i], flat.getZ()));
        }
        if (cruise[samples] - b.location().getY() > 2) chain.add(new Vector(b.location().getX(), cruise[samples], b.location().getZ()));
        edit(s, () -> {
            if (s.nodes.get(a.id()).neighbors().contains(b.id())) link(s, a.id(), b.id(), false);
            String previous = a.id();
            for (Vector point : chain) {
                String id = uniqueId(s, a.id() + "_" + b.id());
                s.nodes.put(id, new TransportNode(id, point, null, null, null, 0, Set.of(), true));
                link(s, previous, id, true);
                previous = id;
            }
            link(s, previous, b.id(), true);
            if (!chain.isEmpty()) s.selected = previous;
        });
        TransportModule.message(player, "Generated " + chain.size() + " placeholder nodes between " + a.id() + " and " + b.id() + ".");
    }

    private static String uniqueId(Session s, String base) {
        String trimmed = base.length() > 58 ? base.substring(0, 58) : base;
        for (int i = 1; ; i++) if (!s.nodes.containsKey(trimmed + "_" + i)) return trimmed + "_" + i;
    }

    // ---- Visuals: visible to the editing player only ----

    private void refresh() {
        var iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Player player = Bukkit.getPlayer(entry.getKey());
            Session s = entry.getValue();
            if (player == null) { clearVisuals(s); iterator.remove(); continue; }
            if (player.getWorld() != s.world) { clearVisuals(s); continue; }
            Vector viewer = player.getLocation().toVector();
            drawLabels(player, s, viewer);
            drawLinks(player, s, viewer);
            drawCurve(player, s, viewer);
        }
    }

    private void drawLabels(Player player, Session s, Vector viewer) {
        Set<String> wanted = new HashSet<>();
        for (TransportNode node : s.nodes.values()) {
            if (node.location().distanceSquared(viewer) > VIEW * VIEW) continue;
            wanted.add(node.id());
            String text = (node.id().equals(s.selected) ? "&e" : "&f") + node.id() + (node.destination() ? "\n&6" + node.name() : "");
            Location at = node.location().add(new Vector(0, 0.75, 0)).toLocation(s.world);
            TextDisplay label = s.labels.get(node.id());
            if (label == null || !label.isValid()) {
                label = s.world.spawn(at, TextDisplay.class, display -> {
                    display.setVisibleByDefault(false);
                    display.setPersistent(false);
                    display.setBillboard(Display.Billboard.CENTER);
                    display.setSeeThrough(true);
                });
                player.showEntity(plugin, label);
                s.labels.put(node.id(), label);
            } else if (label.getLocation().toVector().distanceSquared(at.toVector()) > 0.01) label.teleport(at);
            label.setText(ChatColorConverter.convert(text));
        }
        s.labels.entrySet().removeIf(e -> { if (wanted.contains(e.getKey())) return false; e.getValue().remove(); return true; });
    }

    private void drawLinks(Player player, Session s, Vector viewer) {
        Set<String> wanted = new HashSet<>();
        for (TransportNode node : s.nodes.values())
            for (String neighbor : node.neighbors()) {
                TransportNode other = s.nodes.get(neighbor);
                if (other == null || node.id().compareTo(neighbor) > 0) continue;
                if (node.location().distanceSquared(viewer) > VIEW * VIEW && other.location().distanceSquared(viewer) > VIEW * VIEW) continue;
                String key = node.id() + "|" + neighbor;
                wanted.add(key);
                Location from = node.location().toLocation(s.world), to = other.location().toLocation(s.world);
                DrawLine.LineData line = s.lines.get(key);
                if (line == null || line.getDisplay() == null || !line.getDisplay().isValid()) {
                    line = DrawLine.drawLine(from, to, WIDTH, Material.LIGHT_BLUE_CONCRETE, -1, player);
                    if (line != null) s.lines.put(key, line);
                } else if (!line.getStart().equals(from) || !line.getEnd().equals(to)) DrawLine.updateLine(line, from, to);
            }
        s.lines.entrySet().removeIf(e -> { if (wanted.contains(e.getKey())) return false; e.getValue().remove(); return true; });
    }

    /** The A* preview in green; otherwise the projected curve through the selected node to where you stand, in yellow. */
    private void drawCurve(Player player, Session s, Vector viewer) {
        List<Vector> points = new ArrayList<>();
        Material material = Material.LIME_CONCRETE;
        if (!s.preview.isEmpty()) s.preview.forEach(node -> points.add(node.location()));
        else if (s.selected != null && s.nodes.containsKey(s.selected)) {
            TransportNode selected = s.nodes.get(s.selected);
            selected.neighbors().stream().findFirst().map(s.nodes::get).ifPresent(previous -> points.add(previous.location()));
            points.add(selected.location());
            if (viewer.distanceSquared(selected.location()) > 1) points.add(viewer.clone());
            material = Material.YELLOW_CONCRETE;
        }
        List<Location[]> segments = new ArrayList<>();
        try {
            if (points.size() >= 2) {
                CurvedRoute curve = new CurvedRoute(points);
                double step = Math.max(2, curve.length() / 256);
                for (double d = 0; d < curve.length() && segments.size() < 256; d += step) {
                    Vector a = curve.at(d), b = curve.at(Math.min(curve.length(), d + step));
                    if (a.distanceSquared(viewer) <= VIEW * VIEW && a.distanceSquared(b) > 0)
                        segments.add(new Location[]{a.toLocation(s.world), b.toLocation(s.world)});
                }
            }
        } catch (IllegalArgumentException degenerate) { segments.clear(); }
        if (material != s.curveMaterial) { s.curveLines.forEach(DrawLine.LineData::remove); s.curveLines.clear(); s.curveMaterial = material; }
        while (s.curveLines.size() > segments.size()) s.curveLines.removeLast().remove();
        for (int i = 0; i < segments.size(); i++) {
            Location[] segment = segments.get(i);
            if (i < s.curveLines.size()) {
                var display = s.curveLines.get(i).getDisplay();
                if (display != null && display.isValid()) { DrawLine.updateLine(s.curveLines.get(i), segment[0], segment[1]); continue; }
            }
            DrawLine.LineData line = DrawLine.drawLine(segment[0], segment[1], WIDTH, material, -1, player);
            if (line == null) continue;
            if (i < s.curveLines.size()) { s.curveLines.get(i).remove(); s.curveLines.set(i, line); }
            else s.curveLines.add(line);
        }
    }

    private static void clearVisuals(Session s) {
        s.labels.values().forEach(TextDisplay::remove); s.labels.clear();
        s.lines.values().forEach(DrawLine.LineData::remove); s.lines.clear();
        s.curveLines.forEach(DrawLine.LineData::remove); s.curveLines.clear();
    }

    /** Ends a player's draft and removes its visuals. */
    void end(UUID player) {
        Session s = sessions.remove(player);
        if (s != null) clearVisuals(s);
    }

    @Override public void close() {
        refresh.cancel();
        sessions.values().forEach(NetworkEditor::clearVisuals);
        sessions.clear();
    }

    private static final class Session {
        final String id; final World world; final String authoredWorld;
        final String transportEntity; final double speed, acceleration; final int countdown;
        Map<String, TransportNode> nodes = new LinkedHashMap<>();
        final Deque<Map<String, TransportNode>> undo = new ArrayDeque<>();
        String selected;
        List<TransportNode> preview = List.of();
        final Map<String, TextDisplay> labels = new HashMap<>();
        final Map<String, DrawLine.LineData> lines = new HashMap<>();
        final List<DrawLine.LineData> curveLines = new ArrayList<>();
        Material curveMaterial;
        Session(String id, World world, String authoredWorld, TransportNetwork existing) {
            this.id = id; this.world = world; this.authoredWorld = authoredWorld;
            transportEntity = existing == null ? "transport_cannon_seat.yml" : existing.transportEntity();
            speed = existing == null ? 24 : existing.speed();
            acceleration = existing == null ? 16 : existing.acceleration();
            countdown = existing == null ? 60 : existing.countdown();
            if (existing != null) nodes.putAll(existing.nodes());
        }
    }
}
