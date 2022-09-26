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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

public class Explosion {

    private static final HashSet<Explosion> explosions = new HashSet<>();
    public final List<BlockState> detonatedBlocks = new ArrayList<>();
    private final int delayBeforeRegen = 2;

    public Explosion(List<BlockState> detonatedBlocks) {
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

    public static void regenerateAllPendingBlocks() {
        for (Explosion explosion : explosions)
            explosion.resetAllBlocks();
    }

    public static void generateFakeExplosion(List<Block> blockList, Entity entity, PowersConfigFields powersConfigFields, Location explosionSourceLocation) {
        generateExplosion(blockList, entity, powersConfigFields, explosionSourceLocation);
    }

    public static void generateFakeExplosion(List<Block> blockList, Entity entity) {
        generateExplosion(blockList, entity, null, null);
    }

    private static void generateExplosion(EntityExplodeEvent event) {
        generateExplosion(event.blockList(), event.getEntity(), null, event.getEntity().getLocation());
    }

    private static void generateExplosion(List<Block> blockList, Entity entity, PowersConfigFields powersConfigFields, Location explosionSource) {
        if (!DefaultConfig.isDoExplosionRegen()) return;
        if (EliteMobs.worldGuardIsEnabled &&
                explosionSource != null &&
                !WorldGuardFlagChecker.doExplosionRegenFlag(explosionSource))
            return;

        ArrayList<BlockState> blockStates = new ArrayList<>();

        for (Block block : blockList) {
            if (block.getType().isAir() ||
                    block.getType().equals(Material.FIRE) ||
                    block.isLiquid() ||
                    EntityTracker.isTemporaryBlock(block))
                continue;
            nearbyBlockScan(blockStates, block.getState());
        }

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
        if (eliteExplosionEvent.isCancelled()) return;

        if (explosionSource != null)
            eliteExplosionEvent.setExplosionSourceLocation(explosionSource);

        eliteExplosionEvent.visualExplosionEffect(powersConfigFields);

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

    private static void queueBlock(ArrayList<BlockState> blockStates, BlockState blockState) {
        if (!DefaultConfig.isDoRegenerateContainers() && blockState instanceof Container)
            return;
        blockStates.add(blockState.getBlock().getState());
        if (blockState instanceof Container)
            if (blockState instanceof Chest)
                ((Chest) blockState).getBlockInventory().setContents(new ItemStack[0]);
            else
                ((Container) blockState).getInventory().setContents(new ItemStack[0]);
    }

    public void resetAllBlocks() {
        for (BlockState blockState : detonatedBlocks)
            fullBlockRestore(blockState, true);
        detonatedBlocks.clear();
    }

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
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
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
