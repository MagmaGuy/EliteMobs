package com.magmaguy.elitemobs.api.instanced;

import com.magmaguy.elitemobs.instanced.MatchInstance;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Public API event fired once when a running match reaches a completed, victory, or defeat state.
 * The instance still exists at this point and may remain available during its configured closing
 * delay.
 */
public class MatchEndEvent extends Event implements MatchEvent {
    private static final HandlerList handlers = new HandlerList();
    private final MatchInstance matchInstance;

    public MatchEndEvent(MatchInstance matchInstance) {
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
