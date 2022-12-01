package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.api.EliteMobDamagedByEliteMobEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EliteMobDamagedByEliteMobHandler implements Listener {

    @EventHandler
    public void onIronGolemDamage(EliteMobDamagedByEliteMobEvent event) {

        if (event.isCancelled()) return;
        if (event.getDamager().getLivingEntity() == null || event.getDamagee().getLivingEntity() == null) return;

        if (!(event.getDamager().getLivingEntity().getType().equals(EntityType.IRON_GOLEM) ||
                event.getDamagee().getLivingEntity().getType().equals(EntityType.IRON_GOLEM))) return;

        double damage = event.getEntityDamageByEntityEvent().getDamage() * event.getDamager().getLevel() * CombatSystem.PER_LEVEL_POWER_INCREASE;

        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values())
            if (event.getEntityDamageByEntityEvent().isApplicable(modifier))
                event.getEntityDamageByEntityEvent().setDamage(modifier, 0);

        event.getEntityDamageByEntityEvent().setDamage(damage);

    }

}
