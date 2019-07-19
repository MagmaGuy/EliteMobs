package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.EntityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class NPCChunkLoad implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

        if (EntityTracker.getNPCEntities().isEmpty()) return;

        for (NPCEntity npcEntity : EntityTracker.getNPCEntities())
            if (event.getChunk().equals(npcEntity.getSpawnLocation().getChunk()))
                npcEntity.respawnNPC();

    }

}
