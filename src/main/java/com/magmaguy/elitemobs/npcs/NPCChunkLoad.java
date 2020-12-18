package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.utils.ChunkLocationChecker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class NPCChunkLoad implements Listener {
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (EntityTracker.getNPCEntities().isEmpty()) return;
        for (NPCEntity npcEntity : NPCEntity.getNPCEntityList())
            if (ChunkLocationChecker.chunkLocationCheck(npcEntity.getSpawnLocation(), event.getChunk()))
                npcEntity.respawnNPC();
    }
}
