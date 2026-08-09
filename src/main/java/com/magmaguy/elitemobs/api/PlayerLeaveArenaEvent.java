package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

/** Public API notification fired after a player has fully left an arena match. */
public class PlayerLeaveArenaEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final ArenaInstance arenaInstance;
    @Getter
    @Nullable
    private final Player player;

    /** @deprecated API-created events should include the player. */
    @Deprecated(forRemoval = false)
    public PlayerLeaveArenaEvent(ArenaInstance arenaInstance) {
        this(arenaInstance, null);
    }

    public PlayerLeaveArenaEvent(ArenaInstance arenaInstance, @Nullable Player player) {
        this.arenaInstance = arenaInstance;
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
