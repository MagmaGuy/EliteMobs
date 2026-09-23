package com.magmaguy.elitemobs.explosionregen;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteExplosionEvent;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.EntityFinder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitTask;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

public class Explosion {

    private static final HashSet<Explosion> explosions = new HashSet<>();
    private final Deque<BlockState> detonatedBlocks = new ArrayDeque<>();
    private BukkitTask task;
    private World world;
    private final int delayBeforeRegen = 1;
    private UUID worldUUID;

    public Explosion(List<BlockState> detonatedBlocks) {
        if (detonatedBlocks == null || detonatedBlocks.isEmpty()) return;
        Map<BlockPosition, BlockState> unique = new LinkedHashMap<>();
        for (BlockState block : detonatedBlocks) unique.putIfAbsent(BlockPosition.of(block), block);
        unique.values().stream().sorted(java.util.Comparator.comparingInt(BlockState::getY)).forEach(this.detonatedBlocks::addLast);
        world = this.detonatedBlocks.getFirst().getWorld();
        worldUUID = world.getUID();

        explosions.add(this);
        regenerate();
    }

    public static void shutdown() {
        regenerateAllPendingBlocks();
    }

    public static void regenerateAllPendingBlocks() {
        for (Explosion explosion : List.copyOf(explosions))
            explosion.resetAllBlocks();
    }

    public static void generateFakeExplosion(List<Block> blockList, Entity entity, PowersConfigFields powersConfigFields, Location explosionSourceLocation) {
        generateExplosion(blockList, entity, powersConfigFields, explosionSourceLocation);
    }

    public static void generateFakeExplosion(List<Block> blockList, Entity entity) {
        generateExplosion(blockList, entity, null, null);
    }

    private static void generateExplosion(EntityExplodeEvent event) {
        Generation result = generateExplosion(event.blockList(), event.getEntity(), null, event.getEntity().getLocation());
        if (result == Generation.CANCELLED) event.setCancelled(true);
        else if (result == Generation.APPLIED) event.blockList().clear();
    }

    private enum Generation { BYPASS, CANCELLED, APPLIED }

    private static Generation generateExplosion(List<Block> blockList, Entity entity, PowersConfigFields powersConfigFields, Location explosionSource) {
        if (!DefaultConfig.isDoExplosionRegen()) return Generation.BYPASS;
        if (EliteMobs.worldGuardIsEnabled &&
                explosionSource != null &&
                !WorldGuardFlagChecker.doExplosionRegenFlag(explosionSource))
            return Generation.BYPASS;

        Map<BlockPosition, BlockState> captured = new LinkedHashMap<>();
        Set<BlockPosition> visited = new HashSet<>();
        for (Block block : blockList) {
            if (block.getType().isAir() ||
                    block.getType().equals(Material.FIRE) ||
                    block.isLiquid() ||
                    EntityTracker.isTemporaryBlock(block))
                continue;
            nearbyBlockScan(captured, visited, block.getState());
        }
        ArrayList<BlockState> blockStates = new ArrayList<>(captured.values());

        Entity shooter = EntityFinder.filterRangedDamagers(entity);
        EliteEntity eliteEntity = null;
        if (shooter != null)
            eliteEntity = EntityTracker.getEliteMobEntity(shooter);

        EliteExplosionEvent eliteExplosionEvent = null;

        //for projectiles
        if (entity instanceof Projectile) {
            eliteExplosionEvent = new EliteExplosionEvent(
                    eliteEntity,
                    powersConfigFields = PowersConfig.getPower(EliteProjectile.readExplosivePower((Projectile) entity)),
                    entity.getLocation(),
                    blockStates);
        } else {
            eliteExplosionEvent = new EliteExplosionEvent(
                    eliteEntity,
                    powersConfigFields,
                    entity.getLocation(),
                    blockStates);
        }
        if (explosionSource != null) eliteExplosionEvent.setExplosionSourceLocation(explosionSource);
        Bukkit.getPluginManager().callEvent(eliteExplosionEvent);
        if (eliteExplosionEvent.isCancelled()) return Generation.CANCELLED;
        // Honor listener edits while retaining each position's first authoritative snapshot.
        Map<BlockPosition, BlockState> approved = new LinkedHashMap<>();
        for (BlockState state : blockStates) {
            if (state == null || !state.getWorld().equals(entity.getWorld())) continue;
            if (EntityTracker.isTemporaryBlock(state.getBlock())) continue;
            if (!DefaultConfig.isDoRegenerateContainers() && state instanceof Container) continue;
            approved.putIfAbsent(BlockPosition.of(state), state);
        }
        blockStates.clear();
        blockStates.addAll(approved.values());
        eliteExplosionEvent.visualExplosionEffect(powersConfigFields);
        for (BlockState blockState : blockStates) {
            BlockState live = blockState.getBlock().getState();
            if (live instanceof Chest chest) chest.getBlockInventory().clear();
            else if (live instanceof Container container) container.getInventory().clear();
            blockState.getBlock().setType(Material.AIR);
            blockState.getBlock().getState().update(true);
        }

        new Explosion(blockStates);
        return Generation.APPLIED;
    }

