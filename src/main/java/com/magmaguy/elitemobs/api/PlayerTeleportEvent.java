package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

import java.util.HashMap;

public class PlayerTeleportEvent extends Event implements Cancellable {

    public static void teleportPlayer(Player player, Location destination) {
        new EventCaller(new PlayerTeleportEvent(player, destination));
    }

    public static HashMap<Player, Location> previousLocations = new HashMap<>();

    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;
    private final Location destination;
    private final Location originalLocation;
    private final Player player;

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

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
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
        for (Minidungeon minidungeon : Minidungeon.minidungeons.values())
            if (minidungeon.isInstalled)
                if (minidungeon.dungeonPackagerConfigFields.getDungeonLocationType().equals(DungeonPackagerConfigFields.DungeonLocationType.WORLD))
                    if (minidungeon.world.equals(originalLocation.getWorld()))
                        return;

        if (AdventurersGuildConfig.guildWorldLocation != null)
            if (AdventurersGuildConfig.guildWorldLocation.getWorld().equals(originalLocation.getWorld()))
                return;
        previousLocations.put(player, originalLocation);
    }

    public static class PlayerTeleportEventExecutor implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPlayerTeleport(PlayerTeleportEvent event) {
            event.executeTeleport();
        }
    }

}
