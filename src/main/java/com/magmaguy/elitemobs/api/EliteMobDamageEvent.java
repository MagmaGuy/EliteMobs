package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;

public class EliteMobDamageEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Entity entity;
    private EliteMobEntity eliteMobEntity;
    private boolean isCancelled = false;
    private EntityDamageEvent entityDamageEvent;

    public EliteMobDamageEvent(EliteMobEntity eliteMobEntity, EntityDamageEvent event) {
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

            Bukkit.getServer().getPluginManager().callEvent(new EliteMobDamageEvent(eliteMobEntity, event));

        }

    }

}
