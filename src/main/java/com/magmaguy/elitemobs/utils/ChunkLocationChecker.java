package com.magmaguy.elitemobs.utils;

import org.bukkit.Chunk;
import org.bukkit.Location;

public class ChunkLocationChecker {

    public static boolean chunkLocationCheck(Location location, Chunk chunk) {
        if (!chunk.getWorld().equals(location.getWorld())) return false;
        double chunkX = chunk.getX() * 16;
        double locationX = location.getX();
        double chunkZ = chunk.getZ() * 16;
        double locationZ = location.getZ();
        if (!(chunkX <= locationX && chunkX + 16 >= locationX))
            return false;
        return chunkZ <= locationZ && chunkZ + 16 >= locationZ;
    }

}
