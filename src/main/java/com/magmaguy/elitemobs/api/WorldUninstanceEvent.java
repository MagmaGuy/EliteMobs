package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.instanceddungeons.InstancedDungeonsConfigFields;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WorldUninstanceEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final InstancedDungeonsConfigFields instancedDungeonsConfigFields;

    public WorldUninstanceEvent(InstancedDungeonsConfigFields instancedDungeonsConfigFields) {
        this.instancedDungeonsConfigFields = instancedDungeonsConfigFields;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
