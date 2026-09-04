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
 * Performs a narrowly scoped, same-world player movement which instance escape protection may
 * accept. Authorization exists only on the synchronous call stack, is bound to one player and
 * exact destination, and is always removed in {@code finally}; it cannot authorize another
 * player's teleport or a later event.
 */
public final class InstancePlayerMovement {
    private static final double DESTINATION_EPSILON_SQUARED = 1.0E-10D;
    private static final ThreadLocal<Deque<Authorization>> AUTHORIZATIONS = new ThreadLocal<>();

    private InstancePlayerMovement() {
    }

    /**
     * Moves a player without weakening instance escape protection.
     *
     * <p>When the player belongs to an instance, both endpoints must remain in that same ongoing
     * instance and the player must be an active participant. Other Bukkit listeners may still
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
                    || instance.state != MatchInstance.InstancedRegionState.ONGOING
                    || !instance.players.contains(player)
                    || !instance.isInRegion(player.getLocation())
                    || !instance.isInRegion(destination))
                return false;
        }

        Authorization authorization = new Authorization(
                player.getUniqueId(),
                sourceWorld.getUID(),
                destination.clone(),
                cause,
                instance);
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
            if (!authorization.worldId().equals(event.getFrom().getWorld().getUID())) continue;
            if (!authorization.worldId().equals(destination.getWorld().getUID())) continue;
            if (authorization.cause() != event.getCause()) continue;
            if (authorization.destination().distanceSquared(destination) > DESTINATION_EPSILON_SQUARED) continue;

            MatchInstance current = PlayerData.getMatchInstance(event.getPlayer());
            if (current != authorization.instance()) return false;
            if (current == null) return true;
            return current.state == MatchInstance.InstancedRegionState.ONGOING
                    && current.players.contains(event.getPlayer())
                    && current.world != null
                    && current.world.getUID().equals(authorization.worldId())
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
            UUID worldId,
            Location destination,
            PlayerTeleportEvent.TeleportCause cause,
            MatchInstance instance) {
    }
}
