package com.magmaguy.elitemobs.advancedcombat.minions;

import com.magmaguy.elitemobs.advancedcombat.MonotonicTickClock;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The one real-block class construct. Every purple portal block is tied to a cast and never acts
 * as transportation. Other class constructs remain packet-only presentation.
 */
final class AbyssalGateManager implements Listener, AutoCloseable {
    private final Plugin plugin;
    private final Map<UUID, AbyssalGateOwnershipLedger> ledgersByWorld = new HashMap<>();
    private final Map<UUID, GateCast> casts = new LinkedHashMap<>();
    private boolean closed;

    AbyssalGateManager(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    Optional<GateHandle> place(Player owner, Location aimed, int durationTicks) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(aimed, "aimed");
        if (closed || aimed.getWorld() == null || durationTicks < 1) return Optional.empty();
        World world = aimed.getWorld();
        GateAxis axis = facingAxis(owner.getEyeLocation().getDirection());
        AbyssalGateOwnershipLedger ledger = ledger(world.getUID());
        AbyssalGateFootprint footprint = AbyssalGateFootprintPlanner.find(
                position(aimed), axis, new BukkitProbe(world, ledger, axis)).orElse(null);
        if (footprint == null) return Optional.empty();

        UUID gateId = UUID.randomUUID();
        ledger.reserve(gateId, footprint.blocks());
        boolean placed = false;
        try {
            BlockData portalData = Material.NETHER_PORTAL.createBlockData();
            if (portalData instanceof Orientable orientable) {
                orientable.setAxis(axis == GateAxis.X ? Axis.X : Axis.Z);
            }
            for (GateBlockPosition position : footprint.blocks()) {
                Block block = block(world, position);
                if (block.getType() == Material.NETHER_PORTAL && ledger.isOwned(position)) continue;
                if (!block.getType().isAir()) return Optional.empty();
                block.setBlockData(portalData.clone(), false);
                if (block.getType() != Material.NETHER_PORTAL) return Optional.empty();
            }
            long now = MonotonicTickClock.currentTick();
            long expiresAt = Long.MAX_VALUE - now < durationTicks
                    ? Long.MAX_VALUE
                    : now + durationTicks;
            casts.put(gateId, new GateCast(
                    gateId, owner.getUniqueId(), world.getUID(), footprint, expiresAt));
            placed = true;
            return Optional.of(new GateHandle(gateId));
        } finally {
            if (!placed) clearReservation(world, ledger, gateId);
        }
    }

    void tick(long currentTick) {
        if (closed) return;
        List<UUID> expired = casts.values().stream()
                .filter(cast -> currentTick >= cast.expiresAtTick())
                .map(GateCast::gateId)
                .toList();
        for (UUID gateId : expired) remove(gateId);
    }

    void removeOwner(UUID ownerId) {
        List<UUID> owned = casts.values().stream()
                .filter(cast -> cast.playerOwnerId().equals(ownerId))
                .map(GateCast::gateId)
                .toList();
        for (UUID gateId : owned) remove(gateId);
    }

