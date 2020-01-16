package com.magmaguy.elitemobs;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;


public class CrashFix implements Listener {

    public static final NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, "EliteMobsCullable");

    public static void persistentTracker(Entity entity) {
        entity.getPersistentDataContainer().set(key, PersistentDataType.STRING, "delete_me");
    }

    public static void persistentUntracker(Entity entity) {
        entity.getPersistentDataContainer().remove(key);
    }

    private HashSet<Chunk> knownSessionChunks = new HashSet<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (knownSessionChunks.contains(event.getChunk())) return;
        knownSessionChunks.add(event.getChunk());
        for (Entity entity : event.getChunk().getEntities())
            if (entity.getPersistentDataContainer().has(key, PersistentDataType.STRING))
                entity.remove();
    }

}
