package com.magmaguy.elitemobs.menus.gambling;

import java.util.UUID;

/**
 * Base class for all gambling game sessions.
 * Provides common fields shared by every game.
 */
public abstract class GamblingSession {
    public final UUID playerUUID;
    public int betAmount;

    protected GamblingSession(UUID playerUUID, int betAmount) {
        this.playerUUID = playerUUID;
        this.betAmount = betAmount;
    }
}
