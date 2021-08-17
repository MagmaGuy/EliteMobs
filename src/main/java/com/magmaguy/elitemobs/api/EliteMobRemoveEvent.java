package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EliteMobRemoveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final EliteEntity eliteEntity;
    private final RemovalReason removalReason;

    /**
     * This is always the last event to be called when an Elite Mob dies, and is used to clean up references to it in memory.
     *
     * @param eliteEntity   Elite Mob removed
     * @param removalReason Reason for removal
     */
    public EliteMobRemoveEvent(EliteEntity eliteEntity, RemovalReason removalReason) {
        this.entity = eliteEntity.getUnsyncedLivingEntity();
        this.eliteEntity = eliteEntity;
        this.removalReason = removalReason;
        if (eliteEntity.getUnsyncedLivingEntity() != null)
            EntityTracker.unregister(eliteEntity.getUnsyncedLivingEntity().getUniqueId(), removalReason);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public EliteEntity getEliteMobEntity() {
        return this.eliteEntity;
    }

    public RemovalReason getRemovalReason() {
        return removalReason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
