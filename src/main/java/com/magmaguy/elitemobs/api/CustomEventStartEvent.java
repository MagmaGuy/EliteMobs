package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.events.CustomEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * To clear up the weird naming system here, this event is called when custom event (the type created by users for the
 * customizable in-game event system) is called, i.e. for treasure goblins, meteors and the dead moon event.
 */
public class CustomEventStartEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private CustomEvent customEvent;
    private boolean cancelled = false;

    public CustomEventStartEvent(CustomEvent customEvent) {
        this.customEvent = customEvent;
    }

    public CustomEvent getCustomEvent() {
        return customEvent;
    }

    public void setCustomEvent(CustomEvent customEvent) {
        this.customEvent = customEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
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
