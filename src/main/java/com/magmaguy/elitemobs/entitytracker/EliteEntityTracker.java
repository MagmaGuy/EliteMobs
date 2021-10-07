package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;

import java.util.HashMap;
import java.util.UUID;

public class EliteEntityTracker extends TrackedEntity implements AbstractTrackedEntity {

    public static HashMap<UUID, EliteEntity> eliteMobEntities = new HashMap<>();

    public EliteEntity eliteEntity;

    public EliteEntityTracker(EliteEntity eliteEntity, boolean isPersistent) {
        super(eliteEntity.getLivingEntity().getUniqueId(), eliteEntity.getLivingEntity(), !isPersistent, true, eliteMobEntities);
        this.eliteEntity = eliteEntity;
        eliteMobEntities.put(eliteEntity.getLivingEntity().getUniqueId(), eliteEntity);
    }

    @Override
    public void specificRemoveHandling(RemovalReason removalReason) {
        switch (removalReason) {
            case CHUNK_UNLOAD:
                eliteEntity.chunkUnload();
                break;
            case WORLD_UNLOAD:
                eliteEntity.worldUnload();
                break;
            case UNSPECIFIED_WATCHDOG_REMOVAL:
            case SHUTDOWN:
                eliteEntity.remove(removalReason);
        }
    }

}
