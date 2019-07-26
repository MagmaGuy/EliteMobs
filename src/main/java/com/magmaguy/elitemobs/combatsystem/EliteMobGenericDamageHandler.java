package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.api.EliteMobDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EliteMobGenericDamageHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void eliteMobDamagedGeneric(EliteMobDamageEvent event) {

        if (event.isCancelled()) return;
        if (event.getEntityDamageEvent().getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) return;
        if (!event.getEntityDamageEvent().getCause().equals(EntityDamageEvent.DamageCause.CUSTOM))
            event.getEntityDamageEvent().setDamage(EntityDamageEvent.DamageModifier.BASE, event.getEntityDamageEvent().getDamage());

    }

}
