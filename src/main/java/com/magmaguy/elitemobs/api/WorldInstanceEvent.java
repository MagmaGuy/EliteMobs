package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.instanceddungeons.InstancedDungeonsConfigFields;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WorldInstanceEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final InstancedDungeonsConfigFields instancedDungeonsConfigFields;
    @Getter
    private final String blueprintWorldName;
    @Getter
    private final String instancedWorldName;
    private boolean cancelled = false;


    public WorldInstanceEvent(String blueprintWorldName,
                              String instancedWorldName,
                              InstancedDungeonsConfigFields instancedDungeonsConfigFields) {
        this.blueprintWorldName = blueprintWorldName;
        this.instancedWorldName = instancedWorldName;
        this.instancedDungeonsConfigFields = instancedDungeonsConfigFields;
    }


    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
