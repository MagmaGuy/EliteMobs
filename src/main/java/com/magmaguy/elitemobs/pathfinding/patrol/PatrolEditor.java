package com.magmaguy.elitemobs.pathfinding.patrol;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.PathfindingHandle;
import com.magmaguy.easyminecraftgoals.PathfindingStatus;
import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.easyminecraftgoals.thirdparty.BedrockChecker;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.util.RayTraceResult;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Live, player-scoped patrol authoring. Runtime patrols remain owned by {@link PatrolService}. */
public final class PatrolEditor implements Listener {
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static PatrolEditor listener;
    private static BukkitTask previewTask;
    private static int previewTicks;

    private PatrolEditor() {
    }

    public static void initialize() {
        shutdown();
        listener = new PatrolEditor();
        Bukkit.getPluginManager().registerEvents(listener, MetadataHandler.PLUGIN);
        previewTicks = 0;
        previewTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, PatrolEditor::preview, 1L, 1L);
    }

    public static void shutdown() {
        for (Session session : new ArrayList<>(SESSIONS.values())) session.close(true);
        SESSIONS.clear();
        if (previewTask != null) previewTask.cancel();
        previewTask = null;
        if (listener != null) HandlerList.unregisterAll(listener);
        listener = null;
    }

    public static boolean isEditing(NPCEntity npc) {
        return SESSIONS.values().stream().anyMatch(session -> session.owner == npc);
    }

    public static void edit(Player player) {
        cancel(player, false);
        Object owner = targetedOwner(player);
        if (owner == null) {
            message(player, "Look directly at a spawned custom boss or NPC within 12 blocks.");
            return;
        }

        Session session;
        try {
            session = Session.create(player, owner);
        } catch (IllegalArgumentException exception) {
            message(player, exception.getMessage());
            return;
        }
        SESSIONS.put(player.getUniqueId(), session);
        PatrolService.pause(owner);
        message(player, "Editing " + session.filename() + " with " + session.nodes.size()
                + " node(s), mode " + session.mode + ". Use /em patrol add at each waypoint.");
    }

    public static void add(Player player) {
        Session session = session(player);
        if (session == null) return;
        Location waypoint = player.getLocation().getBlock().getLocation().add(0.5D, 0D, 0.5D);
        if (waypoint.getWorld() != session.origin.getWorld()) {
            message(player, "Waypoints must be in the actor's authored world.");
            return;
        }
        if (EliteMobs.worldGuardIsEnabled && !WorldGuardFlagChecker.doEliteMobsSpawnFlag(waypoint)) {
            message(player, "That waypoint is inside an elitemob-spawning deny region.");
            return;
        }

        Vector offset = waypoint.toVector().subtract(session.origin.toVector());
        Vector previous = session.nodes.getLast();
        double length = previous.distance(offset);
        if (length <= 1.0E-4D) {
            message(player, "That waypoint duplicates the previous node.");
            return;
        }

        session.snapshot();
        session.nodes.add(offset);
        session.driver.moveTo(waypoint, session.speedModifier);
        message(player, "Added node " + (session.nodes.size() - 1) + " at " + vector(offset)
                + "; the route solver is resolving the walk asynchronously.");
    }

    public static void remove(Player player) {
        Session session = session(player);
        if (session == null) return;
        if (session.nodes.size() <= 1) {
            message(player, "A route must retain its origin node.");
            return;
        }
        session.snapshot();
        Vector removed = session.nodes.removeLast();
        message(player, "Removed node " + vector(removed) + ".");
    }

    public static void undo(Player player) {
        Session session = session(player);
        if (session == null) return;
        Snapshot snapshot = session.undo.pollFirst();
        if (snapshot == null) {
            message(player, "Nothing to undo.");
            return;
        }
        session.nodes.clear();
        snapshot.nodes.forEach(node -> session.nodes.add(node.clone()));
        session.mode = snapshot.mode;
        message(player, "Restored the previous route state.");
    }

    public static void mode(Player player, String rawMode) {
        Session session = session(player);
        if (session == null) return;
        PatrolMode mode;
        try {
            mode = PatrolMode.parse(rawMode);
        } catch (IllegalArgumentException exception) {
            message(player, "Mode must be LOOP or REVERSE.");
            return;
        }
        if (mode == session.mode) return;
        session.snapshot();
        session.mode = mode;
        message(player, "Patrol mode set to " + mode + ".");
    }

    public static void save(Player player) {
        Session session = session(player);
        if (session == null) return;
        PatrolRoute route;
        try {
            route = session.validate();
            session.persist(route);
        } catch (Exception exception) {
            message(player, "Could not save patrol: " + rootMessage(exception));
            return;
        }

        SESSIONS.remove(player.getUniqueId());
        session.close(false);
        PatrolService.refresh(session.owner);
        message(player, "Saved " + route.size() + " patrol nodes to " + session.filename()
                + " and refreshed the live actor.");
    }

    public static void cancel(Player player) {
        cancel(player, true);
    }

    public static void status(Player player) {
        Session session = SESSIONS.get(player.getUniqueId());
        if (session != null) {
            message(player, session.filename() + ": editing " + session.nodes.size() + " nodes, mode "
                    + session.mode + ", driver " + session.driver.status() + ".");
            return;
        }
        Object owner = targetedOwner(player);
        if (owner == null) {
            message(player, "No edit session; look at a patrolling actor to inspect it.");
            return;
        }
        Map<String, Object> diagnostics = PatrolService.diagnostics(owner);
        message(player, diagnostics.isEmpty() ? "That actor has no active patrol controller." : diagnostics.toString());
    }

    private static void cancel(Player player, boolean notify) {
        Session session = SESSIONS.remove(player.getUniqueId());
        if (session == null) {
            if (notify) message(player, "No patrol edit session is active.");
            return;
        }
        session.close(true);
        if (notify) message(player, "Patrol edit cancelled; no route changes were written.");
    }

    private static Session session(Player player) {
        Session session = SESSIONS.get(player.getUniqueId());
        if (session == null) message(player, "Start with /em patrol edit while looking at an actor.");
        return session;
    }

    private static Object targetedOwner(Player player) {
        RayTraceResult trace = player.getWorld().rayTraceEntities(
                player.getEyeLocation(), player.getEyeLocation().getDirection(), 12D, 0.5D,
                entity -> entity != player && (EntityTracker.getNPCEntity(entity) != null
                        || EntityTracker.getEliteMobEntity(entity) instanceof CustomBossEntity));
        Entity target = trace == null ? null : trace.getHitEntity();
        if (target == null) return null;
        NPCEntity npc = EntityTracker.getNPCEntity(target);
        if (npc != null) return npc;
        EliteEntity elite = EntityTracker.getEliteMobEntity(target);
        return elite instanceof CustomBossEntity customBoss ? customBoss : null;
    }

    private static void preview() {
        boolean renderRoute = ++previewTicks % 10 == 0;
        for (Session session : new ArrayList<>(SESSIONS.values())) {
            Player player = Bukkit.getPlayer(session.playerId);
            LivingEntity body = session.body();
            if (player == null || !player.isOnline() || body == null || !body.isValid()
                    || !body.getUniqueId().equals(session.bodyId)) {
                SESSIONS.remove(session.playerId, session);
                session.close(true);
                if (player != null && player.isOnline())
                    message(player, "Patrol edit cancelled because the puppet body changed.");
                continue;
            }
            if (renderRoute) session.render(player);
            PathfindingStatus current = session.driver.status();
            if (current != session.lastReportedStatus
                    && (current == PathfindingStatus.NO_PATH || current == PathfindingStatus.ENDED_SHORT
                    || current == PathfindingStatus.STUCK)) {
                message(player, "The puppet could not complete the latest leg (" + current + ").");
            }
            session.lastReportedStatus = current;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void protectPuppet(EntityDamageEvent event) {
        for (Session session : SESSIONS.values()) {
            LivingEntity body = session.body();
            if (body != null && body.getUniqueId().equals(event.getEntity().getUniqueId())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler public void quit(PlayerQuitEvent event) { cancel(event.getPlayer(), false); }
    @EventHandler public void die(PlayerDeathEvent event) { cancel(event.getEntity(), false); }
    @EventHandler public void changeWorld(PlayerChangedWorldEvent event) { cancel(event.getPlayer(), false); }

    private static void message(Player player, String message) {
        Logger.sendMessage(player, "&8[&6Patrol&8] &f" + message);
    }

    private static String vector(Vector vector) {
        return format(vector.getX()) + "," + format(vector.getY()) + "," + format(vector.getZ());
    }

    private static String format(double value) {
        if (Math.rint(value) == value) return Long.toString((long) value);
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    private static final class Session {
        private final UUID playerId;
        private final Object owner;
        private final Location origin;
        private final double speedModifier;
        private final boolean hadPatrol;
        private final UUID bodyId;
        private final PathfindingHandle driver;
        private final Deque<Snapshot> undo = new ArrayDeque<>();
        private final List<Vector> nodes = new ArrayList<>();
        private final Map<Location, BlockData> bedrockPreview = new LinkedHashMap<>();
        private PatrolMode mode;
        private PathfindingStatus lastReportedStatus = PathfindingStatus.IDLE;

        private Session(Player player,
                        Object owner,
                        Location origin,
                        PatrolRoute existing,
                        UUID bodyId,
                        PathfindingHandle driver) {
            this.playerId = player.getUniqueId();
            this.owner = owner;
            this.origin = origin.clone();
            this.speedModifier = existing == null ? 1D : existing.speedModifier();
            this.hadPatrol = existing != null;
            this.bodyId = bodyId;
            this.driver = driver;
            this.mode = existing == null ? PatrolMode.LOOP : existing.mode();
            if (existing == null) nodes.add(new Vector(0D, 0D, 0D));
            else existing.nodes().forEach(node -> nodes.add(node.clone()));
        }

        private static Session create(Player player, Object owner) {
            LivingEntity body;
            Location origin;
            PatrolRoute route;

            if (owner instanceof NPCEntity npc) {
                body = npc.getVillager();
                origin = npc.getSpawnLocation();
                route = npc.getNPCsConfigFields().getPatrolRoute();
                if (npc.getLocationString() == null)
                    throw new IllegalArgumentException("Runtime-only NPCs cannot be used as persistent route templates.");
                if (body != null && body.isInsideVehicle())
                    throw new IllegalArgumentException("A passenger cannot own a patrol; route its mount instead.");
                if (body instanceof Mob mob) {
                    mob.setAI(true);
                    mob.setAware(true);
                    if (NMSManager.getAdapter() == null || !NMSManager.getAdapter().removeFreeWill(body))
                        throw new IllegalArgumentException("This NPC body cannot be prepared for native pathfinding.");
                }
            } else if (owner instanceof CustomBossEntity boss) {
                body = boss.getLivingEntity();
                origin = boss.getSpawnLocation();
                route = boss.getCustomBossesConfigFields().getPatrolRoute();
                if (!boss.getCustomBossesConfigFields().isAi())
                    throw new IllegalArgumentException("This boss has ai: false and cannot patrol.");
                if (boss.getMovementSpeedAttribute() <= 0D)
                    throw new IllegalArgumentException("This boss has zero movement speed and cannot patrol.");
                if (body != null && body.isInsideVehicle())
                    throw new IllegalArgumentException("A passenger cannot own a patrol; route its mount instead.");
            } else {
                throw new IllegalArgumentException("Unsupported patrol actor.");
            }

            if (body == null || !body.isValid()) throw new IllegalArgumentException("The targeted actor has no live body.");
            if (origin == null || origin.getWorld() == null) throw new IllegalArgumentException("The actor has no authored origin.");
            if (NMSManager.getAdapter() == null)
                throw new IllegalArgumentException("No MagmaCore NMS adapter is active on this server version.");
            PathfindingHandle driver = NMSManager.getAdapter().createPathfindingHandle(body)
                    .orElseThrow(() -> new IllegalArgumentException("The targeted body has no native pathfinding handle."));
            return new Session(player, owner, origin, route, body.getUniqueId(), driver);
        }

        private LivingEntity body() {
            if (owner instanceof NPCEntity npc) return npc.getVillager();
            if (owner instanceof CustomBossEntity boss) return boss.getLivingEntity();
            return null;
        }

        private String filename() {
            if (owner instanceof NPCEntity npc) return npc.getNPCsConfigFields().getFilename();
            return ((CustomBossEntity) owner).getCustomBossesConfigFields().getFilename();
        }

        private void snapshot() {
            List<Vector> copy = nodes.stream().map(Vector::clone).toList();
            undo.addFirst(new Snapshot(copy, mode));
            while (undo.size() > 32) undo.removeLast();
        }

        private PatrolRoute validate() {
            YamlConfiguration validation = new YamlConfiguration();
            validation.set("patrol.enabled", true);
            validation.set("patrol.mode", mode.name());
            validation.set("patrol.speed", speedModifier);
            validation.set("patrol.nodes", nodes.stream().map(PatrolEditor::vector).toList());
            return Objects.requireNonNull(PatrolRoute.parse(validation));
        }

        private void persist(PatrolRoute route) throws IOException {
            if (owner instanceof NPCEntity npc) persistNpc(npc, route);
            else persistBoss((CustomBossEntity) owner, route);
        }

        private void persistNpc(NPCEntity npc, PatrolRoute route) throws IOException {
            NPCsConfigFields fields = npc.getNPCsConfigFields();
            FileConfiguration raw = fields.getWritableFileConfiguration();
            List<String> locations = new ArrayList<>(raw.getStringList("spawnLocations"));
            if (locations.size() <= 1) {
                applyRoute(raw, route);
                saveAtomic(raw, fields.getFile());
                if (!fields.reloadPatrolRoute()) throw new IOException("saved route did not parse");
                return;
            }

            String selected = npc.getLocationString();
            if (!locations.remove(selected))
                throw new IOException("target NPC location is not present in spawnLocations");
            File leafFile = nextLeaf(fields.getFile());
            YamlConfiguration leaf = new YamlConfiguration();
            leaf.set("extends", fields.getFilename());
            leaf.set("isEnabled", true);
            leaf.set("spawnLocations", List.of(selected));
            applyRoute(leaf, route);
            writeFork(fields.getFile(), raw, locations, leafFile, leaf);

            fields.setLocations(locations);
            NPCsConfigFields leafFields = NPCsConfig.registerRuntimeFile(leafFile);
            if (leafFields == null || leafFields.getPatrolRoute() == null)
                throw new IOException("runtime registration of " + leafFile.getName() + " failed");
            npc.rebindPatrolConfig(leafFields);
        }

        private void persistBoss(CustomBossEntity boss, PatrolRoute route) throws IOException {
            CustomBossesConfigFields fields = boss.getCustomBossesConfigFields();
            FileConfiguration raw = fields.getWritableFileConfiguration();
            List<String> locations = new ArrayList<>(raw.getStringList("spawnLocations"));
            if (locations.size() <= 1) {
                applyRoute(raw, route);
                saveAtomic(raw, fields.getFile());
                if (!fields.reloadPatrolRoute()) throw new IOException("saved route did not parse");
                return;
            }
            if (!(boss instanceof RegionalBossEntity regionalBoss))
                throw new IOException("multi-spawn boss routes require a regional boss instance");

            String current = regionalBoss.getConfigurationLocationString();
            String selected = locations.stream()
                    .filter(location -> sameConfiguredLocation(location, current))
                    .findFirst()
                    .orElseThrow(() -> new IOException("target boss location is not present in spawnLocations"));
            locations.remove(selected);
            File leafFile = nextLeaf(fields.getFile());
            YamlConfiguration leaf = new YamlConfiguration();
            leaf.set("extends", fields.getFilename());
            leaf.set("isEnabled", true);
            leaf.set("spawnLocations", List.of(selected));
            copyExplicitRoots(raw, leaf, List.of("leashRadius", "powers", "eliteScript"));
            applyRoute(leaf, route);
            writeFork(fields.getFile(), raw, locations, leafFile, leaf);

            fields.setSpawnLocations(locations);
            CustomBossesConfigFields leafFields = CustomBossesConfig.registerRuntimeFile(leafFile);
            if (leafFields == null || leafFields.getPatrolRoute() == null)
                throw new IOException("runtime registration of " + leafFile.getName() + " failed");
            regionalBoss.rebindPatrolConfig(leafFields);
        }

        private void writeFork(File baseFile,
                               FileConfiguration base,
                               List<String> remainingLocations,
                               File leafFile,
                               YamlConfiguration leaf) throws IOException {
            String originalBase = Files.readString(baseFile.toPath(), StandardCharsets.UTF_8);
            try {
                saveAtomic(leaf, leafFile);
                base.set("spawnLocations", remainingLocations);
                base.set("spawnLocation", null);
                saveAtomic(base, baseFile);
            } catch (IOException exception) {
                writeAtomic(baseFile.toPath(), originalBase);
                Files.deleteIfExists(leafFile.toPath());
                throw exception;
            }
        }

        private void render(Player player) {
            restoreBedrockPreview(player);
            if (player.getWorld() != origin.getWorld()) return;
            List<Location> points = nodes.stream().map(node -> origin.clone().add(node)).toList();
            if (points.isEmpty()) return;
            int segments = mode == PatrolMode.LOOP && points.size() > 2 ? points.size() : points.size() - 1;
            for (Location point : points) showPoint(player, point);
            for (int start = 0; start < segments; start++) {
                Location from = points.get(start);
                Location to = points.get((start + 1) % points.size());
                Vector delta = to.toVector().subtract(from.toVector());
                double length = delta.length();
                if (length <= 1.0E-6D) continue;
                Vector step = delta.normalize().multiply(0.75D);
                for (double distance = 0.75D; distance < length; distance += 0.75D)
                    showPoint(player, from.clone().add(step.clone().multiply(distance / 0.75D)));
            }
            for (Location resolvedPoint : driver.routePreview()) showPoint(player, resolvedPoint);
        }

        private void showPoint(Player player, Location location) {
            if (BedrockChecker.isBedrock(player)) {
                Location block = location.getBlock().getLocation();
                if (!block.getWorld().isChunkLoaded(block.getBlockX() >> 4, block.getBlockZ() >> 4)) return;
                if (bedrockPreview.size() >= 256 || bedrockPreview.containsKey(block)) return;
                bedrockPreview.put(block, block.getBlock().getBlockData());
                player.sendBlockChange(block, Material.GLASS.createBlockData());
            } else {
                player.spawnParticle(Particle.BLOCK_MARKER, location, 1, Material.BARRIER.createBlockData());
            }
        }

        private void restoreBedrockPreview(Player player) {
            for (Map.Entry<Location, BlockData> entry : bedrockPreview.entrySet())
                if (entry.getKey().getWorld() == player.getWorld())
                    player.sendBlockChange(entry.getKey(), entry.getValue());
            bedrockPreview.clear();
        }

        private void close(boolean cancelled) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) restoreBedrockPreview(player);
            driver.close();
            if (cancelled) {
                if (hadPatrol) PatrolService.refresh(owner);
                else if (owner instanceof NPCEntity npc && npc.getVillager() != null)
                    npc.getVillager().setAI(false);
            }
        }

        private static void applyRoute(FileConfiguration configuration, PatrolRoute route) {
            configuration.set("patrol.enabled", true);
            configuration.set("patrol.mode", route.mode().name());
            configuration.set("patrol.speed", route.speedModifier());
            configuration.set("patrol.maxLegDistance", null);
            configuration.set("patrol.virtualSpeed", route.virtualSpeed());
            configuration.set("patrol.nodes", route.serializeNodes());
        }

        private static void copyExplicitRoots(FileConfiguration source,
                                              FileConfiguration destination,
                                              List<String> roots) {
            for (Map.Entry<String, Object> entry : source.getValues(true).entrySet()) {
                String path = entry.getKey();
                String root = path.contains(".") ? path.substring(0, path.indexOf('.')) : path;
                if (!roots.contains(root) || entry.getValue() instanceof ConfigurationSection) continue;
                destination.set(path, entry.getValue());
            }
        }

        private static File nextLeaf(File baseFile) {
            String name = baseFile.getName();
            String stem = name.toLowerCase(Locale.ROOT).endsWith(".yml")
                    ? name.substring(0, name.length() - 4) : name;
            for (int index = 1; ; index++) {
                File candidate = new File(baseFile.getParentFile(), stem + "_patrol_" + index + ".yml");
                if (!candidate.exists()) return candidate;
            }
        }

        private static boolean sameConfiguredLocation(String left, String right) {
            if (left == null || right == null) return false;
            return locationPart(left).equals(locationPart(right));
        }

        private static String locationPart(String value) {
            int separator = value.indexOf(':');
            return separator < 0 ? value : value.substring(0, separator);
        }

        private static void saveAtomic(FileConfiguration configuration, File file) throws IOException {
            writeAtomic(file.toPath(), configuration.saveToString());
        }

        private static void writeAtomic(Path target, String contents) throws IOException {
            Files.createDirectories(target.toAbsolutePath().normalize().getParent());
            Path temporary = target.resolveSibling(target.getFileName() + ".patrol-" + UUID.randomUUID() + ".tmp");
            try {
                Files.writeString(temporary, contents, StandardCharsets.UTF_8);
                try {
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException ignored) {
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporary);
            }
        }
    }

    private record Snapshot(List<Vector> nodes, PatrolMode mode) {
    }
}
