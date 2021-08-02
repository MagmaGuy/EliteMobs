package com.magmaguy.elitemobs.api.internal;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class NewMinidungeonRelativeBossLocationEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;
    private final Minidungeon minidungeon;
    private final Location relativeLocation;
    private final Location realLocation;
    private final CustomBossesConfigFields customBossesConfigFields;

    public NewMinidungeonRelativeBossLocationEvent(Minidungeon minidungeon, Location relativeLocation, Location realLocation, CustomBossesConfigFields customBossesConfigFields) {
        this.minidungeon = minidungeon;
        this.relativeLocation = relativeLocation;
        this.realLocation = realLocation;
        this.customBossesConfigFields = customBossesConfigFields;
        if (relativeLocation == null) setCancelled(true);
    }

    public Minidungeon getMinidungeon() {
        return minidungeon;
    }

    public Location getRelativeLocation() {
        return relativeLocation;
    }

    public Location getRealLocation() {
        return realLocation;
    }

    public CustomBossesConfigFields getCustomBossConfigFields() {
        return customBossesConfigFields;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
