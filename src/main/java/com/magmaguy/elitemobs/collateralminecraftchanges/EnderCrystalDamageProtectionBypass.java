package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.utils.EntityFinder;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EnderCrystalDamageProtectionBypass implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!event.getEntity().getType().equals(EntityType.ENDER_CRYSTAL)) return;
        LivingEntity entity = EntityFinder.filterRangedDamagers(event.getDamager());
        if (entity == null || !entity.getType().equals(EntityType.PLAYER)) return;
        event.setCancelled(false);
    }
}
