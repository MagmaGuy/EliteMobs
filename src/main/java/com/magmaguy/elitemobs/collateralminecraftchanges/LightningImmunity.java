package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class LightningImmunity implements Listener {
    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLightningStrike(EntityDamageByEntityEvent event){
        if (!event.getDamager().getType().equals(EntityType.LIGHTNING_BOLT)) return;
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteEntity == null) return;
        if (eliteEntity.hasPower(PowersConfig.getPower("attack_lightning.yml")) ||
                eliteEntity.hasPower(PowersConfig.getPower("lightning_bolts.yml")))
            event.setCancelled(true);
    }
}
