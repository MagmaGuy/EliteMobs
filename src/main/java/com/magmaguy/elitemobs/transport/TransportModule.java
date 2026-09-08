package com.magmaguy.elitemobs.transport;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.instanced.MatchDestroyEvent;
import com.magmaguy.elitemobs.api.instanced.MatchLeaveEvent;
import com.magmaguy.elitemobs.api.mind.*;
import com.magmaguy.elitemobs.instanced.InstancePlayerMovement;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.mobconstructor.*;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.magmacore.ai.*;
import com.magmaguy.magmacore.ai.route.*;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import java.util.*;

/** Owns boarding, flight and recovery. NPCs, commands and prop scripts all enter through start(). */
public final class TransportModule implements Listener, AutoCloseable {
    private static TransportModule instance;
    final TransportFiles files;
    private final Plugin plugin;
    private final TransportChunks chunks;
    private final TransportRecovery recovery;
    private final Map<UUID, Journey> journeys = new HashMap<>();
    private final Set<UUID> recovering = new HashSet<>();
    private final BukkitTask task;
    private final TransportEditor editor;
    private final TransportMenu menu;
    private volatile boolean closed;

    private TransportModule(Plugin plugin) throws Exception {
        this.plugin = plugin;
        files = new TransportFiles(plugin.getDataFolder().toPath(), plugin.getLogger());
        recovery = new TransportRecovery(files.journeys);
        chunks = new TransportChunks(plugin);
        editor = new TransportEditor(this, plugin);
        menu = new TransportMenu(this, plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1, 1);
    }
    public static void initialize() {
        shutdown();
        try {
            instance = new TransportModule(MetadataHandler.PLUGIN);
            for (Player player : Bukkit.getOnlinePlayers()) instance.recover(player);
        } catch (Exception failure) { MetadataHandler.PLUGIN.getLogger().severe("Transport unavailable: " + failure); }
    }
    public static void shutdown() { if (instance != null) instance.close(); instance = null; }
    public static TransportModule get() { return instance; }
    public static boolean isInTransit(Player player) {
        return instance != null && (instance.riding(player) || instance.recovering.contains(player.getUniqueId()));
    }
    public TransportEditor editor() { return editor; }
    public void openDestinations(Player player, com.magmaguy.elitemobs.npcs.NPCEntity npc) { menu.open(player, npc); }

