package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.UUID;

/**
 * Performs narrowly scoped player movement which instance escape protection may
 * accept. Authorization exists only on the synchronous call stack, is bound to one player and
 * exact destination, and is always removed in {@code finally}; it cannot authorize another
 * player's teleport or a later event.
 */
public final class InstancePlayerMovement {
    private static final String WITHIN_INSTANCE_PERMISSION = "elitemobs.instanced.teleport.within";
    private static final double DESTINATION_EPSILON_SQUARED = 1.0E-10D;
    private static final ThreadLocal<Deque<Authorization>> AUTHORIZATIONS = new ThreadLocal<>();

    private InstancePlayerMovement() {
    }

    /** Allows explicitly authorized command/plugin movement within one ongoing match. */
    static boolean permitsWithinInstance(PlayerTeleportEvent event, MatchInstance instance) {
        Player player = event.getPlayer();
        Location destination = event.getTo();
        if (instance == null || instance.isDefunct() || instance.isDestroyingMatch()
                || instance.state != MatchInstance.InstancedRegionState.ONGOING
                || !player.isOnline() || !player.isValid()
                || !player.hasPermission(WITHIN_INSTANCE_PERMISSION)) return false;
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND
                && event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) return false;
        return PlayerData.getMatchInstance(player) == instance
                && MatchInstance.getAnyPlayerInstance(player) == instance
                && instance.world != null
                && instance.world.equals(event.getFrom().getWorld())
                && destination != null && instance.world.equals(destination.getWorld())
                && instance.isInRegion(event.getFrom())
                && instance.isInRegion(destination);
    }

    /**
     * Moves a player without weakening instance escape protection.
     *
     * <p>When the player belongs to an instance, both endpoints must remain in that same
     * instance and the player must be an active participant playing or waiting to start. Other Bukkit listeners may still
     * cancel the teleport.</p>
     */
    public static boolean teleportWithinWorld(
            Player player,
            Location destination,
            PlayerTeleportEvent.TeleportCause cause) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(cause, "cause");
        if (!Bukkit.isPrimaryThread() || !player.isOnline() || !player.isValid()) return false;

        World sourceWorld = player.getWorld();
        if (destination.getWorld() == null || !sourceWorld.equals(destination.getWorld())) return false;

        MatchInstance instance = PlayerData.getMatchInstance(player);
        if (instance != null) {
            if (instance.world == null
                    || !instance.world.equals(sourceWorld)
                    || !(instance.state == MatchInstance.InstancedRegionState.ONGOING
                         || instance.isWaitingPlayer(player))
                    || !instance.players.contains(player)
                    || !instance.isInRegion(player.getLocation())
                    || !instance.isInRegion(destination))
                return false;
        }

        return teleportAuthorized(player, destination, cause, instance, false);
    }

    /** An explicit exit counts as quitting only after Bukkit accepts and completes the teleport. */
    public static boolean teleportLeavingInstance(Player player, Location destination,
                                                  PlayerTeleportEvent.TeleportCause cause) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(cause, "cause");
        if (!Bukkit.isPrimaryThread() || !player.isOnline() || !player.isValid()
                || destination.getWorld() == null) return false;
        MatchInstance instance = PlayerData.getMatchInstance(player);
        if (instance == null) return player.teleport(destination, cause);
        if (instance.isInRegion(destination)) return false;

        boolean moved = teleportAuthorized(player, destination, cause, instance, true);
        if (moved && PlayerData.getMatchInstance(player) == instance
                && !instance.isInRegion(player.getLocation()))
            instance.removeAnyKind(player);
        return moved;
    }

    private static boolean teleportAuthorized(Player player, Location destination,
                                               PlayerTeleportEvent.TeleportCause cause,
                                               MatchInstance instance, boolean leavingInstance) {
        Authorization authorization = new Authorization(
                player.getUniqueId(),
                player.getWorld().getUID(),
                destination.getWorld().getUID(),
                destination.clone(),
                cause,
                instance,
                leavingInstance);
        Deque<Authorization> stack = AUTHORIZATIONS.get();
        if (stack == null) {
            stack = new ArrayDeque<>();
            AUTHORIZATIONS.set(stack);
        }
        stack.push(authorization);
        try {
            return player.teleport(destination, cause);
        } finally {
            Authorization removed = stack.pop();
            if (removed != authorization)
                throw new IllegalStateException("Instance movement authorization stack was corrupted");
            if (stack.isEmpty()) AUTHORIZATIONS.remove();
        }
    }

    static boolean authorizes(PlayerTeleportEvent event) {
        Location destination = event.getTo();
        if (destination == null || destination.getWorld() == null) return false;
        Deque<Authorization> stack = AUTHORIZATIONS.get();
        if (stack == null) return false;
        for (Authorization authorization : stack) {
            if (!authorization.playerId().equals(event.getPlayer().getUniqueId())) continue;
            if (!authorization.sourceWorldId().equals(event.getFrom().getWorld().getUID())) continue;
            if (!authorization.destinationWorldId().equals(destination.getWorld().getUID())) continue;
            if (authorization.cause() != event.getCause()) continue;
            if (authorization.destination().distanceSquared(destination) > DESTINATION_EPSILON_SQUARED) continue;

            MatchInstance current = PlayerData.getMatchInstance(event.getPlayer());
            if (current != authorization.instance()) return false;
            if (current == null) return true;
            if (authorization.leavingInstance())
                return !current.isInRegion(destination)
                        && (current.players.contains(event.getPlayer())
                            || current.spectators.contains(event.getPlayer())
                            || current.participants.contains(event.getPlayer()));
            return (current.state == MatchInstance.InstancedRegionState.ONGOING
                    || current.isWaitingPlayer(event.getPlayer()))
                    && current.players.contains(event.getPlayer())
                    && current.world != null
                    && current.world.getUID().equals(authorization.sourceWorldId())
                    && current.isInRegion(event.getFrom())
                    && current.isInRegion(destination);
        }
        return false;
    }

    static boolean hasAuthorization(Player player) {
        Deque<Authorization> stack = AUTHORIZATIONS.get();
        if (stack == null) return false;
        UUID playerId = player.getUniqueId();
        return stack.stream().anyMatch(authorization -> authorization.playerId().equals(playerId));
    }

    private record Authorization(
            UUID playerId,
            UUID sourceWorldId,
            UUID destinationWorldId,
            Location destination,
            PlayerTeleportEvent.TeleportCause cause,
            MatchInstance instance,
            boolean leavingInstance) {
    }
}
