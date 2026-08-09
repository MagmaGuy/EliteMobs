package com.magmaguy.elitemobs.api.instanced;

import com.magmaguy.elitemobs.instanced.MatchInstance;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Public API event fired once after the generic match roster, spectator, and death-marker state has
 * been cleared. Subtypes may perform their own arena reset or world teardown immediately after this
 * notification.
 */
public class MatchDestroyEvent extends Event implements MatchEvent {
    private static final HandlerList handlers = new HandlerList();
    private final MatchInstance matchInstance;

    public MatchDestroyEvent(MatchInstance matchInstance) {
        this.matchInstance = matchInstance;
    }

    @Override
    public MatchInstance getInstance() {
        return matchInstance;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
