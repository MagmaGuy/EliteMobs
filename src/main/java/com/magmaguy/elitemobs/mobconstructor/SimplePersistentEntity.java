package com.magmaguy.elitemobs.mobconstructor;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.utils.ChunkVectorizer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.HashSet;

public class SimplePersistentEntity {

    private static final HashSet<Integer> chunkLocations = new HashSet<>();
    //Values are stored for the chunk load events
    public static ArrayListMultimap<Integer, SimplePersistentEntity> persistentEntities = ArrayListMultimap.create();
    public static ArrayListMultimap<String, SimplePersistentEntity> persistentEntitiesForQueuedWorlds = ArrayListMultimap.create();
    public int chunk;
    public Location location;
    public CustomBossEntity customBossEntity;
    public String worldName;
    public NPCEntity npcEntity;

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
        } else addPersistentEntityForWorld(this);
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
     * @param simplePersistentEntity
     */
    private static void addPersistentEntity(SimplePersistentEntity simplePersistentEntity) {
        persistentEntities.put(simplePersistentEntity.chunk, simplePersistentEntity);
    }

    private static void addPersistentEntityForWorld(SimplePersistentEntity simplePersistentEntity) {
        persistentEntitiesForQueuedWorlds.put(simplePersistentEntity.worldName, simplePersistentEntity);
    }

    private static int chunkLocation(Chunk chunk) {
        return ChunkVectorizer.hash(chunk);
    }

    /**
     * Behavior that runs when a chunk loads, spawning the entity
     *
     * @param chunkLocation
     */
    private static void loadChunk(int chunkLocation) {
        persistentEntities.get(chunkLocation).forEach((simplePersistentEntity) -> {
            if (simplePersistentEntity.customBossEntity != null)
                simplePersistentEntity.customBossEntity.chunkLoad();
            else if (simplePersistentEntity.npcEntity != null)
                simplePersistentEntity.npcEntity.chunkLoad();
        });
        persistentEntities.removeAll(chunkLocation);
        chunkLocations.remove(chunkLocation);
    }

    private static void unloadWorld(World world) {
        persistentEntities.values().removeIf(simplePersistentEntity -> simplePersistentEntity.location.getWorld().equals(world));
    }

    public void remove() {
        persistentEntities.remove(this.chunk, this);
    }

    public static class PersistentEntityEvent implements Listener {
        public static boolean ignore = false;

        @EventHandler(ignoreCancelled = true)
        public void worldLoadEvent(WorldLoadEvent event) {
            if (persistentEntitiesForQueuedWorlds.get(event.getWorld().getName()) == null) return;
            for (SimplePersistentEntity simplePersistentEntity : persistentEntitiesForQueuedWorlds.get(event.getWorld().getName())) {
                if (simplePersistentEntity.customBossEntity != null)
                    simplePersistentEntity.customBossEntity.worldLoad();
                else if (simplePersistentEntity.npcEntity != null) {
                    simplePersistentEntity.npcEntity.setSpawnLocation();
                    simplePersistentEntity.npcEntity.queueSpawn();
                }
            }
        }

        //Store world names and serialized locations
        @EventHandler(ignoreCancelled = true)
        public void chunkLoadEvent(ChunkLoadEvent event) {
            if (ignore) return;
            int chunkLocation = chunkLocation(event.getChunk());
            if (persistentEntities.get(chunkLocation) == null) return;
            if (chunkLocations.contains(chunkLocation)) return;
            chunkLocations.add(chunkLocation);
            loadChunk(chunkLocation);
        }

        @EventHandler(ignoreCancelled = true)
        public void worldUnloadEvent(WorldUnloadEvent event) {
            unloadWorld(event.getWorld());
        }
    }
}
