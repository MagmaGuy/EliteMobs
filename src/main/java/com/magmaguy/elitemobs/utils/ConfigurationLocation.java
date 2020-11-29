package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class ConfigurationLocation {

    /*
    Location format: worldname,x,y,z,pitch,yaw
     */
    public static String serialize(String worldName, double x, double y, double z, float pitch, float yaw) {
        return worldName + "," + x + "," + y + "," + z + "," + pitch + "," + yaw;
    }

    public static String serialize(Location location) {
        return serialize(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
    }

    public static Location deserialize(String locationString) {

        World world = null;
        double x = 0;
        double y = 0;
        double z = 0;
        float yaw = 0;
        float pitch = 0;

        try {
            String locationOnlyString = locationString.split(":")[0];

            world = Bukkit.getWorld(getSubString(locationOnlyString, 0, ","));
            x = Double.parseDouble(getSubString(locationOnlyString, 1, ","));
            y = Double.parseDouble(getSubString(locationOnlyString, 2, ","));
            z = Double.parseDouble(getSubString(locationOnlyString, 3, ","));
            yaw = Float.parseFloat(getSubString(locationOnlyString, 4, ","));
            pitch = Float.parseFloat(getSubString(locationOnlyString, 5, ","));
        } catch (Exception ex) {
            if (locationString == null || locationString.equals("null"))
                return null;
            new WarningMessage("Attempted to deserialize an invalid location!");
            new WarningMessage("Expected location format: worldname,x,y,z,pitch,yaw");
            new WarningMessage("Real location format: " + locationString);
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    private static String getSubString(String originalString, int index, String splitter) {
        return originalString.split(splitter)[index];
    }

}
