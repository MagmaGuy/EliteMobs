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
import org.bukkit.scheduler.BukkitRunnable;

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
    private static final HashSet<Integer> temporarilyCachedChunks = new HashSet<>();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        int hashedChunk = ChunkVectorizer.hash(event.getChunk());
        //For some reason there is double chunk loading going on, and entities aren't getting detected correctly
        if (temporarilyCachedChunks.contains(hashedChunk)) return;
        temporarilyCachedChunks.add(hashedChunk);
        if (knownSessionChunks.contains(hashedChunk)) return;
        knownSessionChunks.add(hashedChunk);
        laterChunkLoad(event.getChunk());
    }

    //Hopefully this scans for entities properly by giving the chunk a tick to load
    private void laterChunkLoad(Chunk chunk) {
        new BukkitRunnable() {
            @Override
            public void run() {
                chunkCheck(chunk);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);
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
