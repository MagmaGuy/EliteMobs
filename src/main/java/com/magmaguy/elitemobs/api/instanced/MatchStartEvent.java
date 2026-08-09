package com.magmaguy.elitemobs.api.instanced;

import com.magmaguy.elitemobs.instanced.MatchInstance;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Public API event fired after a match enters {@code ONGOING}, its players are moved to the start,
 * and its participant snapshot has been established.
 */
public class MatchStartEvent extends Event implements MatchEvent {
    private static final HandlerList handlers = new HandlerList();
    private final MatchInstance matchInstance;

    public MatchStartEvent(MatchInstance matchInstance) {
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
