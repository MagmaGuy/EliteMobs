package com.magmaguy.elitemobs.mobconstructor;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.utils.ChunkVectorizer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SimplePersistentEntity {

    //Values are stored for the chunk load events
    @Getter
    private static final ArrayListMultimap<Integer, SimplePersistentEntity> persistentEntities = ArrayListMultimap.create();
    private static final ArrayListMultimap<String, SimplePersistentEntity> persistentEntitiesForQueuedWorlds = ArrayListMultimap.create();
    private final Location location;
    private int chunk;
    private CustomBossEntity customBossEntity;
    private String worldName;
    private NPCEntity npcEntity;

    /**
     * Used to store the locations of custom bosses that have gone into unloaded chunks.
     *
     * @param customBossEntity
     */
    public SimplePersistentEntity(CustomBossEntity customBossEntity, Location location) {
        this.customBossEntity = customBossEntity;
        this.location = location;
        if (location == null) location = customBossEntity.getSpawnLocation();
        this.worldName = customBossEntity.getWorldName();
        if (location.getWorld() != null) {
            this.chunk = ChunkVectorizer.hash(
                    location.getBlockX() >> 4,
                    location.getBlockZ() >> 4,
                    location.getWorld().getUID());
            addPersistentEntity(this);
        } else {
            addPersistentEntityForWorld(this);
        }
    }

    public SimplePersistentEntity(NPCEntity npcEntity) {
        this.npcEntity = npcEntity;
        this.location = npcEntity.getSpawnLocation();
        if (location != null && location.getWorld() != null) {
            this.chunk = ChunkVectorizer.hash(
                    location.getBlockX() >> 4,
                    location.getBlockZ() >> 4,
                    location.getWorld().getUID());
            addPersistentEntity(this);
        } else {
            this.worldName = npcEntity.getNpCsConfigFields().getLocation().split(",")[0];
            addPersistentEntityForWorld(this);
        }
    }

    /**
     * Used to add persistent entities to the list. This inserts an arbitrary amount of persistent entities into a chunk.
     * Called when a SimplePersistentEntity is created.
     *
     * @param simplePersistentEntity Entity to be added
     */
    private static void addPersistentEntity(SimplePersistentEntity simplePersistentEntity) {
        persistentEntities.put(simplePersistentEntity.chunk, simplePersistentEntity);
    }

    private static void addPersistentEntityForWorld(SimplePersistentEntity simplePersistentEntity) {
        persistentEntitiesForQueuedWorlds.put(simplePersistentEntity.worldName, simplePersistentEntity);
    }

    public void remove() {
        persistentEntities.remove(this.chunk, this);
    }

    public static class PersistentEntityEvent implements Listener {
        private static final HashSet<Integer> loadingChunks = new HashSet<>();

        private static int chunkLocation(Chunk chunk) {
            return ChunkVectorizer.hash(chunk);
        }

        //Store world names and serialized locations
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void chunkLoadEvent(ChunkLoadEvent event) {
            int chunkLocation = chunkLocation(event.getChunk());
            List<SimplePersistentEntity> simplePersistentEntityList = new ArrayList<>(persistentEntities.get(chunkLocation));
            if (loadingChunks.contains(chunkLocation)) return;
            loadingChunks.add(chunkLocation);
            Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> loadChunk(chunkLocation, simplePersistentEntityList), 1);
            Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> loadingChunks.remove(chunkLocation), 1);
        }

        @EventHandler(ignoreCancelled = true)
        public void worldUnloadEvent(WorldUnloadEvent event) {
            unloadWorld(event.getWorld());
        }

        /**
         * Behavior that runs when a chunk loads, spawning the entity
         *
         * @param chunkLocation
         */
        private static void loadChunk(int chunkLocation, List<SimplePersistentEntity> simplePersistentEntityList) {
            simplePersistentEntityList.forEach(simplePersistentEntity -> {
                if (simplePersistentEntity.customBossEntity != null)
                    simplePersistentEntity.customBossEntity.chunkLoad();
                else if (simplePersistentEntity.npcEntity != null)
                    simplePersistentEntity.npcEntity.chunkLoad();
            });
            persistentEntities.removeAll(chunkLocation);
        }

        private static void unloadWorld(World world) {
            persistentEntities.values().removeIf(simplePersistentEntity -> {
                if (simplePersistentEntity.location == null) return false;
                if (simplePersistentEntity.location.getWorld() == null) return false;
                if (!simplePersistentEntity.location.getWorld().equals(world)) return false;
                if (simplePersistentEntity.customBossEntity != null)
                    simplePersistentEntity.customBossEntity.worldUnload();
                else if (simplePersistentEntity.npcEntity != null)
                    simplePersistentEntity.npcEntity.worldUnload();
                return true;
            });
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void worldLoadEvent(WorldLoadEvent event) {
            Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, task -> {
                for (SimplePersistentEntity simplePersistentEntity : persistentEntitiesForQueuedWorlds.get(event.getWorld().getName()))
                    if (simplePersistentEntity.customBossEntity != null)
                        simplePersistentEntity.customBossEntity.worldLoad();
                    else if (simplePersistentEntity.npcEntity != null)
                        simplePersistentEntity.npcEntity.worldLoad();
                persistentEntitiesForQueuedWorlds.removeAll(event.getWorld().getName());
            }, 20);
        }
    }
}
