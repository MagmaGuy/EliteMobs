package com.magmaguy.elitemobs.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class DebugBlockLocation {

    public DebugBlockLocation(Location location) {
        if (!VersionChecker.serverVersionOlderThan(18, 0))
            location.getWorld().spawnParticle(Particle.BLOCK_MARKER, location.getBlock().getLocation().add(new Vector(0.5, 0.5, 0.5)), 1);
        else
            location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location.getBlock().getLocation().add(new Vector(0.5, 0.5, 0.5)), 1);
    }

}
