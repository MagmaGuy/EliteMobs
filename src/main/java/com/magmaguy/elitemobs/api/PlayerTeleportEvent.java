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

public class PlayerTeleportEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Location destination;
    private final Location originalLocation;
    @Getter
    private final Player player;
    private boolean isCancelled = false;

    /**
     * Event fired when players teleport due to EliteMobs. This is used for teleporting to the Adventurer's Guild and to
     * Dungeons.
     *
     * @param player      Player to be teleported
     * @param destination Teleport destination
     */
    public PlayerTeleportEvent(Player player, Location destination) {
        this.player = player;
        this.destination = destination;
        this.originalLocation = player.getLocation().clone();
    }

    public static void teleportPlayer(Player player, Location destination) {
        new EventCaller(new PlayerTeleportEvent(player, destination));
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
            event.executeTeleport();
        }
    }

}
