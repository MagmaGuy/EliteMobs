package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DungeonUninstallEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final ContentPackagesConfigFields contentPackagesConfigFields;
    @Getter
    private final String dungeonName;
    @Getter
    private final String dungeonFilename;
    private boolean isCancelled = false;

    public DungeonUninstallEvent(ContentPackagesConfigFields contentPackagesConfigFields) {
        this.contentPackagesConfigFields = contentPackagesConfigFields;
        this.dungeonName = contentPackagesConfigFields.getName();
        this.dungeonFilename = contentPackagesConfigFields.getFilename();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }
}
