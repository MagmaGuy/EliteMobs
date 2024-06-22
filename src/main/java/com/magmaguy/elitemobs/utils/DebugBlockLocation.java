package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;

public class DebugBlockLocation {

    public DebugBlockLocation(Location location) {
        location.getWorld().spawnParticle(Particle.BLOCK_MARKER,
                location.getBlock().getX() + .5,
                location.getBlock().getY() + .5,
                location.getBlock().getZ() + .5,
                1,
                Bukkit.createBlockData(Material.BARRIER));
    }

}
