package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.UUID;

public class NPCEntityTracker extends TrackedEntity implements AbstractTrackedEntity {

    public static HashMap<UUID, NPCEntity> npcEntities = new HashMap<>();

    public NPCEntity npcEntity;

    public NPCEntityTracker(UUID uuid, NPCEntity npcEntity) {
        super(uuid, npcEntity.getVillager(), false, true, npcEntities);
        this.npcEntity = npcEntity;
        npcEntities.put(uuid, npcEntity);
        npcEntity.getVillager().setMetadata(MetadataHandler.NPC_METADATA, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
    }

    @Override
    public void specificRemoveHandling(RemovalReason removalReason) {
        npcEntity.getVillager().removeMetadata(MetadataHandler.NPC_METADATA, MetadataHandler.PLUGIN);
        if (removalReason.equals(RemovalReason.REMOVE_COMMAND)) {
            npcEntity.deleteNPCEntity();
            return;
        }
        if (removalReason.equals(RemovalReason.WORLD_UNLOAD)){
            npcEntity.removeNPCEntity();
            return;
        }
        if (!removalReason.equals(RemovalReason.SHUTDOWN))
            npcEntity.chunkUnload();
    }

}
