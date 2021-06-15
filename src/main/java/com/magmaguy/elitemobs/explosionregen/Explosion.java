package com.magmaguy.elitemobs.explosionregen;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteExplosionEvent;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.entitytracker.TemporaryBlockTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.utils.EntityFinder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

public class Explosion {

    public static HashSet<Explosion> explosions = new HashSet();

    public static void regenerateAllPendingBlocks() {
        for (Explosion explosion : explosions)
            explosion.resetAllBlocks();
    }

    public ArrayList<BlockState> detonatedBlocks = new ArrayList<>();

    public Explosion(ArrayList<BlockState> detonatedBlocks) {
        if (detonatedBlocks == null || detonatedBlocks.isEmpty()) return;
        //sort blocks bottom to top
        HashMap<BlockState, Integer> unsortedBlocks = new HashMap<>();
        for (BlockState blockState : detonatedBlocks)
            unsortedBlocks.put(blockState, blockState.getY());

        unsortedBlocks.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .forEachOrdered(x -> this.detonatedBlocks.add(x.getKey()));

        explosions.add(this);
        regenerate();
    }

    public void resetAllBlocks() {
        for (BlockState blockState : detonatedBlocks)
            fullBlockRestore(blockState, true);
        detonatedBlocks.clear();
    }

    private final int delayBeforeRegen = 2;

    public void regenerate() {

        Explosion explosion = this;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (detonatedBlocks.isEmpty()) {
                    explosions.remove(explosion);
                    cancel();
                    return;
                }

                BlockState firstBlock = null;
                for (BlockState block : detonatedBlocks) {
                    firstBlock = block;
                    break;
                }

                fullBlockRestore(firstBlock, false);

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20 * 60 * delayBeforeRegen, 1);

    }

    private void fullBlockRestore(BlockState blockState, boolean isShutdown) {

        for (Entity entity : blockState.getWorld().getNearbyEntities(new BoundingBox(blockState.getX(), blockState.getY(), blockState.getZ(),
                blockState.getX() + 1, blockState.getY() + 1, blockState.getZ() + 1)))
            entity.teleport(entity.getLocation().clone().add(new Vector(0, 1, 0)));

        blockState.setBlockData(blockState.getBlockData());

        if (blockState instanceof Container) {

            Inventory container = null;

            switch (blockState.getType()) {

                case LECTERN:
                    container = ((Lectern) blockState).getInventory();
                    break;

                case JUKEBOX:
                    //((Jukebox) blockState).setRecord(this.items.get(0));
                    blockState.update(true, false);
                    break;

                default:
                    container = ((Container) blockState).getInventory();
            }

            if (container != null) container.setContents(((Container) blockState).getInventory().getContents());
        }

        blockState.update(true);
        if (!isShutdown)
            detonatedBlocks.remove(blockState);
    }

    public static class ExplosionEvent implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void entityExplodeEvent(EntityExplodeEvent event) {
            EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            if (eliteMobEntity != null) {
                generateExplosion(event);
                return;
            }
            Projectile eliteProjectile = EntityTracker.getProjectileEntity(event.getEntity().getUniqueId());
            if (eliteProjectile != null) {
                generateExplosion(event);
            }
        }
    }

    public static void generateFakeExplosion(List<Block> blockList, Entity entity, ElitePower elitePower, Location explosionSourceLocation) {
        generateExplosion(blockList, entity, elitePower, explosionSourceLocation);
    }

    public static void generateFakeExplosion(List<Block> blockList, Entity entity) {
        generateExplosion(blockList, entity, null, null);
    }

    private static void generateExplosion(EntityExplodeEvent event) {
        generateExplosion(event.blockList(), event.getEntity(), null, null);
    }

    private static void generateExplosion(List<Block> blockList, Entity entity, ElitePower elitePower, Location explosionSource) {

        ArrayList<BlockState> blockStates = new ArrayList<>();

        for (Block block : blockList) {
            if (block.getType().isAir() ||
                    block.getType().equals(Material.FIRE) ||
                    block.isLiquid())
                continue;
            if (TemporaryBlockTracker.temporaryBlocks.contains(block))
                continue;
            nearbyBlockScan(blockStates, block.getState());
        }

        Entity shooter = EntityFinder.filterRangedDamagers(entity);
        EliteMobEntity eliteMobEntity = null;
        if (shooter != null)
            eliteMobEntity = EntityTracker.getEliteMobEntity(shooter);

        EliteExplosionEvent eliteExplosionEvent = null;

        //for projectiles
        if (entity instanceof Projectile) {
            eliteExplosionEvent = new EliteExplosionEvent(
                    eliteMobEntity,
                    elitePower = ElitePower.getElitePower(EliteProjectile.readExplosivePower((Projectile) entity)),
                    entity.getLocation(),
                    blockStates);
            if (eliteExplosionEvent.isCancelled()) return;

        } else {
            eliteExplosionEvent = new EliteExplosionEvent(
                    eliteMobEntity,
                    elitePower,
                    entity.getLocation(),
                    blockStates);
            if (eliteExplosionEvent.isCancelled()) return;
        }

        if (explosionSource != null)
            eliteExplosionEvent.setExplosionSourceLocation(explosionSource);

        eliteExplosionEvent.visualExplosionEffect(elitePower);

        for (BlockState blockState : blockStates) {
            blockState.getBlock().setType(Material.AIR);
            blockState.getBlock().getState().update(true);
        }

        new Explosion(blockStates);

    }

    /**
     * This scans the blocks adjacent to the block getting blown up. This is because certain blocks like ladders will break
     * when lacking the support of the source block
     *
     * @param blockState
     */
    private static void nearbyBlockScan(ArrayList<BlockState> blockStates, BlockState blockState) {
        queueBlock(blockStates, blockState);
        for (int x = -1; x < 2; x++)
            for (int y = -1; y < 2; y++)
                for (int z = -1; z < 2; z++) {
                    Location blockLocation = blockState.getLocation().clone().add(new Vector(x, y, z));
                    BlockState iteratedBlockState = blockLocation.getBlock().getState();
                    if (blockStates.contains(iteratedBlockState)) continue;
                    if (!isCodependentBlock(iteratedBlockState, y)) continue;
                    nearbyBlockScan(blockStates, iteratedBlockState);
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
            switch (blockState.getType()) {
                case VINE:
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

    private static void queueBlock(ArrayList<BlockState> blockStates, BlockState blockState) {
        blockStates.add(blockState.getBlock().getState());
        if (blockState instanceof Container)
            ((Container) blockState).getInventory().setContents(new ItemStack[0]);
    }

}
