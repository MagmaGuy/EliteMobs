package com.magmaguy.elitemobs.mobpowers.miscellaneouspowers;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class Corpse extends MinorPower implements Listener {

    @Override
    public void applyPowers(Entity entity) {

    }

    private static HashSet<Block> blocks = new HashSet<>();

    public static void clearBlocks() {
        for (Block block : blocks)
            if (block.getType().equals(Material.BONE_BLOCK))
                block.setType(Material.AIR);

        blocks.clear();
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteMobEntity == null) return;

        if (!eliteMobEntity.hasPower(this)) return;

        if (!event.getEntity().getLocation().getBlock().getType().equals(Material.AIR)) return;

        blocks.add(event.getEntity().getLocation().getBlock());
        event.getEntity().getLocation().getBlock().setType(Material.BONE_BLOCK);

        new BukkitRunnable() {
            @Override
            public void run() {
                blocks.remove(event.getEntity().getLocation().getBlock());
                if (event.getEntity().getLocation().getBlock().getType().equals(Material.BONE_BLOCK))
                    event.getEntity().getLocation().getBlock().setType(Material.AIR);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 60 * 2);

    }

    @EventHandler
    public void onMine(BlockBreakEvent event) {

        if (blocks.contains(event.getBlock()))
            event.setDropItems(false);

    }

}
