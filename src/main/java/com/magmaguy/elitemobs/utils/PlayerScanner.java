package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class PlayerScanner {
    private static final int range = Bukkit.getServer().getViewDistance() * 16;

    public static ArrayList<Player> getNearbyPlayers(Location location) {
        ArrayList<Player> nearbyPlayers = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers())
            if (player.getWorld().equals(location.getWorld()))
                if (player.getLocation().distanceSquared(location) <= range * range)
                    nearbyPlayers.add(player);
        return nearbyPlayers;
    }
}
