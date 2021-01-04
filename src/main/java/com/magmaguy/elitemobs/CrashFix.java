package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.utils.ChunkVectorizer;
import com.magmaguy.elitemobs.utils.PersistentVanillaData;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.HashSet;

public class CrashFix implements Listener {

    public static String key = "EliteMobsCullable";

    public static void persistentTracker(Entity entity) {
        PersistentVanillaData.write(entity, key, "delete_me");
    }

    public static boolean isPersistentEntity(Entity entity) {
        return PersistentVanillaData.hasString(entity, key);
    }

    public static HashSet<Integer> knownSessionChunks = new HashSet<>();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        int hashedChunk = ChunkVectorizer.hash(event.getChunk());
        if (knownSessionChunks.contains(hashedChunk)) return;
        knownSessionChunks.add(hashedChunk);
        chunkCheck(event.getChunk());
    }

    public static void startupCheck() {
        for (World world : Bukkit.getWorlds())
            for (Chunk chunk : world.getLoadedChunks()) {
                chunkCheck(chunk);
                knownSessionChunks.add(ChunkVectorizer.hash(chunk));
            }
    }

    private static void chunkCheck(Chunk chunk) {
        for (Entity entity : chunk.getEntities())
            if (isPersistentEntity(entity))
                entity.remove();
    }

}
