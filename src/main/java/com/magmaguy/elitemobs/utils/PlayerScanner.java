package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerScanner {
    private static final int range = Math.max(Bukkit.getServer().getViewDistance() * 16, 5 * 16);

    public static List<Player> getNearbyPlayers(Location location) {
        List<Player> nearbyPlayers = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers())
            if (player.getWorld().equals(location.getWorld()))
                if (player.getLocation().distanceSquared(location) <= range * range)
                    nearbyPlayers.add(player);
        return nearbyPlayers;
    }
}
