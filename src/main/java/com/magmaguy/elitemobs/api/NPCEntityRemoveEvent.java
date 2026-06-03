package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NPCEntityRemoveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final LivingEntity livingEntity;
    private final NPCEntity npcEntity;
    private final RemovalReason removalReason;

    public NPCEntityRemoveEvent(LivingEntity livingEntity, NPCEntity npcEntity, RemovalReason removalReason) {
        this.livingEntity = livingEntity;
        this.npcEntity = npcEntity;
        this.removalReason = removalReason;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public NPCEntity getNPCEntity() {
        return npcEntity;
    }

    public RemovalReason getRemovalReason() {
        return removalReason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
