package com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks;

import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TransitiveBossBlock implements Listener {

    private static Location getRealLocation(TransitiveBlock transitiveBlock, Location spawnLocation) {
        return new Location(spawnLocation.getWorld(),
                spawnLocation.getX() + transitiveBlock.getRelativeLocation().getX(),
                spawnLocation.getY() + transitiveBlock.getRelativeLocation().getY(),
                spawnLocation.getZ() + transitiveBlock.getRelativeLocation().getZ());
    }

    @EventHandler
    public void onBossSpawn(EliteMobSpawnEvent event) {
        if (!(event.getEliteMobEntity() instanceof RegionalBossEntity)) return;

        RegionalBossEntity regionalBossEntity = (RegionalBossEntity) event.getEliteMobEntity();
        if (regionalBossEntity.getOnSpawnTransitiveBlocks() != null){
            for (TransitiveBlock transitiveBlock : regionalBossEntity.getOnSpawnTransitiveBlocks()){
                getRealLocation(transitiveBlock, regionalBossEntity.getSpawnLocation()).getBlock().setBlockData(transitiveBlock.getBlockData());}}
    }

    @EventHandler
    public void onBossRemove(EliteMobRemoveEvent event) {
        if (!(event.getEliteMobEntity() instanceof RegionalBossEntity)) return;
        RegionalBossEntity regionalBossEntity = (RegionalBossEntity) event.getEliteMobEntity();
        if (regionalBossEntity.getOnRemoveTransitiveBlocks() != null){
            for (TransitiveBlock transitiveBlock : regionalBossEntity.getOnRemoveTransitiveBlocks())
                getRealLocation(transitiveBlock, regionalBossEntity.getSpawnLocation()).getBlock().setBlockData(transitiveBlock.getBlockData());}
    }
}
