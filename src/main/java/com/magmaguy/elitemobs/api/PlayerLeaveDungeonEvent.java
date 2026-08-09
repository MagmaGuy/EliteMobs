package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

/** Public API notification fired after a player has fully left an instanced dungeon match. */
public class PlayerLeaveDungeonEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final DungeonInstance dungeonInstance;
    @Getter
    @Nullable
    private final Player player;

    /** @deprecated API-created events should include the player. */
    @Deprecated(forRemoval = false)
    public PlayerLeaveDungeonEvent(DungeonInstance dungeonInstance) {
        this(dungeonInstance, null);
    }

    public PlayerLeaveDungeonEvent(DungeonInstance dungeonInstance, @Nullable Player player) {
        this.dungeonInstance = dungeonInstance;
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
