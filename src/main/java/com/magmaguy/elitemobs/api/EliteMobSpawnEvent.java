package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EliteMobSpawnEvent extends Event implements Cancellable {

    private final HandlerList handlers = new HandlerList();
    private Entity entity;
    private EliteMobEntity eliteMobEntity;
    private CreatureSpawnEvent.SpawnReason spawnReason;
    private boolean isCancelled = false;

    /**
     * Cancelling this event will prevent the Elite Mob from being constructed.
     *
     * @param entity         Entity associated to the Elite Mob
     * @param eliteMobEntity EliteMobEntity being formed
     * @param spawnReason    Reason for the Entity's spawn
     */
    public EliteMobSpawnEvent(Entity entity, EliteMobEntity eliteMobEntity, CreatureSpawnEvent.SpawnReason spawnReason) {
        this.entity = entity;
        this.eliteMobEntity = eliteMobEntity;
        this.spawnReason = spawnReason;
    }

    public HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Returns the entity being converted to an EliteMobEntity
     *
     * @return Entity being converted into an Elite Mob
     */
    public Entity getEntity() {
        return this.entity;
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
     * @return Reason for the Entity gettings spawned
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
     * Cancels the event. This will cancel the formation of the Elite Mob/
     *
     * @param cancel Cancels teh event
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
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
