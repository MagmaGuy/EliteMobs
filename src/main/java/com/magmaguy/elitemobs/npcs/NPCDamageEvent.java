package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class NPCDamageEvent implements Listener {

    @EventHandler
    public void onNPCDamage(EntityDamageEvent event) {
        if (!EntityTracker.isNPCEntity(event.getEntity())) return;
        event.setCancelled(true);
    }

}
