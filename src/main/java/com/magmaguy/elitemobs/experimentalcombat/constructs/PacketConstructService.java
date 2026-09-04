package com.magmaguy.elitemobs.experimentalcombat.constructs;

import com.magmaguy.elitemobs.experimentalcombat.MonotonicTickClock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Owns temporary packet-only block constructs without changing the world.
 *
 * <p>A single reconciler handles fixed and moving layouts, viewer range, overlapping casts and
 * exact restoration. Removing a top construct reveals any active construct below it; removing the
 * final layer sends the viewer the world's current block data. No operation loads a chunk.</p>
 */
public final class PacketConstructService implements Listener, AutoCloseable {
    private static final long UPDATE_PERIOD_TICKS = 2L;

    private final Plugin plugin;
    private final Map<UUID, Session> sessions = new LinkedHashMap<>();
    private final Map<UUID, ViewerState> viewers = new HashMap<>();
    private final BukkitTask updateTask;
    private boolean closed;

    public PacketConstructService(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        updateTask = Bukkit.getScheduler().runTaskTimer(
                plugin, (Runnable) this::tick, 1L, UPDATE_PERIOD_TICKS);
    }

    public Optional<Handle> spawn(
            Player owner,
            Location anchor,
            PacketConstructDefinition definition,
            int durationTicks) {
        return spawn(owner, anchor, definition, durationTicks, ignored -> { });
    }

    public Optional<Handle> spawn(
            Player owner,
            Location anchor,
            PacketConstructDefinition definition,
            int durationTicks,
            Consumer<UUID> onRemoved) {
        Objects.requireNonNull(anchor, "anchor");
        Location fixed = anchor.clone();
        return spawnFollowing(owner, () -> fixed, definition, durationTicks, onRemoved);
    }

    public Optional<Handle> spawnFollowing(
            Player owner,
            Supplier<Location> anchor,
            PacketConstructDefinition definition,
            int durationTicks) {
        return spawnFollowing(owner, anchor, definition, durationTicks, ignored -> { });
    }

    public Optional<Handle> spawnFollowing(
            Player owner,
            Supplier<Location> anchor,
            PacketConstructDefinition definition,
            int durationTicks,
            Consumer<UUID> onRemoved) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(onRemoved, "onRemoved");
        if (closed || !Bukkit.isPrimaryThread() || durationTicks < 1
                || !owner.isOnline() || !owner.isValid() || owner.isDead()) return Optional.empty();
        Location initial = resolve(anchor);
        if (initial == null || initial.getWorld() == null
                || !initial.getWorld().equals(owner.getWorld())) return Optional.empty();

