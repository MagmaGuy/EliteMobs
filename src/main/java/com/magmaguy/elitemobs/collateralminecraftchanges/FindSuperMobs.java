package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class FindSuperMobs implements Listener {

    @EventHandler
    public void findSuperMob(ChunkLoadEvent event) {

        for (Entity entity : event.getChunk().getEntities())
            if (SuperMobProperties.isValidSuperMobType(entity)) {
            }

    }

}
