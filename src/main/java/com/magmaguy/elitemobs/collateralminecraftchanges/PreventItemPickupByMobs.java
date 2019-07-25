package com.magmaguy.elitemobs.collateralminecraftchanges;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class PreventItemPickupByMobs implements Listener {

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {

        if (event.getEntity().getType().equals(EntityType.ZOMBIE) ||
                event.getEntity().getType().equals(EntityType.DROWNED) ||
                event.getEntity().getType().equals(EntityType.HUSK) ||
                event.getEntity().getType().equals(EntityType.SKELETON) ||
                event.getEntity().getType().equals(EntityType.WITHER_SKELETON) ||
                event.getEntity().getType().equals(EntityType.ENDERMAN) ||
                event.getEntity().getType().equals(EntityType.PIG_ZOMBIE) ||
                event.getEntity().getType().equals(EntityType.STRAY) ||
                event.getEntity().getType().equals(EntityType.ILLUSIONER))
            event.setCancelled(true);

    }

}
