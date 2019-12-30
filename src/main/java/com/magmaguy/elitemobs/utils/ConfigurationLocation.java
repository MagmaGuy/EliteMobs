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

        int counter = 0;
        World world = null;
        double x = 0;
        double y = 0;
        double z = 0;
        float yaw = 0;
        float pitch = 0;

        try {

            for (String substring : locationString.split(",")) {
                switch (counter) {
                    case 0:
                    /*
                    World is contained here
                     */
                        world = Bukkit.getWorld(substring);
                        break;
                    case 1:
                    /*
                    X value is contained here
                     */
                        x = Double.valueOf(substring);
                        break;
                    case 2:
                    /*
                    Y value is contained here
                     */
                        y = Double.valueOf(substring);
                        break;
                    case 3:
                    /*
                    Z value is contained here
                     */
                        z = Double.valueOf(substring);
                        break;
                    case 4:
                        yaw = Float.valueOf(substring);
                        break;
                    case 5:
                        pitch = Float.valueOf(substring);
                        break;
                }

                counter++;
            }

        } catch (Exception ex) {
            new WarningMessage("Attempted to deserialize an invalid location!");
            new WarningMessage("Expected location format: worldname,x,y,z,pitch,yaw");
            new WarningMessage("Real location format: " + locationString);
        }

        if (world == null) return null;

        return new Location(world, x, y, z, yaw, pitch);
    }

}
