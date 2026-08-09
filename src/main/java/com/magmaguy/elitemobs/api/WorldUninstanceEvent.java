package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

/**
 * Public API notification fired after an instanced dungeon world has been successfully unloaded
 * and scheduled for permanent deletion.
 */
public class WorldUninstanceEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final ContentPackagesConfigFields contentPackagesConfigFields;
    @Getter
    @Nullable
    private final String instancedWorldName;

    /** @deprecated API-created events should include the instanced world name. */
    @Deprecated(forRemoval = false)
    public WorldUninstanceEvent(ContentPackagesConfigFields contentPackagesConfigFields) {
        this(contentPackagesConfigFields, null);
    }

    public WorldUninstanceEvent(ContentPackagesConfigFields contentPackagesConfigFields,
                                @Nullable String instancedWorldName) {
        this.contentPackagesConfigFields = contentPackagesConfigFields;
        this.instancedWorldName = instancedWorldName;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
