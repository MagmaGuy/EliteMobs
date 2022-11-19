package com.magmaguy.elitemobs.utils;

import org.bukkit.Location;

public class Lerp {
    public static Location lerpLocation(Location startLocation, Location endLocation, double timePercentage) {
        return new Location(startLocation.getWorld(),
                lerp(startLocation.getX(), endLocation.getX(), timePercentage),
                lerp(startLocation.getY(), endLocation.getY(), timePercentage),
                lerp(startLocation.getZ(), endLocation.getZ(), timePercentage));
    }

    public static double lerp(double start, double end, double timePercentage) {
        return start * (1 - timePercentage) + end * timePercentage;
    }
}
