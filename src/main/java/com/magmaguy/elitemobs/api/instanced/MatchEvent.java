package com.magmaguy.elitemobs.api.instanced;

import com.magmaguy.elitemobs.instanced.MatchInstance;

/**
 * Marker contract for the public Bukkit lifecycle events emitted by an EliteMobs match instance.
 * Events are fired synchronously on the server thread.
 */
public interface MatchEvent {
    MatchInstance getInstance();
}