        long now = MonotonicTickClock.currentTick();
        long expiresAt = Long.MAX_VALUE - now < durationTicks
                ? Long.MAX_VALUE
                : now + durationTicks;
        UUID id = UUID.randomUUID();
        Session session = new Session(
                id, owner.getUniqueId(), definition, anchor, initial.getWorld().getUID(),
                expiresAt, onRemoved);
        sessions.put(id, session);
        reconcile();
        return Optional.of(new HandleImpl(id));
    }

    public void deactivate(Player owner) {
        if (owner != null) deactivate(owner.getUniqueId());
    }

    public void deactivate(UUID ownerId) {
        if (ownerId == null) return;
        sessions.values().stream()
                .filter(session -> session.ownerId().equals(ownerId))
                .map(Session::id)
                .toList()
                .forEach(this::remove);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        deactivate(event.getPlayer());
        viewers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        deactivate(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        deactivate(event.getPlayer());
        clearViewer(event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        UUID worldId = event.getWorld().getUID();
        sessions.values().stream()
                .filter(session -> session.worldId().equals(worldId))
                .map(Session::id)
                .toList()
                .forEach(this::remove);
        for (Player player : List.copyOf(event.getWorld().getPlayers())) clearViewer(player, false);
    }

    private void tick() {
        if (!closed) reconcile();
    }

    private void reconcile() {
        long now = MonotonicTickClock.currentTick();
        Map<UUID, Frame> frames = new LinkedHashMap<>();
        for (Session session : List.copyOf(sessions.values())) {
            Player owner = Bukkit.getPlayer(session.ownerId());
            Location anchor = resolve(session.anchor());
            if (now >= session.expiresAtTick()
                    || owner == null || !owner.isOnline() || !owner.isValid() || owner.isDead()
                    || anchor == null || anchor.getWorld() == null
                    || !anchor.getWorld().getUID().equals(session.worldId())) {
                remove(session.id());
                continue;
            }
            frames.put(session.id(), new Frame(session, anchor, blocks(session.definition(), anchor)));
        }

        Set<UUID> online = new LinkedHashSet<>();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            online.add(viewer.getUniqueId());
            reconcileViewer(viewer, frames);
        }
        viewers.keySet().removeIf(playerId -> !online.contains(playerId));
    }

    private void reconcileViewer(Player player, Map<UUID, Frame> frames) {
        ViewerState viewer = viewers.computeIfAbsent(player.getUniqueId(), ignored -> new ViewerState());
        Set<UUID> desired = new LinkedHashSet<>();
        for (Frame frame : frames.values()) {
            if (!canSee(player, frame) || frame.blocks().isEmpty()) continue;
            desired.add(frame.session().id());
            send(player, viewer.layers().replace(frame.session().id(), frame.blocks()));
        }
        for (UUID stale : new ArrayList<>(viewer.visibleConstructs())) {
            if (!desired.contains(stale)) send(player, viewer.layers().remove(stale));
        }
        viewer.visibleConstructs().clear();
        viewer.visibleConstructs().addAll(desired);
        if (viewer.layers().isEmpty() && viewer.visibleConstructs().isEmpty()) {
            viewers.remove(player.getUniqueId(), viewer);
        }
    }

    private void remove(UUID constructId) {
        Session removed = sessions.remove(constructId);
        if (removed == null) return;
        for (Map.Entry<UUID, ViewerState> entry : new ArrayList<>(viewers.entrySet())) {
            ViewerState state = entry.getValue();
            if (!state.visibleConstructs().remove(constructId)) continue;
            Player player = Bukkit.getPlayer(entry.getKey());
            List<PacketConstructLayerIndex.VisibilityChange<BlockCoordinate, String>> changes =
                    state.layers().remove(constructId);
            if (player != null && player.isOnline()) send(player, changes);
            if (state.layers().isEmpty() && state.visibleConstructs().isEmpty()) {
                viewers.remove(entry.getKey(), state);
            }
        }
        try {
            removed.onRemoved().accept(removed.id());
        } catch (RuntimeException exception) {
            plugin.getLogger().log(Level.FINE,
                    "Could not report packet construct removal " + removed.id(), exception);
        }
    }

    private void clearViewer(Player player, boolean sendRestoration) {
        ViewerState state = viewers.remove(player.getUniqueId());
        if (state == null) return;
        for (UUID constructId : List.copyOf(state.visibleConstructs())) {
            List<PacketConstructLayerIndex.VisibilityChange<BlockCoordinate, String>> changes =
                    state.layers().remove(constructId);
            if (sendRestoration && player.isOnline()) send(player, changes);
        }
        state.visibleConstructs().clear();
    }

    private void send(
            Player player,
            List<PacketConstructLayerIndex.VisibilityChange<BlockCoordinate, String>> changes) {
        for (PacketConstructLayerIndex.VisibilityChange<BlockCoordinate, String> change : changes) {
            BlockCoordinate position = change.key();
            World world = Bukkit.getWorld(position.worldId());
            if (world == null || !player.getWorld().equals(world)
                    || !world.isChunkLoaded(position.x() >> 4, position.z() >> 4)) continue;
            try {
                Location location = position.location(world);
                BlockData data = change.after().isPresent()
                        ? Bukkit.createBlockData(change.after().orElseThrow())
                        : world.getBlockAt(position.x(), position.y(), position.z()).getBlockData();
                player.sendBlockChange(location, data);
            } catch (RuntimeException exception) {
                plugin.getLogger().log(Level.FINE,
                        "Could not update packet-only class construct for " + player.getName(), exception);
            }
        }
    }

    private static Map<BlockCoordinate, String> blocks(
            PacketConstructDefinition definition,
            Location anchor) {
        World world = anchor.getWorld();
        if (world == null) return Map.of();
        Map<BlockCoordinate, String> blocks = new LinkedHashMap<>();
        for (PacketConstructDefinition.BlockVisual visual : definition.blocks()) {
            int x = anchor.getBlockX() + visual.relativeX();
            int y = anchor.getBlockY() + visual.relativeY();
            int z = anchor.getBlockZ() + visual.relativeZ();
            if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
            blocks.put(new BlockCoordinate(world.getUID(), x, y, z), visual.serializedBlockData());
        }
        return Map.copyOf(blocks);
    }

    private static boolean canSee(Player player, Frame frame) {
        if (!player.isOnline() || !player.isValid()
                || !player.getWorld().equals(frame.anchor().getWorld())) return false;
        double range = frame.session().definition().viewRange();
        return player.getLocation().distanceSquared(frame.anchor()) <= range * range;
    }

    private static Location resolve(Supplier<Location> anchor) {
        try {
            Location location = anchor.get();
            return location == null ? null : location.clone();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        updateTask.cancel();
        for (UUID constructId : new ArrayList<>(sessions.keySet())) remove(constructId);
        for (Player player : Bukkit.getOnlinePlayers()) clearViewer(player, true);
        sessions.clear();
        viewers.clear();
        HandlerList.unregisterAll(this);
    }

    public interface Handle extends AutoCloseable {
        UUID id();

        boolean active();

        @Override
        void close();
    }

    private final class HandleImpl implements Handle {
        private final UUID id;

        private HandleImpl(UUID id) {
            this.id = id;
        }

        @Override
        public UUID id() {
            return id;
        }

        @Override
        public boolean active() {
            return sessions.containsKey(id);
        }

        @Override
        public void close() {
            if (closed || !sessions.containsKey(id)) return;
            if (Bukkit.isPrimaryThread()) remove(id);
            else Bukkit.getScheduler().runTask(plugin, () -> remove(id));
        }
    }

    private record Session(
            UUID id,
            UUID ownerId,
            PacketConstructDefinition definition,
            Supplier<Location> anchor,
            UUID worldId,
            long expiresAtTick,
            Consumer<UUID> onRemoved) {
    }

    private record Frame(
            Session session,
            Location anchor,
            Map<BlockCoordinate, String> blocks) {
    }

    private record BlockCoordinate(UUID worldId, int x, int y, int z) {
        private Location location(World world) {
            return new Location(world, x, y, z);
        }
    }

    private record ViewerState(
            PacketConstructLayerIndex<BlockCoordinate, String> layers,
            Set<UUID> visibleConstructs) {
        private ViewerState() {
            this(new PacketConstructLayerIndex<>(), new LinkedHashSet<>());
        }
    }
}
