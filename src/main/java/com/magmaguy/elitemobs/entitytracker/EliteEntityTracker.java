package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.UUID;

public class EliteEntityTracker extends TrackedEntity implements AbstractTrackedEntity {

    public static HashMap<UUID, EliteMobEntity> eliteMobEntities = new HashMap<>();

    public EliteMobEntity eliteMobEntity;

    public EliteEntityTracker(EliteMobEntity eliteMobEntity, boolean isPersistent) {
        super(eliteMobEntity.uuid, eliteMobEntity.getLivingEntity(), !isPersistent, true, eliteMobEntities);
        this.eliteMobEntity = eliteMobEntity;
        eliteMobEntities.put(eliteMobEntity.uuid, eliteMobEntity);
        if (eliteMobEntity.getLivingEntity() != null)
            eliteMobEntity.getLivingEntity().setMetadata(
                    MetadataHandler.ELITE_MOB_METADATA,
                    new FixedMetadataValue(MetadataHandler.PLUGIN, eliteMobEntity.getLevel()));
    }

    @Override
    public void specificRemoveHandling(RemovalReason removalReason) {
        eliteMobEntity.remove(removalReason);
        if (removalReason.equals(RemovalReason.CHUNK_UNLOAD)) {
            if (eliteMobEntity.customBossEntity != null)
                eliteMobEntity.customBossEntity.chunkUnload();
            if (eliteMobEntity.regionalBossEntity != null)
                eliteMobEntity.regionalBossEntity.chunkUnload();
        }

        if (removalReason.equals(RemovalReason.SHUTDOWN))
            if (eliteMobEntity.customBossEntity != null)
                eliteMobEntity.customBossEntity.remove(true);

        if (removalReason.equals(RemovalReason.DEATH))
            if (eliteMobEntity.regionalBossEntity != null)
                eliteMobEntity.regionalBossEntity.respawnRegionalBoss();

        if (removalReason.equals(RemovalReason.REMOVE_COMMAND)) {
            if (eliteMobEntity.customBossEntity != null)
                eliteMobEntity.customBossEntity.remove(true);
            if (eliteMobEntity.regionalBossEntity != null)
                eliteMobEntity.regionalBossEntity.removePermanently();
        }
    }

}
