package com.magmaguy.elitemobs.mobconstructor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.InstancedDungeonRemoveEvent;
import com.magmaguy.elitemobs.utils.ChunkVectorizer;
import com.magmaguy.magmacore.util.Logger;
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
import java.util.UUID;

public class PersistentObjectHandler {

    /*
    This ListMultimap keeps two types of string keys: The first type is a world name and the second type is a chunk vector converted to a string
    Handlers are created from the async content-package initialization thread as well as from the main thread (chunk
    and world events, boss spawns), so the backing multimap has to be synchronized. Guava only guards single
    operations; every iteration over a collection view below additionally locks on the multimap itself, which is the
    lock Multimaps#synchronizedListMultimap uses internally.
     */
    private static final ListMultimap<String, PersistentObjectHandler> persistentObjects =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
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
     * <p>
     * Falls back to the world key when there is nothing to hash a chunk from. Returning without adding any key at all
     * orphans the handler: it stops being reachable from chunk loads, world loads and world unloads alike, so the
     * persistent object it guards can never come back and can never be cleaned up.
     *
     * @param simplePersistentEntity Entity to be added
     */
    private void addChunkKey(PersistentObjectHandler simplePersistentEntity) {
        if (persistentLocation == null || persistentLocation.getWorld() == null) {
            addWorldKey(simplePersistentEntity);
            return;
        }
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
        //Assign world to the location. The handler can be constructed before the persistent object knows where it
        //lives (regional bosses parsed while their world is still unloaded), so the location may only materialize
        //once the implementation above has run - ask for it again rather than dereferencing a null field.
        if (this.persistentLocation == null)
            this.persistentLocation = persistentObject.getPersistentLocation();
        if (this.persistentLocation != null)
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

    /**
     * Removes every handler whose stored location lives in the given world. Instanced dungeon chests
     * (and any other persistent object inside an instanced world) get keyed by chunk hash while the
     * world is loaded, so neither the instanced-world-name removal nor the WorldUnloadEvent sweep
     * (which both key off the base blueprint world name) can reach them. Without this, the handler
     * keeps its persistentLocation -> instanced World -> ServerLevel alive after the dungeon closes.
     */
    public static void removeForWorld(UUID worldUUID) {
        if (worldUUID == null) return;
        List<PersistentObjectHandler> copy = snapshotAll();
        for (PersistentObjectHandler handler : copy) {
            Location handlerLocation = handler.persistentLocation;
            if (handlerLocation != null &&
                    handlerLocation.getWorld() != null &&
                    handlerLocation.getWorld().getUID().equals(worldUUID))
                handler.remove();
        }
    }

    /**
     * Guava's synchronized multimap only locks individual operations - copying a collection view still iterates it, so
     * the copy has to happen while holding the multimap's own lock.
     */
    private static List<PersistentObjectHandler> snapshot(String key) {
        synchronized (persistentObjects) {
            return new ArrayList<>(persistentObjects.get(key));
        }
    }

    private static List<PersistentObjectHandler> snapshotAll() {
        synchronized (persistentObjects) {
            return new ArrayList<>(persistentObjects.values());
        }
    }

    /**
     * Drains every handler that is currently filed under a world name and hands it back to chunk-based tracking.
     * <p>
     * World-less persistent objects are filed under their world name and, until this was made callable, the only thing
     * that ever drained that key was {@link WorldLoadEvent}. That is not enough: worlds loaded by
     * {@code TemporaryWorldManager} short-circuit and return the existing {@link World} when it is already loaded,
     * which fires no event at all. Calling this directly from the dungeon world-load path makes the drain independent
     * of the event. It is safe to call more than once for the same world - a drained handler is re-keyed by chunk hash,
     * so a second pass finds an empty bucket - and a handler that failed to acquire a chunk key stays under the world
     * key precisely so the next pass can retry it.
     */
    public static void loadWorld(World world) {
        if (world == null) return;
        for (PersistentObjectHandler persistentObjectHandler : snapshot(world.getName()))
            try {
                persistentObjectHandler.worldLoad(world);
            } catch (Exception exception) {
                //One broken persistent object must not strand every handler still queued behind it in the drain
                Logger.warn("Failed to restore a persistent object in world " + world.getName() + " : " + exception.getMessage());
                exception.printStackTrace();
            }
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
            for (PersistentObjectHandler persistentObjectHandler : snapshotAll())
                if (Objects.equals(persistentObjectHandler.worldName, world.getName()))
                    copy.add(persistentObjectHandler);
            copy.forEach(PersistentObjectHandler::worldUnload);
        }

        //Store world names and serialized locations
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void chunkLoadEvent(ChunkLoadEvent event) {
            int chunkLocation = chunkLocation(event.getChunk());
            List<PersistentObjectHandler> simplePersistentEntityList = snapshot(chunkLocation + "");
            Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> loadChunk(simplePersistentEntityList), 1L);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
        public void worldUnloadEvent(WorldUnloadEvent event) {
            unloadWorld(event.getWorld());
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void worldLoadEvent(WorldLoadEvent event) {
            PersistentObjectHandler.loadWorld(event.getWorld());
        }

        @EventHandler (priority = EventPriority.LOWEST)
        public void chunkUnloadEvent(ChunkUnloadEvent event) {
            int chunkLocation = chunkLocation(event.getChunk());
            List<PersistentObjectHandler> simplePersistentEntityList = snapshot(chunkLocation + "");
            unloadChunk(simplePersistentEntityList);
        }

        @EventHandler
        public void onInstanceRemove(InstancedDungeonRemoveEvent event) {
            persistentObjects.removeAll(event.getDungeonInstance().getInstancedWorldName());
        }

    }
}
