package com.magmaguy.elitemobs.gamemodes.zoneworld;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import com.magmaguy.elitemobs.utils.WarningMessage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;

public class ZoneWarner implements Listener {

    public static HashMap<Player, EliteChunk> playerLocations = new HashMap<>();

    private static void sendMessage(Player player) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(
                            ChatColorConverter.convert("&8[EM] &7You've entered a tier &c"
                                    + Grid.getChunkMap().get(Grid.getEliteChunk(player.getLocation()))
                                    + " &7zone (" + Grid.getEliteChunk(player.getLocation()).getxCoord() + "," +
                                    Grid.getEliteChunk(player.getLocation()).getzCoord() + ")")));
        } catch (Exception ex) {
            new WarningMessage("Failed to get the chunk region. This may be normal if you're trying to move around while EliteMobs is still starting up.");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        if (!ValidWorldsConfig.getZoneBasedWorlds().contains(event.getPlayer().getWorld().getName()))
            return;

        if (!playerLocations.containsKey(event.getPlayer())) {
            playerLocations.put(event.getPlayer(), Grid.getEliteChunk(event.getPlayer().getLocation()));
            sendMessage(event.getPlayer());
        }

        if (Grid.getEliteChunk(event.getPlayer().getLocation()).equals(playerLocations.get(event.getPlayer()))) return;
        playerLocations.put(event.getPlayer(), Grid.getEliteChunk(event.getPlayer().getLocation()));
        sendMessage(event.getPlayer());

    }

}
