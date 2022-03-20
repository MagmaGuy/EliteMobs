package com.magmaguy.elitemobs.collateralminecraftchanges;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.potion.PotionEffectType;

public class CombustionPrevention implements Listener {
    @EventHandler
    public void onCombust(EntityCombustEvent event) {
        if (event.getEntity() instanceof LivingEntity &&
                ((LivingEntity) event.getEntity()).hasPotionEffect(PotionEffectType.FIRE_RESISTANCE))
            event.setCancelled(true);
    }
}
