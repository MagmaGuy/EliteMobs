package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EliteBlazeWaterDamagePrevention implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlazeWaterDamage(EliteMobDamagedEvent event) {
        if (!event.getEntity().getType().equals(EntityType.BLAZE)) return;
        if (!event.getEntityDamageEvent().getCause().equals(EntityDamageEvent.DamageCause.DROWNING)) return;
        event.setCancelled(true);
    }
}
