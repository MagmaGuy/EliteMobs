package com.magmaguy.elitemobs.thirdparty.mythicmobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class MythicMobsInterface {
    public static Entity spawn(Location location, String name, int level) {
        if (!Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) return null;
        return MythicMobs.spawn(location, name, level);
    }
}
