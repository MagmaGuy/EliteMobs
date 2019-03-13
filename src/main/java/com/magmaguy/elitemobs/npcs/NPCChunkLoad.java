package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.EntityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class NPCChunkLoad implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (NPCEntity npcEntity : EntityTracker.getNPCEntities())
            if (npcEntity.getSpawnLocation().getChunk().equals(event.getChunk()))
                npcEntity.respawnNPC();
    }

}
