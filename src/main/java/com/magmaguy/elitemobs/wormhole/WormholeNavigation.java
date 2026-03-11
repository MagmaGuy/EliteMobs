package com.magmaguy.elitemobs.wormhole;

import org.bukkit.Location;
import org.bukkit.World;

public class WormholeNavigation {
    private WormholeNavigation() {
    }

    public static Location findNavigationTarget(Location sourceLocation, Location destinationLocation) {
        if (sourceLocation == null || destinationLocation == null) return null;
        World sourceWorld = sourceLocation.getWorld();
        World destinationWorld = destinationLocation.getWorld();
        if (sourceWorld == null || destinationWorld == null) return null;
        if (sourceWorld.equals(destinationWorld)) return destinationLocation;
        return findDirectWormholeEntry(sourceWorld, destinationWorld);
    }

    public static Location findDirectWormholeEntry(World sourceWorld, World destinationWorld) {
        if (sourceWorld == null || destinationWorld == null || sourceWorld.equals(destinationWorld)) return null;
        for (Wormhole wormhole : Wormhole.getWormholes()) {
            Location entry1 = wormhole.getWormholeEntry1().getLocation();
            Location entry2 = wormhole.getWormholeEntry2().getLocation();
            if (isEntryInWorld(entry1, sourceWorld) && isEntryInWorld(entry2, destinationWorld))
                return entry1;
            if (isEntryInWorld(entry2, sourceWorld) && isEntryInWorld(entry1, destinationWorld))
                return entry2;
        }
        return null;
    }

    private static boolean isEntryInWorld(Location location, World world) {
        return location != null &&
                location.getWorld() != null &&
                location.getWorld().equals(world);
    }
}
