package com.magmaguy.elitemobs.instanced;

import com.magmaguy.magmacore.match.MatchCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

/**
 * Player movement that instance escape protection accepts, kept for existing callers. Each
 * move carries an authorization bound to one player and one exact destination for the duration
 * of the call, issued by MagmaCore's match core.
 */
public final class InstancePlayerMovement {
    private InstancePlayerMovement() {
    }

    /**
     * Moves a player without weakening instance escape protection. When the player belongs to an
     * instance, both endpoints must remain in that same instance and the player must be an
     * active participant playing or waiting to start. Other listeners may still cancel it.
     */
    public static boolean teleportWithinWorld(Player player, Location destination,
                                              PlayerTeleportEvent.TeleportCause cause) {
        return MatchCore.moveWithin(player, destination, cause);
    }

    /** An explicit exit counts as quitting only after Bukkit accepts and completes the teleport. */
    public static boolean teleportLeavingInstance(Player player, Location destination,
                                                  PlayerTeleportEvent.TeleportCause cause) {
        return MatchCore.moveOut(player, destination, cause);
    }

    /** The existing match owner admits, rescues, spectates or evacuates one player synchronously. */
    public static boolean teleportForMatch(Player player, Location destination, MatchInstance match,
                                           boolean notifyEliteMobs) {
        Objects.requireNonNull(player, "player");
        if (match == null) return false;
        return match.moveParticipant(player, destination);
    }
}
