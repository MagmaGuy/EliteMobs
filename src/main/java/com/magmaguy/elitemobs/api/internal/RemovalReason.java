package com.magmaguy.elitemobs.api.internal;

/**
 * DEATH is used for when Elites die in a way detected correctly by EliteMobs
 * CHUNK_UNLOAD fires when an Elite is removed due to a chunk unload. This is a true removal, so persistent Elites won't have it.
 * WORLD_UNLOAD fires when an Elite is removed due to a World unload. Persistent Elites can't survive this.
 * SHUTDOWN fires when a server shuts down, reload or when EliteMobs reloads
 * OTHER fires when the reason couldn't be determined, probably a #remove() API call by another plugin
 * EFFECT_TIMEOUT fires when either a power has ended or when an effect is done being displayed for any reason
 */
public enum RemovalReason {
    DEATH,
    PHASE_BOSS_RESET,
    PHASE_BOSS_PHASE_END,
    CHUNK_UNLOAD,
    WORLD_UNLOAD,
    SHUTDOWN,
    EFFECT_TIMEOUT,
    BOSS_TIMEOUT,
    NPC_TIMEOUT,
    OTHER,
    REMOVE_COMMAND,
    KILL_COMMAND
}
