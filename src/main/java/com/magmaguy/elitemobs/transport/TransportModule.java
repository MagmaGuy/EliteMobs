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
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;
import java.util.function.Supplier;

/** Follows authored curves with configured mounts. Map makers own path clearance. */
public final class TransportModule implements Listener, AutoCloseable {
    private static TransportModule instance;
    /** Paper's dismount events say whether they can be cancelled (death, quit and vehicle removal cannot); Spigot's API has no such method. */
    private static final java.lang.reflect.Method DISMOUNT_CANCELLABLE = dismountCancellable();
    final TransportFiles files;
    /** Route ids whose mount failure was already logged since the last reload; the fallback repeats on every boarding. */
    private final Set<String> warnedMounts = new HashSet<>();
    private final Plugin plugin;
    private final Map<UUID, Journey> journeys = new HashMap<>();
    private final BukkitTask task;
    private final TransportEditor editor;
    private final TransportMenu menu;
    private final NamespacedKey pendingLanding;
    private final NetworkEditor networkEditor;
    private volatile boolean closed;

    private TransportModule(Plugin plugin) throws Exception {
        this.plugin = plugin;
        files = new TransportFiles(plugin.getDataFolder().toPath(), plugin.getLogger());
        editor = new TransportEditor(this, plugin);
        menu = new TransportMenu(this, plugin);
        pendingLanding = new NamespacedKey(plugin, "pending_flight_landing");
        networkEditor = new NetworkEditor(this, plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1, 1);
        // Players who joined while transport was down (async init, reload, failed init) still carry their landing.
        for (Player online : Bukkit.getOnlinePlayers()) applyPendingLanding(online);
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
    public NetworkEditor networkEditor() { return networkEditor; }
    public void openDestinations(Player player, com.magmaguy.elitemobs.npcs.NPCEntity npc) {
        var fields = npc.getNPCsConfigFields();
        if (fields.getTransportNetwork() == null) { menu.open(player, npc); return; }
        openFlightPoint(player, TransportMenu.npcAnchor(npc), fields.getTransportNetwork(), fields.getTransportNode(),
                fields.getTransportDestination());
    }

    /** Flies from one network node to a destination, checking the departure and destination permissions. */
    public boolean startFlight(Player player, String networkId, String fromNode, String toNode) {
        return startFlight(player, networkId, fromNode, toNode, null, 0);
    }

    private boolean startFlight(Player player, String networkId, String fromNode, String toNode,
                                Location departureAnchor, double departureRadius) {
        TransportNetwork network = files.network(networkId);
        if (network == null) { message(player, "Unknown flight network " + networkId + "."); return false; }
        TransportNode from = network.nodes().get(fromNode), to = network.nodes().get(toNode);
        if (from == null || to == null || !to.destination()) { message(player, "Unknown flight point."); return false; }
        if (!from.unlockedFor(player)) { message(player, TransportMenu.lockedMessage(from)); return false; }
        if (!to.unlockedFor(player)) { message(player, TransportMenu.lockedMessage(to)); return false; }
        try { return start(player, network.route(fromNode, toNode), departureAnchor, departureRadius); }
        catch (IllegalArgumentException failure) { message(player, failure.getMessage()); return false; }
    }

    /** Opens a flight point's menu, or flies straight to its fixed destination. */
    public void openFlightPoint(Player player, Supplier<Location> anchor, String networkId, String node, String destination) {
        openFlightPoint(player, anchor, networkId, node, destination, null, 0);
    }

    /**
     * As above, but a direct flight's countdown only cancels once the player leaves the departure zone, so a
     * player still settling into a portal keeps their seat. The menu path keeps the any-movement rule.
     */
    public void openFlightPoint(Player player, Supplier<Location> anchor, String networkId, String node, String destination,
                                Location departureAnchor, double departureRadius) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Flight points open on the server thread");
        if (destination != null && !destination.isBlank()) {
            startFlight(player, networkId, node, destination, departureAnchor, departureRadius); return;
        }
        TransportNetwork network = files.network(networkId);
        if (network == null) { message(player, "Unknown flight network " + networkId + "."); return; }
        menu.openNetwork(player, anchor, network, node);
    }

    /** A server-thread entry point for authored prop scripts. Reports rejection to the player. */
    public static boolean startRoute(Player player, String routeId) {
        if (instance == null) { message(player, "Transport is unavailable."); return false; }
        return instance.start(player, instance.files.get(routeId));
    }
    public boolean start(Player player, TransportRoute route) {
        return start(player, route, null, 0);
    }