    /** A server-thread entry point for authored prop scripts. Reports rejection to the player. */
    public static boolean startRoute(Player player, String routeId) {
        if (instance == null) { message(player, "Transport is unavailable."); return false; }
        return instance.start(player, instance.files.get(routeId));
    }
    public boolean start(Player player, TransportRoute route) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Transport starts on the server thread");
        if (closed || !player.isOnline() || player.isDead() || journeys.containsKey(player.getUniqueId())
                || player.isInsideVehicle() || journeys.size() >= 32) {
            message(player, "You cannot board right now."); return false;
        }
        if (route == null) { message(player, "Unknown transport route."); return false; }
        try {
            if (Bukkit.getServicesManager().load(EliteMindService.class) == null)
                throw new IllegalStateException("Native flying Minds are unavailable on this server version.");
            if (recovery.read(player.getUniqueId()) != null) { recover(player); return false; }
            World world = resolveWorld(player, route);
            CurvedRoute curve = route.curve();
            if (player.getLocation().toVector().distanceSquared(curve.at(0)) > 64)
                throw new IllegalArgumentException("Board at this route's departure point.");
            MatchInstance match = PlayerData.getMatchInstance(player);
            if (match != null) {
                for (double d = 0; d < curve.length() + 1; d += 1)
                    if (!match.authorizesTransport(player, curve.at(Math.min(d, curve.length())).toLocation(world)))
                        throw new IllegalArgumentException("This route leaves the current match's permitted area.");
            }
            Journey journey = new Journey(player, route, world, curve, match);
            journeys.put(player.getUniqueId(), journey);
            try { journey.terrain = chunks.acquire(world, curve); }
            catch (RuntimeException failure) { journeys.remove(player.getUniqueId()); throw failure; }
            message(player, "Preparing travel to " + route.name() + "...");
            journey.terrain.ready.whenComplete((ignored, failure) -> {
                if (closed || !plugin.isEnabled()) return;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (journeys.get(player.getUniqueId()) != journey) return;
                    if (failure != null) { finish(journey, false, "Route terrain could not be loaded."); return; }
                    String obstruction = TransportClearance.route(world, curve);
                    if (obstruction != null || !TransportClearance.landing(journey.destination)) {
                        finish(journey, false, "Route is unsafe: " + (obstruction == null ? "landing needs clear solid ground" : obstruction));
                        return;
                    }
                    journey.ready = true;
                    message(player, "Stand still. Departing for " + route.name() + " in " + route.countdown() / 20D + " seconds.");
                });
            });
            return true;
        } catch (Exception failure) { message(player, failure.getMessage()); return false; }
    }

    static World resolveWorld(Player player, TransportRoute route) {
        MatchInstance match = PlayerData.getMatchInstance(player);
        if (match instanceof DungeonInstance dungeon
                && route.world().equals(dungeon.getContentPackagesConfigFields().getWorldName())
                && player.getWorld().equals(dungeon.getWorld())) return dungeon.getWorld();
        if (!player.getWorld().getName().equals(route.world()))
            throw new IllegalArgumentException("This route belongs to another world.");
        return player.getWorld();
    }

    private void tick() {
        for (Journey j : List.copyOf(journeys.values())) {
            try {
                if (!j.player.isOnline() || j.player.isDead() || j.player.getWorld() != j.world
                        || PlayerData.getMatchInstance(j.player) != j.match || j.match != null && j.match.isDefunct()) {
                    finish(j, false, null); continue;
                }
                if (++j.age > 20 * 120 + j.curve.length() / j.route.speed() * 40) {
                    finish(j, false, "Travel timed out."); continue;
                }
                if (j.actor == null) {
                    if (j.player.getLocation().distanceSquared(j.departure) > .15 * .15) {
                        finish(j, false, "Departure cancelled because you moved."); continue;
                    }
                    if (j.ready && ++j.charge >= j.route.countdown()) board(j);
                } else if (!j.actor.getLivingEntity().isValid()
                        || j.player.getVehicle() != j.actor.getLivingEntity()) {
                    finish(j, false, "Travel interrupted.");
                } else if (j.status != RouteFlight.Status.FLYING) {
                    finish(j, j.status == RouteFlight.Status.ARRIVED,
                            j.status == RouteFlight.Status.ARRIVED ? "Arrived at " + j.route.name() + "." : "The route became obstructed.");
                }
            } catch (Exception failure) {
                plugin.getLogger().warning("Transport " + j.route.id() + ": " + failure);
                finish(j, false, "Travel could not continue safely.");
            }
        }
    }

    private void board(Journey j) throws Exception {
        if (j.player.isInsideVehicle()) throw new IllegalStateException("Already riding another entity");
        Location fallback = j.match == null ? j.departure : j.match.getPreviousPlayerLocations().get(j.player);
        if (fallback == null || fallback.getWorld() == null || fallback.getWorld() == j.world && j.match != null)
            fallback = Bukkit.getWorlds().getFirst().getSpawnLocation();
        recovery.write(j.player.getUniqueId(), j.destination, fallback, j.match == null ? null : j.match.getRuntimeId());
        j.journaled = true;
        j.flight = new RouteFlight(j.curve, j.route.speed(), j.route.acceleration());
        MindProgram program = MindProgram.builder("elitemobs:transport/" + j.route.id(), 1)
                .behavior(new MindBehavior() {
                    public String identifier() { return "elitemobs:transport/flight"; }
                    public Set<MindControl> controls() { return Set.of(MindControl.MOVE, MindControl.LOOK); }
                    public boolean canStart(MindContext context) { return journeys.get(j.player.getUniqueId()) == j; }
                    public void tick(MindContext context) {
                        Vector next = j.curve.at(Math.min(j.curve.length(), j.flight.distance() + 2));
                        if (TransportClearance.point(j.world, next) != null
                                || j.match != null && !j.match.authorizesTransport(j.player, next.toLocation(j.world))) {
                            context.actuator().stopMoving(); j.status = RouteFlight.Status.BLOCKED; return;
                        }
                        j.status = j.flight.tick(context);
                    }
                }).build();
        var profile = EliteMindBodyProfile.forCarrier(Objects.requireNonNull(NamespacedKey.fromString(j.route.carrier())), EliteMindBodyLocomotion.FLYING);
        j.actor = EliteMindServiceModule.spawnInternal(new InternalMindActorSpawnRequest(plugin,
                j.player.getUniqueId(), j.curve.at(0).toLocation(j.world), 1, profile, program, actor -> {
                    actor.setPersistent(false); actor.setEliteLoot(false); actor.setVanillaLoot(false); actor.setRandomLoot(false);
                    LivingEntity body = actor.getLivingEntity();
                    body.setPersistent(false); body.setRemoveWhenFarAway(false); body.setCanPickupItems(false);
                    body.setInvulnerable(true); body.setSilent(true); body.setCustomNameVisible(false);
                    body.setPortalCooldown(Integer.MAX_VALUE);
                    if (body.getEquipment() != null) body.getEquipment().clear();
                })).orElseThrow(() -> new IllegalStateException("Native flying Minds are unavailable on this server version"));
        LivingEntity body = j.actor.getLivingEntity();
        j.model = TransportModel.attach(body, j.route);
        var bounds = body.getBoundingBox();
        if (bounds.getWidthX() > 1.3 || bounds.getWidthZ() > 1.3 || bounds.getHeight() > 1.4)
            throw new IllegalArgumentException("Transport carrier exceeds the supported clearance envelope; use the pig carrier with a custom model");
        if (!body.addPassenger(j.player) || j.player.getVehicle() != body)
            throw new IllegalStateException("Could not seat the player on the mount");
        j.player.setFallDistance(0);
        j.world.playSound(body.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1, 1);
    }

    private void finish(Journey j, boolean arrived, String reason) {
        if (!journeys.remove(j.player.getUniqueId(), j)) return;
        try {
            if (j.actor != null) {
                j.player.leaveVehicle();
                if (j.model != null) try { j.model.close(); } catch (Exception error) { plugin.getLogger().warning("Transport model cleanup: " + error); }
                EliteMindServiceModule.clearInternal(j.actor);
            }
            if (j.journaled && !j.playerOffline && j.player.isOnline() && !j.player.isDead()) {
                Location target = arrived ? j.destination : j.departure;
                boolean moved = false;
                if (TransportClearance.landing(target) && j.player.getWorld() == j.world && PlayerData.getMatchInstance(j.player) == j.match) {
                    if (j.match == null) moved = j.player.teleport(target);
                    else if (j.match.authorizesTransport(j.player, target))
                        moved = InstancePlayerMovement.teleportWithinWorld(j.player, target, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
                if (moved) { j.player.setFallDistance(0); recovery.clear(j.player.getUniqueId()); }
                else recover(j.player);
            }
            if (reason != null && j.player.isOnline()) message(j.player, reason);
        } catch (Exception error) { plugin.getLogger().warning("Transport cleanup: " + error); }
        finally { if (j.terrain != null) j.terrain.close(); }
    }

    private void recover(Player player) {
        if (!player.isOnline() || player.isDead() || journeys.containsKey(player.getUniqueId())
                || recovering.contains(player.getUniqueId())) return;
        try {
            var saved = recovery.read(player.getUniqueId());
            if (saved == null) return;
            // Instance membership and destruction decide re-entry; never resurrect a deleted instance.
            MatchInstance match = PlayerData.getMatchInstance(player);
            Location target = saved.instanced() ? saved.fallback() : saved.destination();
            if (saved.instanced() && match != null && saved.destination() != null
                    && match.getRuntimeId().equals(saved.matchId())
                    && match.authorizesTransport(player, saved.destination())) target = saved.destination();
            if (target == null) target = saved.fallback();
            if (target == null) target = Bukkit.getWorlds().getFirst().getSpawnLocation();
            final Location destination = target.clone();
            recovering.add(player.getUniqueId());
            chunks.loadExisting(destination.getWorld(), destination.getBlockX() >> 4, destination.getBlockZ() >> 4)
                    .orTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .whenComplete((chunk, failure) -> {
                        if (closed || !plugin.isEnabled()) return;
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            recovering.remove(player.getUniqueId());
                            if (!player.isOnline() || player.isDead() || journeys.containsKey(player.getUniqueId())) return;
                            if (PlayerData.getMatchInstance(player) != match) { recover(player); return; }
                            try {
                                Location safe = failure == null && chunk != null && TransportClearance.landing(destination)
                                        ? destination : Bukkit.getWorlds().getFirst().getSpawnLocation();
                                MatchInstance current = PlayerData.getMatchInstance(player);
                                boolean success = current != null && current.authorizesTransport(player, safe)
                                        ? InstancePlayerMovement.teleportWithinWorld(player, safe, PlayerTeleportEvent.TeleportCause.PLUGIN)
                                        : current == null && player.teleport(safe);
                                if (success) { player.setFallDistance(0); recovery.clear(player.getUniqueId()); }
                            } catch (Exception error) { plugin.getLogger().warning("Transport recovery: " + error); }
                        });
                    });
        } catch (Exception error) {
            recovering.remove(player.getUniqueId());
            plugin.getLogger().warning("Transport recovery for " + player.getUniqueId() + ": " + error);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void dismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player player && riding(player)) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void vehicleExit(VehicleExitEvent event) {
        if (event.getExited() instanceof Player player && riding(player)) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void damage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && isInTransit(player)) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void attack(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        if (attacker instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) attacker = shooter;
        if (attacker instanceof Player player && isInTransit(player)) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void interact(PlayerInteractEvent event) { if (isInTransit(event.getPlayer())) event.setCancelled(true); }
    @EventHandler(priority = EventPriority.LOWEST)
    public void swap(PlayerSwapHandItemsEvent event) { if (isInTransit(event.getPlayer())) event.setCancelled(true); }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void launch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player player && isInTransit(player)) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void recoveringMovement(PlayerMoveEvent event) {
        if (!(event instanceof PlayerTeleportEvent) && recovering.contains(event.getPlayer().getUniqueId()))
            event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void teleport(PlayerTeleportEvent event) { if (riding(event.getPlayer())) event.setCancelled(true); }
    @EventHandler public void quit(PlayerQuitEvent event) {
        Journey j = journeys.get(event.getPlayer().getUniqueId());
        if (j != null) { j.playerOffline = true; finish(j, false, null); }
    }
    @EventHandler public void join(PlayerJoinEvent event) {
        recover(event.getPlayer());
    }
    @EventHandler public void respawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> recover(event.getPlayer()));
    }
    @EventHandler public void leave(MatchLeaveEvent event) {
        Journey j = journeys.get(event.getPlayer().getUniqueId()); if (j != null) finish(j, false, null);
        else Bukkit.getScheduler().runTask(plugin, () -> recover(event.getPlayer()));
    }
    @EventHandler public void destroy(MatchDestroyEvent event) {
        for (Journey j : List.copyOf(journeys.values())) if (j.match == event.getInstance()) finish(j, false, null);
    }
    private boolean riding(Player player) {
        Journey j = journeys.get(player.getUniqueId()); return j != null && j.actor != null;
    }
    public void cancel(Player player) {
        Journey j = journeys.get(player.getUniqueId()); if (j != null) finish(j, false, "Travel cancelled.");
    }
    @Override public void close() {
        for (Journey j : List.copyOf(journeys.values())) finish(j, j.match == null, null);
        closed = true; task.cancel(); editor.close(); menu.close(); chunks.close(); recovering.clear(); HandlerList.unregisterAll(this);
    }
    static void message(Player player, String text) { player.sendMessage(ChatColorConverter.convert("&6[Travel] &f" + text)); }

    private static final class Journey {
        final Player player; final TransportRoute route; final World world; final CurvedRoute curve;
        final MatchInstance match; final Location departure; final Location destination;
        TransportChunks.Lease terrain; EliteEntity actor; AutoCloseable model; RouteFlight flight;
        RouteFlight.Status status = RouteFlight.Status.FLYING;
        boolean ready, journaled, playerOffline; int age, charge;
        Journey(Player player, TransportRoute route, World world, CurvedRoute curve, MatchInstance match) {
            this.player = player; this.route = route; this.world = world; this.curve = curve; this.match = match;
            departure = player.getLocation().clone(); destination = curve.at(curve.length()).toLocation(world);
            destination.setYaw(route.arrivalYaw());
        }
    }
}
