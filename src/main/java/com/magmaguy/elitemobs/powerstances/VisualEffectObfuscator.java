package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.utils.EntityFinder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class VisualEffectObfuscator implements Listener {

    @EventHandler
    public void onTargetPlayer(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player)) return;
        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteMobEntity == null) return;

        eliteMobEntity.setHasVisualEffectObfuscated(false);
        eliteMobEntity.getLivingEntity().setCustomNameVisible(true);
    }

    @EventHandler
    public void onDamageByPlayer(EntityDamageByEntityEvent event) {
        if (!(EntityFinder.getRealDamager(event) instanceof Player)) return;
        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteMobEntity == null) return;

        eliteMobEntity.setHasVisualEffectObfuscated(false);
        eliteMobEntity.getLivingEntity().setCustomNameVisible(true);
    }

}
