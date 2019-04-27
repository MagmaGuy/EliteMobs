package com.magmaguy.elitemobs.combattag;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.TranslationConfig;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class TeleportTag implements Listener {

    private static HashSet<Player> teleportingPlayers = new HashSet<>();

    public static HashSet<Player> getTeleportingPlayers() {
        return teleportingPlayers;
    }

    public static boolean isTeleportingPlayer(Player player) {
        return teleportingPlayers.contains(player);
    }

    public static void addTeleportingPlayer(Player player) {
        teleportingPlayers.add(player);
    }

    public static void removeTeleportingPlayer(Player player) {
        teleportingPlayers.remove(player);
    }

    public static void intializePlayerTeleport(Player player, int teleportDuration, Location teleportLocation) {

        if (isTeleportingPlayer(player)) return;
        addTeleportingPlayer(player);

        new BukkitRunnable() {

            int timerLeft = teleportDuration;

            @Override
            public void run() {

                if (!isTeleportingPlayer(player)) {
                    cancel();
                    return;
                }

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(ChatColorConverter.convert(
                                ConfigValues.translationConfig.getString(
                                        TranslationConfig.TELEPORT_TIME_LEFT
                                ).replace("$time", timerLeft + ""))));

                timerLeft--;
                if (timerLeft == 0) {
                    player.teleport(teleportLocation);
                    removeTeleportingPlayer(player);
                    cancel();
                }

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20);

    }

    public static void cancelPlayerTeleport(Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ChatColorConverter.convert(
                        ConfigValues.translationConfig.getString(
                                TranslationConfig.TELEPORT_CANCELLED))));
        removeTeleportingPlayer(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (teleportingPlayers.isEmpty()) return;
        if (isTeleportingPlayer(event.getPlayer())) cancelPlayerTeleport(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) return;
        if (teleportingPlayers.isEmpty()) return;
        if (isTeleportingPlayer((Player) event.getEntity())) cancelPlayerTeleport((Player) event.getEntity());
    }

}
