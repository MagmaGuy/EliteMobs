package com.magmaguy.elitemobs.transport;

import com.magmaguy.magmacore.ai.route.CurvedRoute;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import java.util.*;

/** Player-local waypoint drafts. Preview and the mount consume the same curve geometry. */
public final class TransportEditor implements AutoCloseable {
    private final TransportModule module;
    private final Map<UUID, Draft> drafts = new HashMap<>();
    private final BukkitTask preview;

    TransportEditor(TransportModule module, Plugin plugin) {
        this.module = module;
        preview = Bukkit.getScheduler().runTaskTimer(plugin, this::preview, 10, 10);
    }

    public void execute(Player player, String action, String value) {
        try {
            if (action.equals("create") || action.equals("edit")) {
                if (drafts.containsKey(player.getUniqueId())) throw new IllegalArgumentException("Save or cancel your current draft first.");
                TransportRoute route = module.files.get(value);
                if (action.equals("create") && route != null) throw new IllegalArgumentException("Route already exists. Use edit.");
                if (action.equals("edit") && route == null) throw new IllegalArgumentException("Unknown route.");
                if (value == null || !value.matches("[a-z0-9_-]{1,64}")) throw new IllegalArgumentException("Use a lowercase route id with letters, digits, underscores or hyphens.");
                World world = route == null ? player.getWorld() : TransportModule.resolveWorld(player, route);
                String authoredWorld = world.getName();
                if (com.magmaguy.elitemobs.playerdata.database.PlayerData.getMatchInstance(player)
                        instanceof com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance dungeon)
                    authoredWorld = dungeon.getContentPackagesConfigFields().getWorldName();
                Draft draft = new Draft(value, world, authoredWorld, route);
                if (route == null) draft.points.add(player.getLocation().toVector());
                drafts.put(player.getUniqueId(), draft);
                TransportModule.message(player, "Editing " + value + ". Fly to each waypoint and use /em transport add. The faint curve previews your next point.");
                return;
            }
            if (action.equals("list")) { TransportModule.message(player, String.join(", ", module.files.ids())); return; }
            if (action.equals("reload")) { module.files.reload(); TransportModule.message(player, "Routes reloaded; active flights retain their original route."); return; }
            if (action.equals("start")) { TransportModule.startRoute(player, value); return; }
            if (action.equals("stop")) { module.cancel(player); return; }
            Draft draft = drafts.get(player.getUniqueId());
            if (draft == null) throw new IllegalArgumentException("Use /em transport create <id> or edit <id> first.");
            if (action.equals("cancel")) { drafts.remove(player.getUniqueId()); TransportModule.message(player, "Draft discarded."); return; }
            if (!player.getWorld().equals(draft.world)) throw new IllegalArgumentException("Return to the draft's world to edit it.");
            switch (action) {
                case "add", "remove", "move" -> {
                    List<Vector> next = new ArrayList<>(draft.points);
                    if (action.equals("add")) next.add(player.getLocation().toVector());
                    else if (action.equals("remove")) {
                        if (next.isEmpty()) throw new IllegalArgumentException("No waypoints to remove.");
                        next.removeLast();
                    } else {
                        int index = Integer.parseInt(value) - 1;
                        if (index < 0 || index >= next.size()) throw new IllegalArgumentException("Waypoint number is out of range.");
                        next.set(index, player.getLocation().toVector());
                    }
                    if (next.size() >= 2) new CurvedRoute(next);
                    if (draft.undo.size() == 32) draft.undo.removeLast();
                    draft.undo.push(List.copyOf(draft.points));
                    draft.points = next; draft.dirty = true; draft.ghostPosition = null;
                }
                case "undo" -> {
                    if (draft.undo.isEmpty()) throw new IllegalArgumentException("Nothing to undo.");
                    draft.points = new ArrayList<>(draft.undo.pop()); draft.dirty = true; draft.ghostPosition = null;
                }
                case "save" -> {
                    module.files.save(draft.route(player.getLocation().getYaw()));
                    drafts.remove(player.getUniqueId());
                    TransportModule.message(player, "Saved " + draft.id + ". Board at its first waypoint with /em transport start " + draft.id + ".");
                    return;
                }
                case "ride" -> { module.start(player, draft.route(player.getLocation().getYaw())); return; }
                case "status" -> { }
                default -> throw new IllegalArgumentException("Unknown editor action.");
            }
            TransportModule.message(player, draft.id + ": " + draft.points.size() + " waypoints. Add, move <number>, remove, undo, save, ride or cancel.");
        } catch (Exception failure) { TransportModule.message(player, failure.getMessage()); }
    }

    private void preview() {
        var iterator = drafts.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) { iterator.remove(); continue; }
            Draft draft = entry.getValue();
            if (player.getWorld() != draft.world) continue;
            if (draft.dirty) {
                draft.curve = draft.points.size() < 2 ? null : new CurvedRoute(draft.points);
                draft.dirty = false;
            }
            Vector position = player.getLocation().toVector();
            if (draft.ghostPosition == null || draft.ghostPosition.distanceSquared(position) > .25) {
                draft.ghostPosition = position;
                var candidate = new ArrayList<>(draft.points);
                candidate.add(position);
                try { draft.ghost = new CurvedRoute(candidate); }
                catch (IllegalArgumentException invalid) { draft.ghost = null; }
            }
            draw(player, draft.curve, false);
            draw(player, draft.ghost, true);
            for (Vector point : draft.points) if (point.distanceSquared(position) < 96 * 96)
                player.spawnParticle(Particle.END_ROD, point.toLocation(draft.world), 1, 0, 0, 0, 0);
        }
    }

    private static void draw(Player player, CurvedRoute curve, boolean ghost) {
        if (curve == null) return;
        Vector viewer = player.getLocation().toVector();
        double step = Math.max(.5, curve.length() / 300);
        for (double distance = 0; distance <= curve.length(); distance += step) {
            Vector point = curve.at(distance);
            if (point.distanceSquared(viewer) > 96 * 96) continue;
            boolean loaded = player.getWorld().isChunkLoaded((int) Math.floor(point.getX()) >> 4, (int) Math.floor(point.getZ()) >> 4);
            Color color = !loaded ? Color.YELLOW : TransportClearance.point(player.getWorld(), point) != null ? Color.RED : ghost ? Color.AQUA : Color.LIME;
            player.spawnParticle(Particle.DUST, point.toLocation(player.getWorld()), 1, new Particle.DustOptions(color, ghost ? .5F : 1F));
        }
    }

    @Override public void close() { preview.cancel(); drafts.clear(); }

    private static final class Draft {
        final String id; final World world; final String authoredWorld; final TransportRoute original;
        List<Vector> points = new ArrayList<>();
        final Deque<List<Vector>> undo = new ArrayDeque<>();
        boolean dirty = true;
        CurvedRoute curve, ghost; Vector ghostPosition;
        Draft(String id, World world, String authoredWorld, TransportRoute original) {
            this.id = id; this.world = world; this.authoredWorld = authoredWorld; this.original = original;
            if (original != null) points.addAll(original.points());
        }
        TransportRoute route(float yaw) {
            return new TransportRoute(id, original == null ? id : original.name(),
                    original == null ? authoredWorld : original.world(),
                    original == null ? "minecraft:pig" : original.carrier(),
                    original == null ? "" : original.model(), original == null ? "fly" : original.animation(),
                    original == null ? 12 : original.speed(), original == null ? 8 : original.acceleration(),
                    original == null ? 60 : original.countdown(), yaw, points);
        }
    }
}
