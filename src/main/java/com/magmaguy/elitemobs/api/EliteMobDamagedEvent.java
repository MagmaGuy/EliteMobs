package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;

public class EliteMobDamagedEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final EliteMobEntity eliteMobEntity;
    private boolean isCancelled = false;
    private final EntityDamageEvent entityDamageEvent;

    public EliteMobDamagedEvent(EliteMobEntity eliteMobEntity, EntityDamageEvent event) {
        this.entity = eliteMobEntity.getLivingEntity();
        this.eliteMobEntity = eliteMobEntity;
        this.entityDamageEvent = event;
    }

    public Entity getEntity() {
        return entity;
    }

    public EliteMobEntity getEliteMobEntity() {
        return eliteMobEntity;
    }

    public EntityDamageEvent getEntityDamageEvent() {
        return entityDamageEvent;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
        entityDamageEvent.setCancelled(b);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


    public static class EliteMobDamageEventFilter implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void onEntityDamageByEntityEvent(EntityDamageEvent event) {

            if (event.isCancelled()) return;
            EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            if (eliteMobEntity == null) return;

            EliteMobDamagedEvent eliteMobDamagedEvent = new EliteMobDamagedEvent(eliteMobEntity, event);
            new EventCaller(eliteMobDamagedEvent);
            if (eliteMobDamagedEvent.isCancelled) {
                eliteMobDamagedEvent.setCancelled(true);
                return;
            }

            if (EliteMobs.worldGuardIsEnabled && !WorldGuardFlagChecker.checkFlag(eliteMobEntity.getLivingEntity().getLocation(),
                    WorldGuardCompatibility.getEliteMobsAntiExploitFlag()))
                return;
            Bukkit.getServer().getPluginManager().callEvent(new GenericAntiExploitEvent(eliteMobEntity, event));

        }

    }

}
