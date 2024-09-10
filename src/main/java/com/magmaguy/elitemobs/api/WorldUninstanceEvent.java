package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WorldUninstanceEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final ContentPackagesConfigFields contentPackagesConfigFields;

    public WorldUninstanceEvent(ContentPackagesConfigFields contentPackagesConfigFields) {
        this.contentPackagesConfigFields = contentPackagesConfigFields;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
