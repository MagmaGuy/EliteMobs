package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class PlayerPreTeleportEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Location destination;
    @Getter
    private final Location originalLocation;
    @Getter
    private final Player player;
    private final boolean deferredStart;
    private boolean isCancelled = false;

    /**
     * Called when a player initiates a teleport to a location. The teleport will go through 3 seconds after the event is
     * launched, assuming it isn't cancelled by the player or via code.
     *
     * @param player      Player that will teleport
     * @param destination Teleport destination
     */
    public PlayerPreTeleportEvent(Player player, Location destination) {
        this(player, destination, false);
    }

    private PlayerPreTeleportEvent(Player player, Location destination, boolean deferredStart) {
        this.player = player;
        this.destination = destination.clone();
        this.originalLocation = player.getLocation().clone();
        this.deferredStart = deferredStart;
    }

    public static void teleportPlayer(Player player, Location destination) {
        if (destination.getWorld() == null) return;
        if (CombatTagConfig.isEnableTeleportTimer())
            new EventCaller(new PlayerPreTeleportEvent(player, destination));
        else
            PlayerTeleportEvent.teleportPlayer(player, destination);
    }

    /**
     * Starts a group teleport only after every member's cancellable EliteMobs event accepts it.
     * Once accepted, each player keeps the ordinary movement-cancellable countdown.
     *
     * @return true when all events passed preflight and all countdowns/final teleports were started
     */
    public static boolean teleportPlayers(Collection<Player> players, Location destination) {
        if (destination == null || destination.getWorld() == null) return false;
        if (!CombatTagConfig.isEnableTeleportTimer())
            return PlayerTeleportEvent.teleportPlayers(players, destination);

        LinkedHashMap<UUID, Player> uniquePlayers = new LinkedHashMap<>();
        for (Player player : players)
            if (player != null) uniquePlayers.putIfAbsent(player.getUniqueId(), player);
        if (uniquePlayers.isEmpty()) return false;

        List<PlayerPreTeleportEvent> events = new ArrayList<>();
        for (Player player : uniquePlayers.values()) {
            if (!player.isOnline() || !player.isValid()) return false;
            PlayerPreTeleportEvent event = new PlayerPreTeleportEvent(player, destination, true);
            new EventCaller(event);
            if (event.isCancelled()) return false;
            events.add(event);
        }
        if (events.stream().anyMatch(event -> !event.player.isOnline() || !event.player.isValid())) return false;
        events.forEach(PlayerPreTeleportEvent::startTeleport);
        return true;
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

                ChatMessageType chatMessageType = CombatTagConfig.isUseActionBarMessagesInsteadOfChat() ? ChatMessageType.ACTION_BAR : ChatMessageType.CHAT;

                if (isCancelled) {
                    player.spigot().sendMessage(chatMessageType,
                            TextComponent.fromLegacyText(CombatTagConfig.getTeleportCancelled()));
                    cancel();
                    return;
                }

                player.spigot().sendMessage(chatMessageType,
                        TextComponent.fromLegacyText(CombatTagConfig.getTeleportTimeLeft()
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

    public static class PlayerPreTeleportEventEvents implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onTeleportEvent(PlayerPreTeleportEvent event) {
            if (!event.deferredStart) event.startTeleport();
        }
    }

}
