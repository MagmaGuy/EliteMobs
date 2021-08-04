package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import org.bukkit.entity.LivingEntity;

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
    }

    @Override
    public void specificRemoveHandling(RemovalReason removalReason) {
    }

}
