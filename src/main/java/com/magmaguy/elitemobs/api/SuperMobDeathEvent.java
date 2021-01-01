package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class SuperMobDeathEvent extends Event {

    public static void callEvent(LivingEntity livingEntity) {
        new EventCaller(new SuperMobDeathEvent(livingEntity));
    }

    private static final HandlerList handlers = new HandlerList();
    private final LivingEntity livingEntity;

    public SuperMobDeathEvent(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
        new SuperMobRemoveEvent(livingEntity.getUniqueId(), RemovalReason.DEATH);
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

    /**
     * Returns a list of handlers
     *
     * @return List of handlers
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class SuperMobDeathEventFilter implements Listener {
        @EventHandler
        public void onMobDeath(EntityDeathEvent event) {
            LivingEntity livingEntity = EntityTracker.getSuperMob(event.getEntity());
            if (livingEntity == null) return;
            new EventCaller(new SuperMobDeathEvent(livingEntity));
        }
    }

}
