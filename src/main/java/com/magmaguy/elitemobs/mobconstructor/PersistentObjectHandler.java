package com.magmaguy.elitemobs.mobconstructor;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.InstancedDungeonRemoveEvent;
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
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PersistentObjectHandler {

    /*
    This ArrayListMultiMap keeps two types of string keys: The first type is a world name and the second type is a chunk vector converted to a string
     */
    private static final ArrayListMultimap<String, PersistentObjectHandler> persistentObjects = ArrayListMultimap.create();
    private final PersistentObject persistentObject;
    private final String worldName;
    @Getter
    private Location persistentLocation;
    private String chunk;

    /**
     * Used to store the locations of custom bosses that have gone into unloaded chunks.
     *
     * @param persistentObject
     */
    public PersistentObjectHandler(PersistentObject persistentObject) {
        this.persistentObject = persistentObject;
        this.persistentLocation = persistentObject.getPersistentLocation();
        this.worldName = persistentObject.getWorldName();
        if (persistentLocation != null &&
                persistentLocation.getWorld() != null &&
                Bukkit.getWorld(persistentLocation.getWorld().getUID()) != null)
            addChunkKey(this);
        else
            addWorldKey(this);
    }

    /**
     * Clears all data for a correct shutdown
     */
    public static void shutdown() {
        persistentObjects.clear();
    }

    /**
     * Used to add persistent entities to the list. This inserts an arbitrary amount of persistent entities into a chunk.
     * Called when a SimplePersistentEntity is created.
     *
     * @param simplePersistentEntity Entity to be added
     */
    private void addChunkKey(PersistentObjectHandler simplePersistentEntity) {
        if (persistentLocation.getWorld() == null) return;
        this.chunk = ChunkVectorizer.hash(persistentLocation.getBlockX() >> 4, persistentLocation.getBlockZ() >> 4, persistentLocation.getWorld().getUID()) + "";
        persistentObjects.put(simplePersistentEntity.chunk, simplePersistentEntity);
    }

    private void addWorldKey(PersistentObjectHandler persistentObjectHandler) {
        persistentObjects.put(persistentObjectHandler.worldName, persistentObjectHandler);
    }

    public void worldLoad(World world) {
        //run implementations
        persistentObject.worldLoad(world);
        //convert persistent object handler to chunk-based detection
        //Start by removing old key
        remove();
        //Assign world to the location
        this.persistentLocation.setWorld(world);
        //Assign key
        addChunkKey(this);

    }

    public void worldUnload() {
        //run implementations
        persistentObject.worldUnload();
        //convert persistent object handler to world-based detection
        //Start by removing old key
        remove();
        //Assign key
        addWorldKey(this);
    }

    public void updatePersistentLocation(Location location) {
        remove();
        this.persistentLocation = location;
        addChunkKey(this);
    }


    public void remove() {
        persistentObjects.remove(this.chunk, this);
        persistentObjects.remove(this.worldName, this);
    }

    public static class PersistentObjectHandlerEvents implements Listener {

        private static int chunkLocation(Chunk chunk) {
            return ChunkVectorizer.hash(chunk);
        }


        /**
         * Behavior that runs when a chunk loads, spawning the entity
         */
        private static void loadChunk(List<PersistentObjectHandler> persistentObjectHandlers) {
            persistentObjectHandlers.forEach(persistentObjectHandler -> persistentObjectHandler.persistentObject.chunkLoad());
        }

        private static void unloadChunk(List<PersistentObjectHandler> persistentObjectHandlers) {
            persistentObjectHandlers.forEach(persistentObjectHandler -> {
                //The chunk unload for moving entities is handled by the EntityTracker
                if (persistentObjectHandler.persistentObject instanceof PersistentMovingEntity) return;
                persistentObjectHandler.persistentObject.chunkUnload();
            });
        }

        private static void unloadWorld(World world) {
            List<PersistentObjectHandler> copy = new ArrayList<>();
            for (PersistentObjectHandler persistentObjectHandler : persistentObjects.values())
                if (Objects.equals(persistentObjectHandler.worldName, world.getName()))
                    copy.add(persistentObjectHandler);
            copy.forEach(PersistentObjectHandler::worldUnload);
        }

        private static void loadWorld(World world) {
            List<PersistentObjectHandler> copy = new ArrayList<>(persistentObjects.get(world.getName()));
            copy.forEach(persistentObjectHandler -> persistentObjectHandler.worldLoad(world));
        }

        //Store world names and serialized locations
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void chunkLoadEvent(ChunkLoadEvent event) {
            int chunkLocation = chunkLocation(event.getChunk());
            List<PersistentObjectHandler> simplePersistentEntityList = new ArrayList<>(persistentObjects.get(chunkLocation + ""));
            Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> loadChunk(simplePersistentEntityList), 1L);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
        public void worldUnloadEvent(WorldUnloadEvent event) {
            unloadWorld(event.getWorld());
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void worldLoadEvent(WorldLoadEvent event) {
            loadWorld(event.getWorld());
        }

        @EventHandler
        public void chunkUnloadEvent(ChunkUnloadEvent event) {
            int chunkLocation = chunkLocation(event.getChunk());
            List<PersistentObjectHandler> simplePersistentEntityList = new ArrayList<>(persistentObjects.get(chunkLocation + ""));
            unloadChunk(simplePersistentEntityList);
        }

        @EventHandler
        public void onInstanceRemove(InstancedDungeonRemoveEvent event){
            persistentObjects.removeAll(event.getDungeonInstance().getInstancedWorldName());
        }

    }
}
