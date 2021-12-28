package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ConfigurationLocation {
    private static final Set<String> notLoadedWorldNames = new HashSet<>();

    private ConfigurationLocation() {
    }

    /*
    Location format: worldname,x,y,z,pitch,yaw
     */
    public static String deserialize(String worldName, double x, double y, double z, float pitch, float yaw) {
        return worldName + "," + x + "," + y + "," + z + "," + pitch + "," + yaw;
    }

    public static String deserialize(Location location) {
        return deserialize(Objects.requireNonNull(location.getWorld()).getName(), location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
    }

    public static Location serialize(String locationString) {

        if (locationString == null)
            return null;

        World world = null;
        double x = 0;
        double y = 0;
        double z = 0;
        float yaw = 0;
        float pitch = 0;

        try {
            String locationOnlyString = locationString.split(":")[0];
            String[] slicedString = locationOnlyString.split(",");

            world = Bukkit.getWorld(slicedString[0]);
            if (world == null && !notLoadedWorldNames.contains(slicedString[0])) {
                if (!notLoadedWorldNames.isEmpty())
                    new WarningMessage("Some NPCs/bosses don't their world installed! If you need help setting things up, you can go to " + DiscordLinks.mainLink + " !");
                new InfoMessage("World " + slicedString[0] + " is not yet loaded! Entities that should spawn there have been queued.");
                notLoadedWorldNames.add(slicedString[0]);
            }
            x = Double.parseDouble(slicedString[1]);
            y = Double.parseDouble(slicedString[2]);
            z = Double.parseDouble(slicedString[3]);
            yaw = Float.parseFloat(slicedString[4]);
            pitch = Float.parseFloat(slicedString[5]);
        } catch (Exception ex) {
            if (locationString.equals("null"))
                return null;
            new WarningMessage("Attempted to deserialize an invalid location!");
            new WarningMessage("Expected location format: worldname,x,y,z,pitch,yaw");
            new WarningMessage("Actual location format: " + locationString);
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

}
