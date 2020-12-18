package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;


/**
 * Created by MagmaGuy on 20/12/2016.
 */
public class ChunkUnloadMetadataPurge implements Listener {

    @EventHandler
    public void onUnload(ChunkUnloadEvent event) {
        EntityTracker.chunkWiper(event);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        EntityTracker.chunkWiper(event);
    }

}
