package com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks;

import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.dungeons.SchematicPackage;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.utils.ChunkLocationChecker;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TransitiveBossBlock implements Listener {

    private static void setBlockData(RegionalBossEntity regionalBossEntity, TransitiveBlock transitiveBlock, Location spawnLocation) {
        Location location;
        if (!ChunkLocationChecker.locationIsLoaded(spawnLocation)) return;
        double rotation = 0;
        if (regionalBossEntity.getEmPackage() instanceof SchematicPackage)
            rotation = regionalBossEntity.getEmPackage().getDungeonPackagerConfigFields().getCalculatedRotation();

        BlockData blockData = transitiveBlock.getBlockData().clone();

        if (rotation == 0)
            location = spawnLocation.clone().add(transitiveBlock.getRelativeLocation());
        else {
            location = spawnLocation.clone().add(transitiveBlock.getRelativeLocation().clone().rotateAroundY(Math.toRadians(rotation)));
            if (blockData instanceof Directional)
                ((Directional) blockData).setFacing(rotateBlockFace(((Directional) blockData).getFacing(), rotation));
        }

        //Minor optimization, does not replace blocks that are already identical
        if (!location.getBlock().getBlockData().getMaterial().equals(blockData.getMaterial()))
            location.getBlock().setBlockData(blockData, blockData.getMaterial() == Material.WATER || blockData.getMaterial() == Material.LAVA);

    }

    /*
    1 = north
    2 = east
    3 = south
    4 = west
     */
    private static int blockFaceToInt(BlockFace blockFace) {
        switch (blockFace) {
            case NORTH:
                return 1;
            case EAST:
                return 2;
            case SOUTH:
                return 3;
            case WEST:
                return 4;
            default:
                new WarningMessage("Attempted to rotate a block through the transitive block system that does not have a north / south / east / west face. This is not currently supported.");
                return 1;
        }
    }

    private static BlockFace intToBlockFace(int blockFaceValue) {
        switch (blockFaceValue) {
            case 1:
                return BlockFace.NORTH;
            case 2:
                return BlockFace.EAST;
            case 3:
                return BlockFace.SOUTH;
            case 4:
                return BlockFace.WEST;
            default:
                new WarningMessage("Attempted to rotate a block through the transitive block system that does not have a north / south / east / west face. This is not currently supported.");
                return BlockFace.NORTH;
        }
    }

    private static BlockFace rotateBlockFace(BlockFace blockFace, double rotation) {
        int adjustedRotation = (int) (rotation / 90d);
        int blockFaceInt = blockFaceToInt(blockFace);
        int result = blockFaceInt + adjustedRotation;
        if (result > 4) result -= 4;
        if (result < 1) result += 4;
        return intToBlockFace(result);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBossSpawn(EliteMobSpawnEvent event) {
        if (!(event.getEliteMobEntity() instanceof RegionalBossEntity regionalBossEntity)) return;
        if (regionalBossEntity.getOnSpawnTransitiveBlocks() != null && !regionalBossEntity.getOnSpawnTransitiveBlocks().isEmpty())
            for (TransitiveBlock transitiveBlock : regionalBossEntity.getOnSpawnTransitiveBlocks())
                setBlockData(regionalBossEntity, transitiveBlock, regionalBossEntity.getSpawnLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBossRemove(EliteMobRemoveEvent event) {
        if (!(event.getEliteMobEntity() instanceof RegionalBossEntity regionalBossEntity)) return;
        if (regionalBossEntity.getOnRemoveTransitiveBlocks() != null && !regionalBossEntity.getOnRemoveTransitiveBlocks().isEmpty())
            for (TransitiveBlock transitiveBlock : regionalBossEntity.getOnRemoveTransitiveBlocks())
                setBlockData(regionalBossEntity, transitiveBlock, regionalBossEntity.getSpawnLocation());
    }
}
