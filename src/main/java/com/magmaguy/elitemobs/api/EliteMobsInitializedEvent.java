package com.magmaguy.elitemobs.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EliteMobsInitializedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public EliteMobsInitializedEvent() {
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
