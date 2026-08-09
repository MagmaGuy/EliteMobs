package com.magmaguy.elitemobs.utils;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EntitySearch {
    private EntitySearch() {
    }

    public static List<Player> getNearbyCombatPlayers(Location location, double distance) {
        List<Player> players = new ArrayList<>();
        List<Player> worldPlayers = location.getWorld().getPlayers();
        if (worldPlayers.isEmpty()) return players;
        double distanceSquared = distance * distance;
        worldPlayers.forEach(player -> {
            if (player.getGameMode().equals(GameMode.SPECTATOR)) return;
            if (player.getLocation().distanceSquared(location) <= distanceSquared) players.add(player);
        });
        return players;
    }
}
