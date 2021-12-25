package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SuperMobRemoveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final LivingEntity livingEntity;
    public SuperMobRemoveEvent(LivingEntity livingEntity, RemovalReason removalReason) {
        this.livingEntity = livingEntity;
        EntityTracker.unregister(livingEntity, removalReason);
    }

    public static void callEvent(LivingEntity livingEntity, RemovalReason removalReason) {
        new EventCaller(new SuperMobRemoveEvent(livingEntity, removalReason));
    }

    public static HandlerList getHandlerList() {
        return handlers;
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

}
