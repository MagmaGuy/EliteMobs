package com.magmaguy.elitemobs.api.instanced;

import com.magmaguy.elitemobs.instanced.MatchInstance;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

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
}
