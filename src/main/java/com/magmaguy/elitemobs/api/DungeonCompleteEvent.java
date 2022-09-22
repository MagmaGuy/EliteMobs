package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DungeonCompleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final DungeonInstance dungeonInstance;

    /**
     * Gets called when players complete a dungeon
     *
     * @param dungeonInstance Instance of the dungeon that completed
     */
    public DungeonCompleteEvent(DungeonInstance dungeonInstance) {
        this.dungeonInstance = dungeonInstance;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
