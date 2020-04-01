package com.magmaguy.elitemobs.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class DebugBlockLocation {

    public DebugBlockLocation(Location location) {
        location.getWorld().spawnParticle(Particle.BARRIER, location.getBlock().getLocation().add(new Vector(0.5, 0.5, 0.5)), 1);
    }

}
