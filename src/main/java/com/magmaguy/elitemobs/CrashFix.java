package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.utils.ChunkVectorizer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Vector;

public class CrashFix implements Listener {

    public static final NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, "EliteMobsCullable");

    public static void persistentTracker(Entity entity) {
        entity.getPersistentDataContainer().set(key, PersistentDataType.STRING, "delete_me");
    }

    public static void persistentUntracker(Entity entity) {
        entity.getPersistentDataContainer().remove(key);
    }

    public static boolean isPersistentEntity(Entity entity) {
        return entity.getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

    private static HashSet<Vector> knownSessionChunks = new HashSet<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (knownSessionChunks.contains(ChunkVectorizer.vectorize(event.getChunk()))) return;
        knownSessionChunks.add(ChunkVectorizer.vectorize(event.getChunk()));
        chunkCheck(event.getChunk());
    }

    public static void startupCheck() {
        for (World world : Bukkit.getWorlds())
            for (Chunk chunk : world.getLoadedChunks()) {
                chunkCheck(chunk);
                knownSessionChunks.add(ChunkVectorizer.vectorize(chunk));
            }
    }

    private static void chunkCheck(Chunk chunk) {
        for (Entity entity : chunk.getEntities())
            if (isPersistentEntity(entity)) {
                entity.remove();
                persistentUntracker(entity);
            }
    }

}
