package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.utils.DebugMessage;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.UUID;

public class NPCEntityTracker extends TrackedEntity implements AbstractTrackedEntity {

    public static HashMap<UUID, NPCEntity> npcEntities = new HashMap<>();

    public UUID uuid;
    public NPCEntity npcEntity;

    public NPCEntityTracker(UUID uuid, NPCEntity npcEntity) {
        super(uuid, npcEntity.getVillager(), false, true, npcEntities);
        this.uuid = uuid;
        this.npcEntity = npcEntity;
        npcEntities.put(uuid, npcEntity);
        npcEntity.getVillager().setMetadata(MetadataHandler.NPC_METADATA, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        new DebugMessage("Registering NPC Spawn! " + uuid);
    }

    @Override
    public void specificRemoveHandling(RemovalReason removalReason) {
        npcEntity.getVillager().removeMetadata(MetadataHandler.NPC_METADATA, MetadataHandler.PLUGIN);
        new DebugMessage("Unregistering NPC spawn due to " + removalReason.toString());
        if (!removalReason.equals(RemovalReason.SHUTDOWN))
            npcEntity.chunkUnload();
    }

}
