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
import org.bukkit.util.Vector;

import java.util.HashMap;

public class TeleportTag implements Listener {

    private static HashMap<Player, Vector> teleportingPlayers = new HashMap<>();

    public static boolean isTeleportingPlayer(Player player) {
        return teleportingPlayers.containsKey(player);
    }

    public static void addTeleportingPlayer(Player player, Vector location) {
        teleportingPlayers.put(player, location);
    }

    public static void removeTeleportingPlayer(Player player) {
        teleportingPlayers.remove(player);
    }

    public static void initializePlayerTeleport(Player player, int teleportDuration, Location teleportLocation) {

        if (isTeleportingPlayer(player)) return;
        Vector vector = new Vector(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        addTeleportingPlayer(player, vector);

        new BukkitRunnable() {

            int timerLeft = teleportDuration;

            @Override
            public void run() {

                timerLeft--;
                if (timerLeft == 0) {
                    player.teleport(teleportLocation);
                    removeTeleportingPlayer(player);
                    return;
                }

                if (!isTeleportingPlayer(player)) {
                    cancel();
                    return;
                }

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(ChatColorConverter.convert(
                                ConfigValues.translationConfig.getString(
                                        TranslationConfig.TELEPORT_TIME_LEFT
                                ).replace("$time", timerLeft + ""))));

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
        if (isTeleportingPlayer(event.getPlayer()))
            if (!teleportingPlayers.get(event.getPlayer()).equals(
                    new Vector(event.getPlayer().getLocation().getX(),
                            event.getPlayer().getLocation().getY(),
                            event.getPlayer().getLocation().getZ())))
                cancelPlayerTeleport(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) return;
        if (teleportingPlayers.isEmpty()) return;
        if (isTeleportingPlayer((Player) event.getEntity())) cancelPlayerTeleport((Player) event.getEntity());
    }

}
