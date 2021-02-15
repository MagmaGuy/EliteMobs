package com.magmaguy.elitemobs.mobconstructor;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.ChunkVectorizer;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

public class SimplePersistentEntity {

    //Values are stored for the chunk load events
    public static ArrayListMultimap<Integer, SimplePersistentEntity> persistentEntities = ArrayListMultimap.create();

    /**
     * Used to add persistent entities to the list. This inserts an arbitrary amount of persistent entities into a chunk.
     * Called when a SimplePersistentEntity is created.
     *
     * @param simplePersistentEntity
     */
    private static void addPersistentEntity(SimplePersistentEntity simplePersistentEntity) {
        persistentEntities.put(simplePersistentEntity.chunk, simplePersistentEntity);
    }

    public int chunk;
    public Location location;
    public CustomBossEntity customBossEntity;
    public UUID uuid;

    /**
     * Used to store the locations of custom bosses that have gone into unloaded chunks.
     *
     * @param customBossEntity
     */
    public SimplePersistentEntity(CustomBossEntity customBossEntity) {
        this.customBossEntity = customBossEntity;
        this.location = customBossEntity.getLocation();
        this.chunk = chunkLocation(location.getChunk());
        this.uuid = customBossEntity.uuid;
        addPersistentEntity(this);
    }

    public NPCEntity npcEntity;

    public SimplePersistentEntity(NPCEntity npcEntity) {
        this.npcEntity = npcEntity;
        this.location = npcEntity.getVillager().getLocation();
        this.chunk = chunkLocation(location.getChunk());
        this.uuid = npcEntity.uuid;
        addPersistentEntity(this);
    }

    private static int chunkLocation(Chunk chunk) {
        return ChunkVectorizer.hash(chunk);
    }

    public static class PersistentEntityEvent implements Listener {
        public static boolean ignore = false;

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
        public void worldUnloadEvent(WorldUnloadEvent event){
            unloadWorld(event.getWorld());
        }
    }

    private static final HashSet<Integer> chunkLocations = new HashSet<>();

    //Something is causing a weird double chunk load issue... not sure what but it's also probably causing a CME
    private static void loadChunk(int chunkLocation) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    for (SimplePersistentEntity simplePersistentEntity : persistentEntities.get(chunkLocation)) {
                        if (simplePersistentEntity.customBossEntity != null)
                            simplePersistentEntity.customBossEntity.chunkLoad();
                        else if (simplePersistentEntity.npcEntity != null)
                            simplePersistentEntity.npcEntity.chunkLoad();
                    }
                } catch (Exception ex) {
                    new DeveloperMessage("CME Fired!");
                }
                persistentEntities.removeAll(chunkLocation);
                chunkLocations.remove(chunkLocation);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }

    private static void unloadWorld(World world){
        persistentEntities.values().removeIf(simplePersistentEntity -> simplePersistentEntity.location.getWorld().equals(world));
    }

    public void remove(){
        persistentEntities.remove(this.chunk, this);
    }
}
