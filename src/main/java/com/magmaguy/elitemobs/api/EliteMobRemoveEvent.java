package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.utils.DebugMessage;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EliteMobRemoveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final EliteMobEntity eliteMobEntity;
    private final RemovalReason removalReason;

    /**
     * This is always the last event to be called when an Elite Mob dies, and is used to clean up references to it in memory.
     *
     * @param eliteMobEntity Elite Mob removed
     * @param removalReason  Reason for removal
     */
    public EliteMobRemoveEvent(EliteMobEntity eliteMobEntity, RemovalReason removalReason) {
        new DebugMessage("Unregistering elite mob!");
        this.entity = eliteMobEntity.getLivingEntity();
        this.eliteMobEntity = eliteMobEntity;
        this.removalReason = removalReason;
        EntityTracker.unregister(eliteMobEntity.uuid, removalReason);
    }

    public Entity getEntity() {
        return this.entity;
    }

    public EliteMobEntity getEliteMobEntity() {
        return this.eliteMobEntity;
    }

    public RemovalReason getRemovalReason() {
        return removalReason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
