package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.utils.EventCaller;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerPreTeleportEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Location destination;
    private final Location originalLocation;
    private final Player player;
    private boolean isCancelled = false;

    /**
     * Called when a player initiates a teleport to a location. The teleport will go through 3 seconds after the event is
     * launched, assuming it isn't cancelled by the player or via code.
     *
     * @param player      Player that will teleport
     * @param destination Teleport destination
     */
    public PlayerPreTeleportEvent(Player player, Location destination) {
        this.player = player;
        this.destination = destination.clone();
        this.originalLocation = player.getLocation().clone();
    }

    public static void teleportPlayer(Player player, Location destination) {
        if (destination.getWorld() == null) return;
        if (CombatTagConfig.isEnableTeleportTimer())
            new EventCaller(new PlayerPreTeleportEvent(player, destination));
        else
            PlayerTeleportEvent.teleportPlayer(player, destination);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public void startTeleport() {
        new BukkitRunnable() {
            int timerLeft = 3;

            @Override
            public void run() {
                if (!player.isValid()) {
                    cancel();
                    return;
                }

                if (player.getLocation().getX() != originalLocation.getX() ||
                        player.getLocation().getY() != originalLocation.getY() ||
                        player.getLocation().getZ() != originalLocation.getZ())
                    isCancelled = true;

                if (isCancelled) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(ChatColorConverter.convert(TranslationConfig.getTeleportCancelled())));
                    cancel();
                    return;
                }

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(ChatColorConverter.convert(TranslationConfig.getTeleportTimeLeft())
                                .replace("$time", timerLeft + "")));


                if (timerLeft == 0) {
                    PlayerTeleportEvent.teleportPlayer(player, destination);
                    cancel();
                    return;
                }

                timerLeft--;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20);
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

    public static class PlayerPreTeleportEventEvents implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onTeleportEvent(PlayerPreTeleportEvent event) {
            event.startTeleport();
        }
    }

}
