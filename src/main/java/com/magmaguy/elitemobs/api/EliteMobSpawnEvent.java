package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EliteMobSpawnEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final LivingEntity livingEntity;
    private final EliteMobEntity eliteMobEntity;
    private final CreatureSpawnEvent.SpawnReason spawnReason;
    private boolean isCancelled = false;

    /**
     * Cancelling this event will prevent the Elite Mob from being constructed by removing it and the entity it would've converted
     *
     * @param eliteMobEntity EliteMobEntity being formed
     */
    public EliteMobSpawnEvent(EliteMobEntity eliteMobEntity) {
        this.livingEntity = eliteMobEntity.getLivingEntity();
        this.eliteMobEntity = eliteMobEntity;
        this.spawnReason = eliteMobEntity.getSpawnReason();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Returns the entity being converted to an EliteMobEntity
     *
     * @return Entity being converted into an Elite Mob
     */
    public LivingEntity getEntity() {
        return this.livingEntity;
    }

    /**
     * Returns the EliteMobEntity currently being formed
     *
     * @return EliteMobEntity currently being formed
     */
    public EliteMobEntity getEliteMobEntity() {
        return this.eliteMobEntity;
    }

    /**
     * Gets the reason for the Entity getting spawned
     *
     * @return Reason for the Entity getting spawned
     */
    public CreatureSpawnEvent.SpawnReason getReason() {
        return this.spawnReason;
    }

    /**
     * Returns if the event is cancelled
     *
     * @return If the event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    /**
     * Cancels the event. This will cancel the formation of the Elite Mob and remove the living entity. Once cancel\led it can't be uncancelled.
     *
     * @param cancel Cancels the event
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
        if (isCancelled)
            eliteMobEntity.remove(true);
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
