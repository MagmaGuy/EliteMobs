package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EliteMobGenericDamagedHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void eliteMobDamagedGeneric(EliteMobDamagedEvent event) {

        if (event.isCancelled()) return;
        if (event.getEntityDamageEvent().getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) return;
        if (!event.getEntityDamageEvent().getCause().equals(EntityDamageEvent.DamageCause.CUSTOM))
            event.getEntityDamageEvent().setDamage(EntityDamageEvent.DamageModifier.BASE, event.getEntityDamageEvent().getDamage());

    }

}
