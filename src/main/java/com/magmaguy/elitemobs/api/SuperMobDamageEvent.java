package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.entitytracker.SuperMobEntityTracker;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;

public class SuperMobDamageEvent extends Event implements Cancellable {
    //todo: make canceleable

    public static void callEvent(LivingEntity livingEntity, EntityDamageEvent entityDamageEvent) {
        new EventCaller(new SuperMobDamageEvent(livingEntity, entityDamageEvent));
    }

    private static final HandlerList handlers = new HandlerList();
    private final LivingEntity livingEntity;
    private final EntityDamageEvent entityDamageEvent;
    private boolean cancelled = false;

    public SuperMobDamageEvent(LivingEntity livingEntity, EntityDamageEvent entityDamageEvent) {
        this.livingEntity = livingEntity;
        this.entityDamageEvent = entityDamageEvent;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Returns the entity being converted to an EliteMobEntity
     *
     * @return Entity being converted into an Elite Mob
     */
    public LivingEntity getLivingEntity() {
        return this.livingEntity;
    }

    public EntityDamageEvent getEntityDamageEvent() {
        return this.entityDamageEvent;
    }

    /**
     * Returns a list of handlers
     *
     * @return List of handlers
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public static class SuperMobDamageEventFilter implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void superMobDamageFilter(EntityDamageEvent event) {
            LivingEntity livingEntity = SuperMobEntityTracker.superMobEntities.get(event.getEntity().getUniqueId());
            if (livingEntity == null) return;
            SuperMobDamageEvent superMobDamageEvent = new SuperMobDamageEvent((LivingEntity) event.getEntity(), event);
            new EventCaller(superMobDamageEvent);
            if (superMobDamageEvent.isCancelled()) event.setCancelled(true);
        }
    }

}
