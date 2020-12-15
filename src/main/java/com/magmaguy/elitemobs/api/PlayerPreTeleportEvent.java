package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.utils.EventCaller;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class PlayerPreTeleportEvent extends Event implements Cancellable {

    public static void teleportPlayer(Player player, Location destination) {
        if (CombatTagConfig.enableTeleportTimer)
            new EventCaller(new PlayerPreTeleportEvent(player, destination));
        else
            PlayerTeleportEvent.teleportPlayer(player, destination);
    }

    private static final HashMap<Player, PlayerPreTeleportEvent> playerPlayerPreTeleportEventHashMap = new HashMap<>();

    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;
    private final Location destination;
    private final Location originalLocation;
    private final Player player;

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
        playerPlayerPreTeleportEventHashMap.put(player, this);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isCancelled)
                    return;
                playerPlayerPreTeleportEventHashMap.remove(player);
                if (!player.isOnline()) return;
                PlayerTeleportEvent.teleportPlayer(player, destination);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 3);

        new BukkitRunnable() {

            int timerLeft = 3;

            @Override
            public void run() {

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(ChatColorConverter.convert(
                                ConfigValues.translationConfig.getString(
                                        TranslationConfig.TELEPORT_TIME_LEFT
                                ).replace("$time", timerLeft + ""))));

                if (isCancelled) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(ChatColorConverter.convert(
                                    ConfigValues.translationConfig.getString(
                                            TranslationConfig.TELEPORT_CANCELLED))));
                    cancel();
                    return;
                }

                if (timerLeft == 0) {
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
        playerPlayerPreTeleportEventHashMap.remove(player);
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
        @EventHandler(ignoreCancelled = true)
        public void onPlayerMove(PlayerMoveEvent event) {
            PlayerPreTeleportEvent playerPreTeleportEvent = playerPlayerPreTeleportEventHashMap.get(event.getPlayer());
            if (playerPreTeleportEvent == null) return;
            if (playerPreTeleportEvent.getOriginalLocation().getX() != event.getPlayer().getLocation().getX() ||
                    playerPreTeleportEvent.getOriginalLocation().getY() != event.getPlayer().getLocation().getY() ||
                    playerPreTeleportEvent.getOriginalLocation().getZ() != event.getPlayer().getLocation().getZ())
                playerPreTeleportEvent.setCancelled(true);
        }
    }

}