    private boolean start(Player player, TransportRoute route, Location departureAnchor, double departureRadius) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Transport starts on the server thread");
        if (closed || !player.isOnline() || player.isDead() || journeys.containsKey(player.getUniqueId())) {
            message(player, "You cannot board right now."); return false;
        }
        if (route == null) { message(player, "Unknown transport route."); return false; }
        World world;
        CurvedRoute curve;
        try { world = resolveWorld(player, route); curve = route.curve(); }
        catch (Exception failure) { message(player, failure.getMessage()); return false; }
        if (Bukkit.getServicesManager().load(EliteMindService.class) == null) return teleportInstead(player, route, world, curve);
        Journey journey = new Journey(player, route, world, curve, PlayerData.getMatchInstance(player));
        journey.departureAnchor = departureAnchor == null ? null : departureAnchor.clone();
        journey.departureRadius = departureRadius;
        journeys.put(player.getUniqueId(), journey);
        try {
            requireTransportEntity(route);
            prepareMount(journey);
        } catch (Exception | LinkageError failure) {
            finish(journey, false, null);
            if (warnedMounts.add(route.id()))
                plugin.getLogger().warning("Transport " + route.id() + " could not spawn its mount; teleporting instead: " + failure);
            if (player.hasPermission("elitemobs.transport.admin"))
                message(player, "&cMount unavailable, teleporting instead: "
                        + (failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage()));
            return teleportInstead(player, route, world, curve);
        }
        message(player, "Stand still. Departing for " + route.name() + " in " + route.countdown() / 20D + " seconds.");
        return true;
    }

    private boolean teleportInstead(Player player, TransportRoute route, World world, CurvedRoute curve) {
        Location destination = curve.at(curve.length()).toLocation(world);
        destination.setYaw(route.arrivalYaw());
        if (!InstancePlayerMovement.teleportWithinWorld(player, destination, PlayerTeleportEvent.TeleportCause.PLUGIN)) {
            message(player, "Travel failed."); return false;
        }
        player.setFallDistance(0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
        markArrivalPortal(player, destination);
        message(player, "Arrived at " + route.name() + ".");
        return true;
    }

    /** Arriving inside a flight portal must not reopen it until the player steps out. */
    private static void markArrivalPortal(Player player, Location destination) {
        var wormholes = com.magmaguy.elitemobs.wormhole.WormholeManager.getInstance(true);
        if (wormholes != null) wormholes.addPlayerToCooldown(player, destination);
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
                    // A dismount the seat lock could not or should not stop (death, teleport, removal) ends the flight.
                    finish(j, false, "Travel interrupted.");
                } else if (j.status != RouteFlight.Status.FLYING) {
                    finish(j, j.status == RouteFlight.Status.ARRIVED,
                            j.status == RouteFlight.Status.ARRIVED ? "Arrived at " + j.route.name() + "." : "The mount does not support flight.");
                } else j.releasing = false; // Still seated after a teleport that kept the vehicle, so lock the seat again.
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
        j.player.getPersistentDataContainer().set(pendingLanding, PersistentDataType.STRING, encode(j.destination));
        j.player.setFallDistance(0);
        j.world.playSound(body.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1, 1);
    }

    private void finish(Journey j, boolean arrived, String reason) {
        if (!journeys.remove(j.player.getUniqueId(), j)) return;
        j.releasing = true; // Our own dismount below must not be refused by the seat lock.
        boolean placed = false;
        try {
            if (j.actor != null) {
                try {
                    if (j.player.getVehicle() == j.actor.getLivingEntity()) j.player.leaveVehicle();
                } finally {
                    EliteMindServiceModule.clearInternal(j.actor);
                }
            }
            if (arrived && j.player.isOnline() && !j.player.isDead()) {
                boolean accepted = InstancePlayerMovement.teleportWithinWorld(j.player, j.destination,
                        PlayerTeleportEvent.TeleportCause.PLUGIN);
                Location actual = j.player.getLocation();
                placed = accepted && actual.getWorld().equals(j.destination.getWorld())
                        && actual.distanceSquared(j.destination) < 0.000001D;
                if (placed) { j.player.setFallDistance(0); markArrivalPortal(j.player, j.destination); }
                else reason = "Travel interrupted. The final destination could not be reached.";
            } else if (arrived) {
                reason = "Travel interrupted.";
            }
            if (reason != null && j.player.isOnline()) message(j.player, reason);
        } catch (Exception error) {
            plugin.getLogger().warning("Transport cleanup: " + error);
            if (j.player.isOnline()) message(j.player, "Travel interrupted.");
        } finally {
            // Kept for a rider who quit mid-air, or whose shutdown landing failed; the login and init sweeps retry it.
            // An online rider whose ordinary arrival failed is already at the route's end, so a later retry would only surprise them.
            boolean retry = j.keepLanding || arrived && !placed && closed && j.match == null;
            if (!retry) j.player.getPersistentDataContainer().remove(pendingLanding);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void moveDuringCountdown(PlayerMoveEvent event) {
        Journey journey = journeys.get(event.getPlayer().getUniqueId());
        if (journey == null || journey.boarded || event.getTo() == null) return;
        Location from = event.getFrom(), to = event.getTo();
        if (journey.departureAnchor != null) {
            // Portal departures only cancel once the player leaves the portal's zone.
            if (to.getWorld() != journey.departureAnchor.getWorld()
                    || to.distanceSquared(journey.departureAnchor) > journey.departureRadius * journey.departureRadius)
                finish(journey, false, "Departure cancelled because you moved.");
            return;
        }
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())
            finish(journey, false, "Departure cancelled because you moved.");
    }

    /** A rider who presses sneak mid-flight stays seated. Every other way off the mount still works. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void keepSeated(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Journey j = journeys.get(player.getUniqueId());
        if (j == null || !j.boarded || j.releasing || j.actor == null) return;
        if (player.isDead() || player.getGameMode() == GameMode.SPECTATOR) return;
        LivingEntity body = j.actor.getLivingEntity();
        // A removed mount is already invalid while it ejects its passengers.
        if (body == null || !body.isValid() || !event.getDismounted().getUniqueId().equals(body.getUniqueId())) return;
        if (dismountCancellable(event)) event.setCancelled(true);
    }

    /** Teleports fire before their dismount. One that goes ahead unseats the rider, and the tick ends the flight. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void releaseOnTeleport(PlayerTeleportEvent event) {
        Journey j = journeys.get(event.getPlayer().getUniqueId());
        if (j != null && j.boarded) j.releasing = true;
    }

    private static java.lang.reflect.Method dismountCancellable() {
        try { return EntityDismountEvent.class.getMethod("isCancellable"); }
        catch (NoSuchMethodException spigot) { return null; }
    }

    private static boolean dismountCancellable(EntityDismountEvent event) {
        if (DISMOUNT_CANCELLABLE == null) return true;
        try { return (boolean) DISMOUNT_CANCELLABLE.invoke(event); }
        catch (ReflectiveOperationException unexpected) { return false; }
    }

    @EventHandler public void quit(PlayerQuitEvent event) {
        networkEditor.end(event.getPlayer().getUniqueId());
        Journey j = journeys.get(event.getPlayer().getUniqueId());
        // Instance riders are returned by instance recovery; instance world names are reused, so no key points into one.
        if (j != null) { j.keepLanding = j.boarded && j.match == null; finish(j, false, null); }
    }
    @EventHandler public void join(PlayerJoinEvent event) { applyPendingLanding(event.getPlayer()); }

    private void applyPendingLanding(Player player) {
        var data = player.getPersistentDataContainer();
        String stored = data.get(pendingLanding, PersistentDataType.STRING);
        if (stored == null) return;
        data.remove(pendingLanding);
        Location landing = decode(stored);
        if (landing == null) return; // That world is gone; instance recovery owns players returning to removed instances.
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline() || !player.teleport(landing)) return;
            player.setFallDistance(0);
            markArrivalPortal(player, landing);
        });
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
        closed = true;
        // Boarded riders land at their destination rather than mid-air.
        for (Journey j : List.copyOf(journeys.values())) finish(j, j.boarded && j.player.isOnline(), null);
        task.cancel(); editor.close(); menu.close(); networkEditor.close(); HandlerList.unregisterAll(this);
    }
    static void message(Player player, String text) { player.sendMessage(ChatColorConverter.convert("&6[Travel] &f" + text)); }

    private static String encode(Location location) {
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw();
    }

    private static Location decode(String text) {
        String[] values = text.split(",");
        if (values.length != 5) return null;
        World world = Bukkit.getWorld(values[0]);
        if (world == null) return null;
        try {
            return new Location(world, Double.parseDouble(values[1]), Double.parseDouble(values[2]),
                    Double.parseDouble(values[3]), Float.parseFloat(values[4]), 0);
        } catch (NumberFormatException invalid) { return null; }
    }

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
        boolean boarded, keepLanding; int charge;
        /** Set when a dismount from the mount is intended: our own release, or a teleport that is going ahead. */
        boolean releasing;
        // Optional zone the countdown tolerates movement within; null means any movement cancels.
        Location departureAnchor; double departureRadius;
        Journey(Player player, TransportRoute route, World world, CurvedRoute curve, MatchInstance match) {
            this.player = player; this.route = route; this.world = world; this.curve = curve; this.match = match;
            destination = curve.at(curve.length()).toLocation(world);
            destination.setYaw(route.arrivalYaw());
        }
    }
}
