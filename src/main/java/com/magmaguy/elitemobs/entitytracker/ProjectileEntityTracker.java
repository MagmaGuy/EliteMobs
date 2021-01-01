package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import org.bukkit.entity.Projectile;

import java.util.HashMap;
import java.util.UUID;

public class ProjectileEntityTracker extends TrackedEntity implements AbstractTrackedEntity {

    public static HashMap<UUID, Projectile> projectileEntities = new HashMap<>();

    public ProjectileEntityTracker(UUID uuid, Projectile projectile) {
        super(uuid, projectile, true, true, projectileEntities);
        projectileEntities.put(uuid, projectile);
    }

    @Override
    public void specificRemoveHandling(RemovalReason removalReason) {

    }

}

