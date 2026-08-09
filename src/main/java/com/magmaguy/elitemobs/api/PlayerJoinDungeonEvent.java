package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

/**
 * Public API notification fired after a player has joined an instanced dungeon match.
 * Use {@link com.magmaguy.elitemobs.api.instanced.MatchJoinEvent} to cancel admission.
 */
public class PlayerJoinDungeonEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final DungeonInstance dungeonInstance;
    @Getter
    @Nullable
    private final Player player;

    /** @deprecated API-created events should include the player. */
    @Deprecated(forRemoval = false)
    public PlayerJoinDungeonEvent(DungeonInstance dungeonInstance) {
        this(dungeonInstance, null);
    }

    public PlayerJoinDungeonEvent(DungeonInstance dungeonInstance, @Nullable Player player) {
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
