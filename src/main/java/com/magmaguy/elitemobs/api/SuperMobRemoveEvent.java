package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class SuperMobRemoveEvent extends Event {

    public static void callEvent(UUID uuid, RemovalReason removalReason) {
        new EventCaller(new SuperMobRemoveEvent(uuid, removalReason));
    }

    private static final HandlerList handlers = new HandlerList();
    private final UUID uuid;

    public SuperMobRemoveEvent(UUID uuid, RemovalReason removalReason) {
        this.uuid = uuid;
        EntityTracker.unregister(uuid, removalReason);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Returns the entity being converted to an EliteMobEntity
     *
     * @return Entity being converted into an Elite Mob
     */
    public UUID getUuid() {
        return this.uuid;
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
