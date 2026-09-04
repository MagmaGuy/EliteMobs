package com.magmaguy.elitemobs.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Fired on the server thread after a player's complete EliteMobs data snapshot is available. */
public final class PlayerDataLoadedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;

    public PlayerDataLoadedEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
