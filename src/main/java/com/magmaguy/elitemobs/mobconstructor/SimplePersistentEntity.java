package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.utils.ChunkVectorizer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class SimplePersistentEntity {

    //Values are stored for the chunk load events
    public static HashMap<Integer, ArrayList<SimplePersistentEntity>> persistentEntities = new HashMap<>();

    /**
     * Used to add persistent entities to the list. This inserts an arbitrary amount of persistent entities into a chunk.
     * Called when a SimplePersistentEntity is created.
     *
     * @param simplePersistentEntity
     */
    private static void addPersistentEntity(SimplePersistentEntity simplePersistentEntity) {
        ArrayList<SimplePersistentEntity> persistentArray = persistentEntities.get(simplePersistentEntity.chunk);
        if (persistentArray == null)
            persistentEntities.put(simplePersistentEntity.chunk, new ArrayList<>(Collections.singletonList(simplePersistentEntity)));
        else {
            persistentArray.add(simplePersistentEntity);
            persistentEntities.put(simplePersistentEntity.chunk, persistentArray);
        }
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

    /**
     * Removes a persistent entity from the list. Persistent entities are culled when their chunk loads, or when the custom
     * boss associated to them is times out while the chunk is unloaded.
     * Note: This method is only called when the entity custom boss associated to the persistent entity times out or is
     * otherwise removed while in an unloaded chunk. For chunk loads, remove the entire key value from the map, as everything
     * in it should get loaded and then deleted.
     */
    public void remove() {
        ArrayList<SimplePersistentEntity> persistentArray = persistentEntities.get(chunk);
        //this happens in chunk unloads, it mass clears entries. It's already handled elsewhere.
        if (persistentArray == null) return;
        persistentArray.remove(this);
        persistentEntities.put(chunk, persistentArray);
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
            ArrayList<SimplePersistentEntity> arrayList = persistentEntities.get(chunkLocation(event.getChunk()));
            if (arrayList == null) return;
            for (SimplePersistentEntity simplePersistentEntity : arrayList)
                if (simplePersistentEntity.customBossEntity != null)
                    simplePersistentEntity.customBossEntity.chunkLoad();
                else if (simplePersistentEntity.npcEntity != null)
                    simplePersistentEntity.npcEntity.chunkLoad();
            persistentEntities.remove(chunkLocation(event.getChunk()));
        }
    }

}
