package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.EntityTracker;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class NPCChunkLoad implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

        if (EntityTracker.getNPCEntities().isEmpty()) return;

        Location location = event.getChunk().getBlock(8, 0, 8).getLocation();

        for (NPCEntity npcEntity : EntityTracker.getNPCEntities()) {

            if (!event.getWorld().equals(npcEntity.getSpawnLocation().getWorld())) continue;

            Location npcLocation = npcEntity.getSpawnLocation().clone();
            npcLocation.setY(0);

            if (npcLocation.distance(location) < 8)
                npcEntity.respawnNPC();

        }

    }

}
