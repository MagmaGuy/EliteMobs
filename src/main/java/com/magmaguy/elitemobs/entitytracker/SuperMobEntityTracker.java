package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.UUID;

public class SuperMobEntityTracker extends TrackedEntity implements AbstractTrackedEntity {

    public static HashMap<UUID, LivingEntity> superMobEntities = new HashMap<>();

    public UUID uuid;
    public LivingEntity livingEntity;

    public SuperMobEntityTracker(UUID uuid, LivingEntity livingEntity) {
        super(uuid, livingEntity, false, false, superMobEntities);
        this.uuid = uuid;
        this.livingEntity = livingEntity;
        superMobEntities.put(uuid, livingEntity);
        livingEntity.setMetadata(MetadataHandler.SUPER_MOB_METADATA, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
    }

    @Override
    public void specificRemoveHandling(RemovalReason removalReason) {
        livingEntity.removeMetadata(MetadataHandler.SUPER_MOB_METADATA, MetadataHandler.PLUGIN);
    }

}
