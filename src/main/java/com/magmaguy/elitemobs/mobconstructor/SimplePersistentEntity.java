package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
        ArrayList<SimplePersistentEntity> persistentArray = persistentEntities.get(simplePersistentEntity.chunkHashCode);
        if (persistentArray == null)
            persistentEntities.put(simplePersistentEntity.chunkHashCode, new ArrayList<>(Arrays.asList(simplePersistentEntity)));
        else {
            persistentArray.add(simplePersistentEntity);
            persistentEntities.put(simplePersistentEntity.chunkHashCode, persistentArray);
        }
    }

    int chunkHashCode;
    public CustomBossEntity customBossEntity;
    public Location location;

    /**
     * Used to store the locations of custom bosses that have gone into unloaded chunks.
     *
     * @param customBossEntity
     */
    public SimplePersistentEntity(CustomBossEntity customBossEntity) {
        this.customBossEntity = customBossEntity;
        this.location = customBossEntity.getLivingEntity().getLocation();
        this.chunkHashCode = chunkLocation(location.getChunk());
        addPersistentEntity(this);
    }

    public NPCEntity npcEntity;

    public SimplePersistentEntity(NPCEntity npcEntity) {
        this.npcEntity = npcEntity;
        this.location = npcEntity.getVillager().getLocation();
        this.chunkHashCode = chunkLocation(location.getChunk());
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
        ArrayList<SimplePersistentEntity> persistentArray = persistentEntities.get(chunkHashCode);
        //this happens in chunk unloads, it mass clears entries. It's already handled elsewhere.
        if (persistentArray == null) return;
        persistentArray.remove(this);
        persistentEntities.put(chunkHashCode, persistentArray);
    }

    private static int chunkLocation(Chunk chunk) {
        return (new Location(chunk.getWorld(),
                chunk.getX(),
                0,
                chunk.getZ()))
                .hashCode();
    }

    public static class PersistentEntityEvent implements Listener {
        //Store world names and serialized locations

        @EventHandler
        public void chunkLoadEvent(ChunkLoadEvent event) {
            ArrayList<SimplePersistentEntity> arrayList = persistentEntities.get(chunkLocation(event.getChunk()));
            if (arrayList == null) return;
            arrayList = (ArrayList<SimplePersistentEntity>) arrayList.clone();
            for (SimplePersistentEntity simplePersistentEntity : arrayList) {
                if (simplePersistentEntity.customBossEntity != null)
                    simplePersistentEntity.customBossEntity.chunkLoad();
                if (simplePersistentEntity.npcEntity != null)
                    simplePersistentEntity.npcEntity.chunkLoad();
            }
            persistentEntities.remove(chunkLocation(event.getChunk()));
        }

    }

}