    /**
     * This scans the blocks adjacent to the block getting blown up. This is because certain blocks like ladders will break
     * when lacking the support of the source block
     *
     * @param blockState
     */
    private record BlockPosition(UUID world, int x, int y, int z) {
        static BlockPosition of(BlockState state) {
            return new BlockPosition(state.getWorld().getUID(), state.getX(), state.getY(), state.getZ());
        }
    }

    private static void nearbyBlockScan(Map<BlockPosition, BlockState> states, Set<BlockPosition> visited, BlockState start) {
        Deque<BlockState> pending = new ArrayDeque<>();
        pending.add(start);
        while (!pending.isEmpty()) {
            BlockState state = pending.removeFirst();
            BlockPosition key = BlockPosition.of(state);
            if (!visited.add(key)) continue;
            if (EntityTracker.isTemporaryBlock(state.getBlock())) continue;
            if (!DefaultConfig.isDoRegenerateContainers() && state instanceof Container) continue;
            states.putIfAbsent(key, state);
            for (int x = -1; x <= 1; x++)
                for (int y = -1; y <= 1; y++)
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        BlockPosition neighbor = new BlockPosition(key.world, key.x + x, key.y + y, key.z + z);
                        if (visited.contains(neighbor)) continue;
                        BlockState adjacent = state.getBlock().getRelative(x, y, z).getState();
                        if (isCodependentBlock(adjacent, y)) pending.addLast(adjacent);
                    }
        }
    }

    private static boolean isCodependentBlock(BlockState blockState, int y) {
        //Getter for blocks that will break if the block below breaks
        if (y == 1) {
            switch (blockState.getType()) {
                case SUGAR_CANE:
                case STRUCTURE_BLOCK:
                case TALL_GRASS:

                case BLACK_CARPET:
                case BLUE_CARPET:
                case BROWN_CARPET:
                case CYAN_CARPET:
                case GRAY_CARPET:
                case GREEN_CARPET:
                case LIGHT_BLUE_CARPET:
                case LIGHT_GRAY_CARPET:
                case LIME_CARPET:
                case MAGENTA_CARPET:
                case ORANGE_CARPET:
                case CARVED_PUMPKIN:
                case PINK_CARPET:
                case PURPLE_CARPET:
                case RED_CARPET:
                case WHITE_CARPET:
                case YELLOW_CARPET:

                case DARK_OAK_DOOR:
                case ACACIA_DOOR:
                case BIRCH_DOOR:
                case CRIMSON_DOOR:
                case IRON_DOOR:
                case JUNGLE_DOOR:
                case OAK_DOOR:
                case SPRUCE_DOOR:
                case WARPED_DOOR:


                case SUNFLOWER:
                case WHEAT:
                case BROWN_MUSHROOM:
                case RED_MUSHROOM:
                case DANDELION:
                case NETHER_WART:


                case REDSTONE:
                case COMPARATOR:
                case REPEATER:

                case TORCH:
                case REDSTONE_TORCH:

                    return true;
            }
        }

        //Getter for blocks that will break if the block above goes away
        if (y == -1) {
            if (blockState.getType() == Material.VINE) {
                return true;
            }
        }

        //Generic getter for codependent blocks, blocks that would break if the adjacent block breaks
        switch (blockState.getType()) {
            case PAINTING:
            case LADDER:
            case LANTERN:
            case VINE:
            case SOUL_LANTERN:

            case TRIPWIRE:
            case TRIPWIRE_HOOK:

            case REDSTONE_WALL_TORCH:
            case WALL_TORCH:

            case ACACIA_TRAPDOOR:
            case BIRCH_TRAPDOOR:
            case CRIMSON_TRAPDOOR:
            case DARK_OAK_TRAPDOOR:
            case IRON_TRAPDOOR:
            case JUNGLE_TRAPDOOR:
            case OAK_TRAPDOOR:
            case SPRUCE_TRAPDOOR:
            case WARPED_TRAPDOOR:

            case COCOA_BEANS:
                return true;

            default:
                return false;
        }
    }

    public void resetAllBlocks() {
        while (!detonatedBlocks.isEmpty()) {
            if (!fullBlockRestore(detonatedBlocks.getFirst())) return;
            detonatedBlocks.removeFirst();
        }
        discard();
    }

    public void regenerate() {
        if (task != null || detonatedBlocks.isEmpty()) return;
        task = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, () -> {
            if (Bukkit.getWorld(worldUUID) != world) {
                task.cancel();
                task = null;
                Logger.warn("Explosion recovery suspended: original world " + worldUUID + " is unavailable.");
                return;
            }
            if (detonatedBlocks.isEmpty()) { discard(); return; }
            if (fullBlockRestore(detonatedBlocks.getFirst())) detonatedBlocks.removeFirst();
            if (detonatedBlocks.isEmpty()) discard();
        }, 20L * 60 * delayBeforeRegen, 1L);
    }

    private boolean fullBlockRestore(BlockState state) {
        if (Bukkit.getWorld(worldUUID) != world) return false;
        for (Entity entity : world.getNearbyEntities(new BoundingBox(state.getX(), state.getY(), state.getZ(),
                state.getX() + 1, state.getY() + 1, state.getZ() + 1)))
            entity.teleport(entity.getLocation().clone().add(new Vector(0, 1, 0)));
        // Tile snapshots carry their saved inventory. Never substitute the live empty inventory.
        return state.update(true, false);
    }

    private void discard() {
        if (task != null) task.cancel();
        task = null;
        detonatedBlocks.clear();
        explosions.remove(this);
        world = null;
    }

    /** Only the instance retirement owner calls this after a disposable world actually unloads. */
    public static void discardForWorld(UUID worldId) {
        for (Explosion explosion : List.copyOf(explosions))
            if (worldId.equals(explosion.worldUUID)) explosion.discard();
    }

    public static class ExplosionEvent implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onWorldUnload(WorldUnloadEvent event) {
            UUID worldId = event.getWorld().getUID();
            if (DungeonInstance.isWorldRetiring(worldId)) return;
            for (Explosion explosion : List.copyOf(explosions)) {
                if (!worldId.equals(explosion.worldUUID)) continue;
                try { explosion.resetAllBlocks(); }
                catch (RuntimeException failure) {
                    Logger.warn("Could not finish explosion recovery before world unload: " + failure.getMessage());
                }
                if (!explosion.detonatedBlocks.isEmpty()) {
                    event.setCancelled(true);
                    Logger.warn("World unload postponed until pending explosion blocks can be restored.");
                }
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void entityExplodeEvent(EntityExplodeEvent event) {
            Entity entity = event.getEntity();
            if (entity instanceof Projectile projectile && projectile.getShooter() instanceof LivingEntity shooter)
                entity = shooter;
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(entity);
            if (eliteEntity != null) {
                generateExplosion(event);
                return;
            }
            if (EntityTracker.isProjectileEntity(event.getEntity()))
                generateExplosion(event);
            //binder of worlds fight bypass, set in the custom reinforcements
            if (event.getEntity().getPersistentDataContainer().has(new NamespacedKey(MetadataHandler.PLUGIN, "eliteCrystal"), PersistentDataType.STRING))
                generateExplosion(event);
        }
    }

}
