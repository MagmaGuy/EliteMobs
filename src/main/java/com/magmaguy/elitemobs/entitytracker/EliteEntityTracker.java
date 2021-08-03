package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.metadata.LazyMetadataValue;

import java.util.HashMap;
import java.util.UUID;

public class EliteEntityTracker extends TrackedEntity implements AbstractTrackedEntity {

    public static HashMap<UUID, EliteEntity> eliteMobEntities = new HashMap<>();

    public EliteEntity eliteEntity;

    public EliteEntityTracker(EliteEntity eliteEntity, boolean isPersistent) {
        super(eliteEntity.getLivingEntity().getUniqueId(), eliteEntity.getLivingEntity(), !isPersistent, true, eliteMobEntities);
        this.eliteEntity = eliteEntity;
        eliteMobEntities.put(eliteEntity.getLivingEntity().getUniqueId(), eliteEntity);
        if (eliteEntity.getLivingEntity() != null)
            eliteEntity.getLivingEntity().setMetadata(
                    MetadataHandler.ELITE_MOB_METADATA,
                    new LazyMetadataValue(MetadataHandler.PLUGIN, LazyMetadataValue.CacheStrategy.NEVER_CACHE, eliteEntity::getLevel));
    }

    @Override
    public void specificRemoveHandling(RemovalReason removalReason) {
        switch (removalReason){
            case CHUNK_UNLOAD:
                eliteEntity.chunkUnload();
                break;
            case WORLD_UNLOAD:
                eliteEntity.worldUnload();
                break;
            default:
                eliteEntity.remove(removalReason);
        }
    }

}
