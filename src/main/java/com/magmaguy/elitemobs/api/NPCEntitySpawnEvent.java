package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.npcs.NPCEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NPCEntitySpawnEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final LivingEntity livingEntity;
    private final NPCEntity npcEntity;
    private boolean isCancelled = false;

    /**
     * Cancelling this event will prevent the NPC from being constructed by removing it and the entity it would've converted
     */
    public NPCEntitySpawnEvent(LivingEntity livingEntity, NPCEntity npcEntity) {
        this.livingEntity = livingEntity;
        this.npcEntity = npcEntity;
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
     * Returns the EliteMobEntity currently being formed
     *
     * @return EliteMobEntity currently being formed
     */
    public NPCEntity getNPCEntity() {
        return this.npcEntity;
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
     * Cancels the event. This will cancel the formation of the Elite Mob and remove the living entity. Once cancelled it can't be uncancelled.
     *
     * @param cancel Cancels the event
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
        if (isCancelled)
            livingEntity.remove();
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
