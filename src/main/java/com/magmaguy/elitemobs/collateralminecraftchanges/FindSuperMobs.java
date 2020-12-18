package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class FindSuperMobs implements Listener {

    @EventHandler
    public void findSuperMob(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities())
            if (SuperMobProperties.isValidSuperMobType(entity))
                if (((LivingEntity) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() ==
                        SuperMobProperties.getDataInstance(entity).getSuperMobMaxHealth())
                    if (!EntityTracker.isSuperMob(entity))
                        EntityTracker.registerSuperMob((LivingEntity) entity);

    }

}
