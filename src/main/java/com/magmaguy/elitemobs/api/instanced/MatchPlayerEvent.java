package com.magmaguy.elitemobs.api.instanced;

import org.bukkit.entity.Player;

/**
 * Public API contract for match lifecycle events associated with one player.
 */
public interface MatchPlayerEvent {
    Player getPlayer();
}
