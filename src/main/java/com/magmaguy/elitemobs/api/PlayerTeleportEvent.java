package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.wormhole.WormholeManager;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class PlayerTeleportEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Location destination;
    private final Location originalLocation;
    @Getter
    private final Player player;
    private final boolean deferredExecution;
    private boolean isCancelled = false;

    /**
     * Event fired when players teleport due to EliteMobs. This is used for teleporting to the Adventurer's Guild and to
     * Dungeons.
     *
     * @param player      Player to be teleported
     * @param destination Teleport destination
     */
    public PlayerTeleportEvent(Player player, Location destination) {
        this(player, destination, false);
    }

    private PlayerTeleportEvent(Player player, Location destination, boolean deferredExecution) {
        this.player = player;
        this.destination = destination;
        this.originalLocation = player.getLocation().clone();
        this.deferredExecution = deferredExecution;
    }

    public static void teleportPlayer(Player player, Location destination) {
        new EventCaller(new PlayerTeleportEvent(player, destination));
    }

    /**
     * Fires every cancellable EliteMobs teleport event before executing any teleport in the batch.
     * A cancellation therefore rejects the whole initial group instead of moving only its first
     * members. Bukkit-level teleport cancellation can still prevent an individual final move.
     *
     * @return true when every EliteMobs event passed preflight and execution was attempted
     */
    public static boolean teleportPlayers(Collection<Player> players, Location destination) {
        if (destination == null || destination.getWorld() == null) return false;
        LinkedHashMap<UUID, Player> uniquePlayers = new LinkedHashMap<>();
        for (Player player : players)
            if (player != null) uniquePlayers.putIfAbsent(player.getUniqueId(), player);
        if (uniquePlayers.isEmpty()) return false;

        List<PlayerTeleportEvent> events = new ArrayList<>();
        for (Player player : uniquePlayers.values()) {
            if (!player.isOnline() || !player.isValid()) return false;
            PlayerTeleportEvent event = new PlayerTeleportEvent(player, destination, true);
            new EventCaller(event);
            if (event.isCancelled()) return false;
            events.add(event);
        }
        if (events.stream().anyMatch(event -> !event.player.isOnline() || !event.player.isValid())) return false;
        events.forEach(PlayerTeleportEvent::executeTeleport);
        return true;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

    public Location getOriginalLocation() {
        return originalLocation;
    }

    public void executeTeleport() {
        if (!EliteMobsWorld.isEliteMobsWorld(player.getLocation().getWorld().getUID()))
            PlayerData.setBackTeleportLocation(player, originalLocation);
        WormholeManager.getInstance(false).addPlayerToCooldown(player, destination);
        if (!player.getPassengers().isEmpty()) player.getPassengers().forEach(player::removePassenger);
        player.teleport(destination);
    }

    public static class PlayerTeleportEventExecutor implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPlayerTeleport(PlayerTeleportEvent event) {
            if (!event.deferredExecution) event.executeTeleport();
        }
    }

}
