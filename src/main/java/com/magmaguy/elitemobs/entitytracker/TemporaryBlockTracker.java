package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class TemporaryBlockTracker {

    public static final HashSet<Block> temporaryBlocks = new HashSet<>();

    //Temporary blocks - blocks in powers
    public static void addTemporaryBlock(Block block, int ticks, Material replacementMaterial) {
        temporaryBlocks.add(block);
        block.setType(replacementMaterial);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (block.getType().equals(replacementMaterial))
                    block.setType(Material.AIR);
                temporaryBlocks.remove(block);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, ticks);
    }

}
