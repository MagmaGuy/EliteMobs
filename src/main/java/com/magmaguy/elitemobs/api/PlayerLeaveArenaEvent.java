package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLeaveArenaEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final ArenaInstance arenaInstance;

    public PlayerLeaveArenaEvent(ArenaInstance arenaInstance) {
        this.arenaInstance = arenaInstance;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
