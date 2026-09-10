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
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;

/** Follows authored curves with configured mounts. Map makers own path clearance. */
public final class TransportModule implements Listener, AutoCloseable {
    private static TransportModule instance;
    final TransportFiles files;
    private final Plugin plugin;
    private final Map<UUID, Journey> journeys = new HashMap<>();
    private final BukkitTask task;
    private final TransportEditor editor;
    private final TransportMenu menu;
    private volatile boolean closed;

    private TransportModule(Plugin plugin) throws Exception {
        this.plugin = plugin;
        files = new TransportFiles(plugin.getDataFolder().toPath(), plugin.getLogger());
        editor = new TransportEditor(this, plugin);
        menu = new TransportMenu(this, plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1, 1);
    }
    public static void initialize() {
        shutdown();
        try {
            instance = new TransportModule(MetadataHandler.PLUGIN);
        } catch (Exception failure) { MetadataHandler.PLUGIN.getLogger().severe("Transport unavailable: " + failure); }
    }
    public static void shutdown() { if (instance != null) instance.close(); instance = null; }
    public static TransportModule get() { return instance; }
    public static boolean isInTransit(Player player) {
        return instance != null && instance.riding(player);
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
        if (closed || !player.isOnline() || player.isDead() || journeys.containsKey(player.getUniqueId())) {
            message(player, "You cannot board right now."); return false;
        }
        if (route == null) { message(player, "Unknown transport route."); return false; }
        try {
            if (Bukkit.getServicesManager().load(EliteMindService.class) == null)
                throw new IllegalStateException("Native flying Minds are unavailable on this server version.");
            requireTransportEntity(route);
            World world = resolveWorld(player, route);
            CurvedRoute curve = route.curve();
            MatchInstance match = PlayerData.getMatchInstance(player);
            Journey journey = new Journey(player, route, world, curve, match);
            journeys.put(player.getUniqueId(), journey);
            try {
                prepareMount(journey);
            } catch (Exception | LinkageError failure) {
                finish(journey, false, null);
                throw failure;
            }
            message(player, "Stand still. Departing for " + route.name() + " in " + route.countdown() / 20D + " seconds.");
            return true;
        } catch (Exception | LinkageError failure) { message(player, failure.getMessage()); return false; }
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
                if (!j.actor.getLivingEntity().isValid()) {
                    finish(j, false, "Travel interrupted."); continue;
                }
                if (!j.boarded) {
                    if (++j.charge >= j.route.countdown()) board(j);
                } else if (j.player.getVehicle() != j.actor.getLivingEntity()) {
                    finish(j, false, "Travel interrupted.");
                } else if (j.status != RouteFlight.Status.FLYING) {
                    finish(j, j.status == RouteFlight.Status.ARRIVED,
                            j.status == RouteFlight.Status.ARRIVED ? "Arrived at " + j.route.name() + "." : "The mount does not support flight.");
                }
            } catch (Exception | LinkageError failure) {
                plugin.getLogger().log(java.util.logging.Level.WARNING, "Transport " + j.route.id() + " failed", failure);
                finish(j, false, "Travel failed: " + failure.getMessage());
            }
        }
    }

    private void prepareMount(Journey j) throws Exception {
        j.flight = new RouteFlight(j.curve, j.route.speed(), j.route.acceleration());
        MindProgram program = MindProgram.builder("elitemobs:transport/" + j.route.id(), 1)
                .behavior(new MindBehavior() {
                    public String identifier() { return "elitemobs:transport/flight"; }
                    public Set<MindControl> controls() { return Set.of(MindControl.MOVE, MindControl.LOOK); }
                    public boolean canStart(MindContext context) { return journeys.get(j.player.getUniqueId()) == j && j.boarded; }
                    public void tick(MindContext context) {
                        j.status = j.flight.tick(context);
                    }
                }).build();
        var entityConfig = requireTransportEntity(j.route);
        var profile = new EliteMindBodyProfile(EliteMindBodyLocomotion.FLYING, entityConfig.getScale(),
                true, entityConfig.getEntityType().getKey());
        j.actor = EliteMindServiceModule.spawnInternal(new InternalMindActorSpawnRequest(plugin,
                j.player.getUniqueId(), j.curve.at(0).toLocation(j.world), 1, profile, program, actor -> {
                    LivingEntity body = actor.getLivingEntity();
                    body.setPersistent(false); body.setRemoveWhenFarAway(false); body.setCanPickupItems(false);
                    body.setInvulnerable(true);
                    body.setGravity(false);
                }, entityConfig)).orElseThrow(() -> new IllegalStateException("Native flying Minds are unavailable on this server version"));
    }

    private void board(Journey j) throws Exception {
        LivingEntity body = j.actor.getLivingEntity();
        j.player.leaveVehicle();
        if (!body.addPassenger(j.player) || j.player.getVehicle() != body)
            throw new IllegalStateException("Could not seat the player on the mount");
        j.boarded = true;
        j.player.setFallDistance(0);
        j.world.playSound(body.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1, 1);
    }

    private void finish(Journey j, boolean arrived, String reason) {
        if (!journeys.remove(j.player.getUniqueId(), j)) return;
        try {
            if (j.actor != null) {
                if (j.player.getVehicle() == j.actor.getLivingEntity()) j.player.leaveVehicle();
                EliteMindServiceModule.clearInternal(j.actor);
            }
            if (arrived && j.player.isOnline() && !j.player.isDead()) {
                InstancePlayerMovement.teleportWithinWorld(j.player, j.destination, PlayerTeleportEvent.TeleportCause.PLUGIN);
                j.player.setFallDistance(0);
            }
            if (reason != null && j.player.isOnline()) message(j.player, reason);
        } catch (Exception error) { plugin.getLogger().warning("Transport cleanup: " + error); }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void moveDuringCountdown(PlayerMoveEvent event) {
        Journey journey = journeys.get(event.getPlayer().getUniqueId());
        if (journey == null || journey.boarded || event.getTo() == null) return;
        Location from = event.getFrom(), to = event.getTo();
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())
            finish(journey, false, "Departure cancelled because you moved.");
    }

    @EventHandler public void quit(PlayerQuitEvent event) {
        Journey j = journeys.get(event.getPlayer().getUniqueId());
        if (j != null) finish(j, false, null);
    }
    @EventHandler public void leave(MatchLeaveEvent event) {
        Journey j = journeys.get(event.getPlayer().getUniqueId()); if (j != null) finish(j, false, null);
    }
    @EventHandler public void destroy(MatchDestroyEvent event) {
        for (Journey j : List.copyOf(journeys.values())) if (j.match == event.getInstance()) finish(j, false, null);
    }
    private boolean riding(Player player) {
        Journey j = journeys.get(player.getUniqueId()); return j != null && j.boarded;
    }
    public void cancel(Player player) {
        Journey j = journeys.get(player.getUniqueId()); if (j != null) finish(j, false, "Travel cancelled.");
    }
    @Override public void close() {
        for (Journey j : List.copyOf(journeys.values())) finish(j, false, null);
        closed = true; task.cancel(); editor.close(); menu.close(); HandlerList.unregisterAll(this);
    }
    static void message(Player player, String text) { player.sendMessage(ChatColorConverter.convert("&6[Travel] &f" + text)); }

    private static com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields requireTransportEntity(TransportRoute route) {
        var config = com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig.getCustomBoss(route.transportEntity());
        if (config == null || !config.isEnabled())
            throw new IllegalArgumentException("Transport entity is missing or disabled: " + route.transportEntity());
        return config;
    }

    private static final class Journey {
        final Player player; final TransportRoute route; final World world; final CurvedRoute curve;
        final MatchInstance match; final Location destination;
        EliteEntity actor; RouteFlight flight;
        RouteFlight.Status status = RouteFlight.Status.FLYING;
        boolean boarded; int charge;
        Journey(Player player, TransportRoute route, World world, CurvedRoute curve, MatchInstance match) {
            this.player = player; this.route = route; this.world = world; this.curve = curve; this.match = match;
            destination = curve.at(curve.length()).toLocation(world);
            destination.setYaw(route.arrivalYaw());
        }
    }
}
