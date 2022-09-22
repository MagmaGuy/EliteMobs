package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class ArenaCompleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final ArenaInstance arenaInstance;

    /**
     * Called when an arena gets successfully completed by players.
     *
     * @param arenaInstance Instance of the arena that got completed
     */
    public ArenaCompleteEvent(ArenaInstance arenaInstance) {
        this.arenaInstance = arenaInstance;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class ArenaCompleteEventHandler implements Listener {
        public static void call(ArenaInstance arenaInstance) {
            ArenaCompleteEvent arenaCompleteEvent = new ArenaCompleteEvent(arenaInstance);
            new EventCaller(arenaCompleteEvent);
        }
    }
}
