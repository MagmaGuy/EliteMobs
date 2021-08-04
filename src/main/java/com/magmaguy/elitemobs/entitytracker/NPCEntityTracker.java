package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.npcs.NPCEntity;

import java.util.HashMap;
import java.util.UUID;

public class NPCEntityTracker extends TrackedEntity implements AbstractTrackedEntity {

    public static HashMap<UUID, NPCEntity> npcEntities = new HashMap<>();

    public NPCEntity npcEntity;

    public NPCEntityTracker(UUID uuid, NPCEntity npcEntity) {
        super(uuid, npcEntity.getVillager(), false, true, npcEntities);
        this.npcEntity = npcEntity;
        npcEntities.put(uuid, npcEntity);
    }

    @Override
    public void specificRemoveHandling(RemovalReason removalReason) {
        npcEntity.remove(removalReason);
    }

}
