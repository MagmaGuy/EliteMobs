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
        delayedChunkCheck(event.getChunk(), hashedChunk);
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

    /**
     * This runs during startup. There is currently an issue with entities not getting scanned correctly when the chunk
     * loads, as well as an issue with double chunk loading. Additionally, since all elites are initialized during startup,
     * delaying the chunk by 1 tick causes the valid entities to be deleted as soon as they load in. To fix this, entities
     * that exist in a chunk when it is loaded are instead cached, and the chunk getting loaded is cached as well. The
     * chunk cache prevents a double check from happening, and entity cache prevents the plugin from scanning entities
     * other than the ones that persisted through a restart.
     *
     * @param chunk
     */
    private static void delayedChunkCheck(Chunk chunk, int hashedChunk) {
        Entity[] entities = chunk.getEntities().clone();
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : entities)
                    if (isPersistentEntity(entity))
                        entity.remove();
                temporarilyCachedChunks.remove(hashedChunk);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }

}