    void remove(GateHandle handle) {
        if (handle != null) remove(handle.gateId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPhysics(BlockPhysicsEvent event) {
        if (ownedPortal(event.getBlock())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (ownedPortal(event.getBlock())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (owned(event.getBlockPlaced())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFlow(BlockFromToEvent event) {
        if (owned(event.getToBlock())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.getBlocks().stream().anyMatch(this::owned)
                || event.getBlocks().stream().map(block -> block.getRelative(event.getDirection()))
                .anyMatch(this::owned)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.getBlocks().stream().anyMatch(this::owned)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplosion(EntityExplodeEvent event) {
        event.blockList().removeIf(this::owned);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplosion(BlockExplodeEvent event) {
        event.blockList().removeIf(this::owned);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPortalCreate(PortalCreateEvent event) {
        if (event.getBlocks().stream().anyMatch(state -> owned(state.getBlock()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (touchesOwnedPortal(event.getPlayer(), event.getFrom())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event) {
        if (touchesOwnedPortal(event.getEntity(), event.getFrom())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        UUID worldId = event.getWorld().getUID();
        int chunkX = event.getChunk().getX();
        int chunkZ = event.getChunk().getZ();
        List<UUID> affected = casts.values().stream()
                .filter(cast -> cast.worldId().equals(worldId))
                .filter(cast -> cast.footprint().blocks().stream()
                        .anyMatch(block -> (block.x() >> 4) == chunkX && (block.z() >> 4) == chunkZ))
                .map(GateCast::gateId)
                .toList();
        for (UUID gateId : affected) remove(gateId);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldUnload(WorldUnloadEvent event) {
        UUID worldId = event.getWorld().getUID();
        List<UUID> affected = casts.values().stream()
                .filter(cast -> cast.worldId().equals(worldId))
                .map(GateCast::gateId)
                .toList();
        for (UUID gateId : affected) remove(gateId);
        ledgersByWorld.remove(worldId);
    }

    @Override
    public void close() {
        if (closed) return;
        for (UUID gateId : new ArrayList<>(casts.keySet())) remove(gateId);
        casts.clear();
        ledgersByWorld.clear();
        closed = true;
        HandlerList.unregisterAll(this);
    }

    private void remove(UUID gateId) {
        GateCast cast = casts.remove(gateId);
        if (cast == null) return;
        World world = Bukkit.getWorld(cast.worldId());
        AbyssalGateOwnershipLedger ledger = ledgersByWorld.get(cast.worldId());
        if (ledger == null) return;
        Set<GateBlockPosition> clearable = ledger.release(gateId);
        if (world != null) clearBlocks(world, clearable);
        if (ledger.isEmpty()) ledgersByWorld.remove(cast.worldId());
    }

    private void clearReservation(
            World world,
            AbyssalGateOwnershipLedger ledger,
            UUID gateId) {
        clearBlocks(world, ledger.release(gateId));
        if (ledger.isEmpty()) ledgersByWorld.remove(world.getUID());
    }

    private static void clearBlocks(World world, Set<GateBlockPosition> positions) {
        for (GateBlockPosition position : positions) {
            if (!world.isChunkLoaded(position.x() >> 4, position.z() >> 4)) continue;
            Block block = block(world, position);
            if (block.getType() == Material.NETHER_PORTAL) block.setType(Material.AIR, false);
        }
    }

    private boolean touchesOwnedPortal(Entity entity, Location from) {
        if (from != null && ownedPortal(from.getBlock())) return true;
        BoundingBox bounds = entity.getBoundingBox().expand(.05D);
        World world = entity.getWorld();
        int minX = floor(bounds.getMinX());
        int maxX = floor(bounds.getMaxX());
        int minY = floor(bounds.getMinY());
        int maxY = floor(bounds.getMaxY());
        int minZ = floor(bounds.getMinZ());
        int maxZ = floor(bounds.getMaxZ());
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (ownedPortal(block)) return true;
                }
            }
        }
        return false;
    }

    private boolean ownedPortal(Block block) {
        return block.getType() == Material.NETHER_PORTAL && owned(block);
    }

    private boolean owned(Block block) {
        AbyssalGateOwnershipLedger ledger = ledgersByWorld.get(block.getWorld().getUID());
        return ledger != null && ledger.isOwned(position(block.getLocation()));
    }

    private AbyssalGateOwnershipLedger ledger(UUID worldId) {
        return ledgersByWorld.computeIfAbsent(worldId, ignored -> new AbyssalGateOwnershipLedger());
    }

    private static GateAxis facingAxis(Vector direction) {
        return Math.abs(direction.getX()) >= Math.abs(direction.getZ()) ? GateAxis.Z : GateAxis.X;
    }

    private static GateBlockPosition position(Location location) {
        return new GateBlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private static Block block(World world, GateBlockPosition position) {
        return world.getBlockAt(position.x(), position.y(), position.z());
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }

    record GateHandle(UUID gateId) {
        GateHandle {
            Objects.requireNonNull(gateId, "gateId");
        }
    }

    private record GateCast(
            UUID gateId,
            UUID playerOwnerId,
            UUID worldId,
            AbyssalGateFootprint footprint,
            long expiresAtTick) {
    }

    private static final class BukkitProbe implements AbyssalGateFootprintPlanner.Probe {
        private final World world;
        private final AbyssalGateOwnershipLedger ledger;
        private final GateAxis axis;

        private BukkitProbe(World world, AbyssalGateOwnershipLedger ledger, GateAxis axis) {
            this.world = world;
            this.ledger = ledger;
            this.axis = axis;
        }

        @Override
        public boolean loaded(GateBlockPosition position) {
            return world.isChunkLoaded(position.x() >> 4, position.z() >> 4);
        }

        @Override
        public boolean solid(GateBlockPosition position) {
            return block(world, position).getType().isSolid();
        }

        @Override
        public boolean available(GateBlockPosition position) {
            Block block = block(world, position);
            if (block.getType().isAir()) return true;
            if (block.getType() != Material.NETHER_PORTAL || !ledger.isOwned(position)) return false;
            return !(block.getBlockData() instanceof Orientable orientable)
                    || orientable.getAxis() == (axis == GateAxis.X ? Axis.X : Axis.Z);
        }
    }
}
