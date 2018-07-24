package com.magmaguy.elitemobs.mobpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class AggroPrevention implements Listener {

    @EventHandler
    public void onTarget (EntityTargetLivingEntityEvent event){

        if (event.getEntity() instanceof  LivingEntity && event.getEntity().hasMetadata(MetadataHandler.ELITE_MOB_MD) &&
                event.getTarget() != null && event.getTarget().hasMetadata(MetadataHandler.ELITE_MOB_MD) &&
                !(event.getTarget() instanceof IronGolem) && !(event.getEntity() instanceof IronGolem))
            event.setCancelled(true);

    }

}
