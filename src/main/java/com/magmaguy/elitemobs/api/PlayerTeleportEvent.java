package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.WorldPackage;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

import java.util.Objects;

public class PlayerTeleportEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Location destination;
    private final Location originalLocation;
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

    public Player getPlayer() {
        return player;
    }

    public Location getDestination() {
        return destination;
    }

    public Location getOriginalLocation() {
        return originalLocation;
    }

    public void executeTeleport() {
        player.teleport(destination);
        for (EMPackage emPackage : EMPackage.getEmPackages().values())
            if (emPackage.isInstalled() &&
                    emPackage instanceof WorldPackage &&
                    ((WorldPackage) emPackage).getWorld() == destination.getWorld())
                return;

        EMPackage adventurersGuildPackage = EMPackage.getContent("adventurers_guild_hub");
        if (adventurersGuildPackage == null || !adventurersGuildPackage.isInstalled() ||
                adventurersGuildPackage.getDungeonPackagerConfigFields().getTeleportLocation() == null ||
                Objects.equals(adventurersGuildPackage.getDungeonPackagerConfigFields().getTeleportLocation().getWorld(), originalLocation.getWorld()))
            return;

        PlayerData.setBackTeleportLocation(player, originalLocation);
    }

    public static class PlayerTeleportEventExecutor implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPlayerTeleport(PlayerTeleportEvent event) {
            event.executeTeleport();
        }
    }

}
