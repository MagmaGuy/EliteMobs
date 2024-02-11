package com.magmaguy.elitemobs.api.instanced;

import com.magmaguy.elitemobs.instanced.MatchInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MatchLeaveEvent extends Event implements MatchEvent, MatchPlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final MatchInstance matchInstance;
    private final Player player;

    public MatchLeaveEvent(MatchInstance matchInstance, Player player) {
        this.matchInstance = matchInstance;
        this.player = player;
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

    @Override
    public Player getPlayer() {
        return player;
    }
}